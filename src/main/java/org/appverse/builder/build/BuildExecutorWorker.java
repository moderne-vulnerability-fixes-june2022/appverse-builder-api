package org.appverse.builder.build;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.appverse.builder.build.comand.BuildCommand;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.domain.enumeration.BuildStatus;
import org.appverse.builder.notification.Notification;
import org.appverse.builder.service.*;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by panthro on 17/01/16.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class BuildExecutorWorker implements Runnable {

    public static final String ARTIFACT_REGEX = "artifactRegex";
    public static final String BUILD_TIMEOUT = "build.timeout";
    private final Logger log = LoggerFactory.getLogger(BuildExecutorWorker.class);

    public static InputStream raceConditionStream() {
        return new ByteArrayInputStream("Something unexpected has happened and we couldn't get the logs, please try again in a few seconds".getBytes());
    }

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private BuildRequestService buildRequestService;

    @Inject
    private BuildCommandBuilderService buildCommandBuilderService;

    @Inject
    private BuildAgentService buildAgentService;

    @Inject
    private DistributionChannelService distributionChannelService;

    @Inject
    private BuildChainService buildChainService;

    @Inject
    private NotificationChannelService notificationChannelService;

    private volatile BuildAgentQueueController buildServerController;

    private volatile BuildRequestDTO currentBuildRequest;

    private volatile PrintWriter currentLogger;

    private final List<PrintWriter> connectedLoggers = new CopyOnWriteArrayList<>();

    private volatile File currentLogFile;

    public BuildExecutorWorker(BuildAgentQueueController buildServerController) {
        this.buildServerController = buildServerController;
    }

    public BuildAgentQueueController getBuildServerController() {
        return buildServerController;
    }

    public AppverseBuilderProperties getAppverseBuilderProperties() {
        return appverseBuilderProperties;
    }

    public void setAppverseBuilderProperties(AppverseBuilderProperties appverseBuilderProperties) {
        this.appverseBuilderProperties = appverseBuilderProperties;
    }

    public void setBuildServerController(BuildAgentQueueController buildServerController) {
        this.buildServerController = buildServerController;
    }

    protected void log(String line, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug("[{}] BUILD-LOG-LINE: {}", currentBuildRequest.getId(), line, params);
        }
        if (currentLogger != null) {
            try {
                currentLogger.println(DateTime.now().toString() + " " + MessageFormatter.arrayFormat(line, params).getMessage());
                currentLogger.flush();
            } catch (Throwable t) {
                log.debug("Could not write log message {} to build log", line, t);
            }
        }
    }

    protected void logError(String line, Object... params) {
        //TODO we might want to add an "ERROR" tag to this logs
        log(line, params);
    }

    private PrintWriter createLogWriter() throws IOException {
        return new PrintWriter(new FileWriterWithEncoding(getBuildLogFile(), "UTF-8"), true) {
            @Override
            public void println(String s) {
                super.println(s);
                super.flush();
                try {
                    connectedLoggers.forEach(writer -> {
                        writer.println(s);
                        writer.flush();
                    });
                } catch (Throwable t) {
                    log.warn("Error writing to to the log", t);
                }
            }

            @Override
            public void close() {
                super.close();
                connectedLoggers.forEach(PrintWriter::close);
                connectedLoggers.clear();
            }
        };
    }


    public InputStream getLogInputStream() {
        if (!isExecuting()) {
            return raceConditionStream();
        } else {
            try {
                if (currentBuildRequest == null) {
                    return raceConditionStream();
                } else {
                    final PipedOutputStream pipedOutputStream = new PipedOutputStream();
                    final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
                    final PrintWriter logger = new PrintWriter(pipedOutputStream);
                    connectedLoggers.add(logger);
                    return pipedInputStream;
                }
            } catch (IOException e) {
                log.warn("Error getting logs for request {}", currentBuildRequest, e);
                return raceConditionStream();
            }
        }
    }

    private File getBuildLogFile() {
        if (currentLogFile == null) {
            try {
                //TODO find a better place to store the log file
                currentLogFile = File.createTempFile(currentBuildRequest.getChainId().toString() + "-" + currentBuildRequest.getId(), "log");
            } catch (IOException e) {
                log.warn("Could not create the temporary log file", e);
            }
        }
        return currentLogFile;
    }

    @Override
    public void run() {
        log.info("Worker initialized");
        try {
            while (!Thread.currentThread().isInterrupted() && buildServerController.isRunning()) {
                log.info("Waiting for new tasks");

                currentBuildRequest = buildServerController.getNext();
                log.info("Received new task, execution will start now {}", currentBuildRequest);
                currentBuildRequest.start();
                sendNotification(currentBuildRequest, Notification.Event.BUILD_REQUEST_STARTED);
                try {
                    currentLogger = createLogWriter();
                    buildRequestService.save(currentBuildRequest);
                    execute(currentBuildRequest);
                } catch (Throwable t) {
                    log.warn("Unhandled Exception during build execution, this is likely to be an error: ", t);
                    failed(t.getMessage().length() > 250 ? t.getMessage().substring(0, 250) : t.getMessage());
                } finally {
                    log.info("Finished task execution {}", currentBuildRequest);
                    distributionChannelService.distributeArtifacts(currentBuildRequest, getRequestArtifacts());
                    try {
                        cleanup(currentBuildRequest);
                    } catch (Exception e) {
                        log.warn("Exception cleaning up on agent {} for request {}", buildServerController.getBuildAgent(), currentBuildRequest);
                    }
                    IOUtils.closeQuietly(currentLogger);
                    connectedLoggers.clear();
                    distributionChannelService.distributeLog(currentBuildRequest, getBuildLogFile());
                    FileUtils.deleteQuietly(getBuildLogFile());
                    if (!currentBuildRequest.isFinished()) {
                        success("Build Successful");
                    }
                    currentLogFile = null;
                    currentBuildRequest = null;
                    currentLogger = null;

                }
            }
            log.info("Current thread is interrupted, finishing worker");
        } catch (InterruptedException e) {
            log.warn("Got an interruption signal, exiting");
        }
    }

    private void sendNotification(BuildRequestDTO buildRequest, Notification.Event event) {
        Notification notification = notificationChannelService.buildBuildRequestNotification(buildRequest, event);
        notificationChannelService.sendNotificationToAllChannels(notification);
    }

    protected abstract void cleanup(BuildRequestDTO request);

    protected String getRemoteRequestRootDir(BuildRequestDTO request) {
        return buildAgentService.getBuildAgentRemoteRequestDir(getBuildServerController().getBuildAgent(), request);
    }

    protected File getBuildRequestRootDir() {
        return buildRequestService.getBuildRequestRootDir(currentBuildRequest);
    }

    protected abstract void execute(BuildRequestDTO request);

    protected void success(String message) {
        log.debug("Request {} SUCCESSFUL with message {} ", currentBuildRequest.getId(), message);
        currentBuildRequest.finish(BuildStatus.SUCCESSFUL, message);
        buildRequestService.save(currentBuildRequest);
        sendNotification(currentBuildRequest, Notification.Event.BUILD_REQUEST_FINISHED);
    }

    protected void failed(String message) {
        log.debug("Request {} FAILED with message {} ", currentBuildRequest.getId(), message);
        currentBuildRequest.finish(BuildStatus.FAILED, message);
        buildRequestService.save(currentBuildRequest);
        sendNotification(currentBuildRequest, Notification.Event.BUILD_REQUEST_FAILED);
    }

    public boolean isExecuting() {
        return currentBuildRequest != null;
    }

    public BuildRequestDTO getCurrentBuildRequest() {
        return currentBuildRequest;
    }

    public Optional<BuildRequestDTO> getExecutingBuildRequest() {
        return Optional.ofNullable(currentBuildRequest);
    }

    protected String getBuildAgentProperty(String key) {
        return getBuildServerController().getBuildAgent().getProperties().get(key);
    }

    protected String getEndpoint() {
        return Optional.ofNullable(getBuildServerController().getBuildAgent().getProperties().get("endpoint"))
            .orElse(getBuildServerController().getBuildAgent().getName());
    }

    protected Optional<BuildCommand> buildExecutionCommand(BuildRequestDTO request) {
        return buildCommandBuilderService.buildCommandFor(getBuildServerController().getBuildAgent(), request);
    }

    protected File getBuildChainCompressedInput() {
        return buildChainService.getBuildChainCompressedInput(getCurrentBuildRequest().getChainId());
    }

    protected Optional<File> getTemporaryArtifactsDir() {
        File localArtifactsDir = new File(getBuildRequestRootDir(), appverseBuilderProperties.getBuild().getArtifactsDirName());
        if (!localArtifactsDir.exists()) {
            if (!localArtifactsDir.mkdirs()) {
                log.warn("could not create local artifacts dir {} , no artifacts will be saved", localArtifactsDir);
                logError("error creating artifacts dir, no artifacts will be retrieved");
                return Optional.empty();
            }
        }
        return Optional.of(localArtifactsDir);
    }

    protected void redirectErrorStream(InputStream errorStream) {
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
        new Thread(() -> {
            errorReader.lines().forEachOrdered(BuildExecutorWorker.this::logError);
            log.debug("[{}] Finished writing error stream from execution", currentBuildRequest.getId());
        }).start();
    }

    protected void redirectInputStream(InputStream errorStream) {
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
        new Thread(() -> {
            errorReader.lines().forEachOrdered(BuildExecutorWorker.this::log);
            log.debug("[{}] Finished writing logs stream from execution", currentBuildRequest.getId());
        }).start();
    }

    protected List<Artifact> getRequestArtifacts() {
        File[] files = getTemporaryArtifactsDir().map(File::listFiles).orElse(new File[0]);
        return Stream.of(files)
            .map(file -> new Artifact(file.toURI(), file.getName(), file.length()))
            .collect(Collectors.toList());

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected File createBuildScript(BuildCommand buildCommand, File inputDir) throws IOException {
        File file = new File(inputDir, buildCommand.getScriptFileName());
        FileUtils.writeStringToFile(file, buildCommand.getBuildScript());
        file.setExecutable(true);
        return file;
    }

    protected Long getTimeoutForRequest() {
        return Optional
            .ofNullable(getCurrentBuildRequest().getVariables().get(BUILD_TIMEOUT))
            .map(Long::parseLong)
            .orElse(getAppverseBuilderProperties().getBuild().getMaxBuildTimeout());
    }
}
