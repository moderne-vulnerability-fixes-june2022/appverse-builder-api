package org.appverse.builder.build.ssh;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import org.apache.commons.io.FileUtils;
import org.appverse.builder.build.BuildAgentQueueController;
import org.appverse.builder.build.BuildExecutorWorker;
import org.appverse.builder.build.comand.BuildCommand;
import org.appverse.builder.service.BuildAgentService;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by panthro on 17/01/16.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SSHBuildExecutorWorker extends BuildExecutorWorker {

    private static final Logger log = LoggerFactory.getLogger(SSHBuildExecutorWorker.class);
    private static final String SSH_PASSWORD = "ssh.password";
    private static final String SSH_USER = "ssh.user";
    private static final String SSH_PORT = "ssh.port";
    private static final String SSH_KEY_LOCATION = "ssh.key.location";
    public static final String FIND = "find ";
    public static final String PROVISIONING = "PROVISIONING";
    public static final String EXTRACTING_PAYLOAD = "EXTRACTING PAYLOAD";
    public static final String BUILD = "BUILD";
    public static final String DOWNLOAD_ARTIFACTS = "DOWNLOAD ARTIFACTS";
    public static final int EXEC_PERMS = 755;
    public static final int WAIT_FOR_CLEANUP = 3;


    public SSHBuildExecutorWorker(BuildAgentQueueController buildServerController) {
        super(buildServerController);
    }

    @Override
    protected void cleanup(BuildRequestDTO request) {
        try {
            log("Cleaning up agent");
            final SSHClient client = getConnectedSshClient();
            Session session = client.startSession();
            // Create the remote dir where all build will occur
            final Session.Command rmDircommand = session.exec("rm -rf " + getRemoteRequestRootDir(request));
            try {
                rmDircommand.join(WAIT_FOR_CLEANUP, TimeUnit.SECONDS);
                if (!hasExecutedSuccessfully(rmDircommand)) {
                    logError("Could not cleanup all files");
                }
            } catch (Throwable t) {
                logError("Could not cleanup all files");
            }
            log("Cleaning up artifacts");
            log.debug("Cleaning up artifacts folder {}", getTemporaryArtifactsDir());
            getTemporaryArtifactsDir().ifPresent(FileUtils::deleteQuietly);
            log("Cleanup finished");
        } catch (IOException e) {
            log.info("Could execute cleanup on agent {} for request {}", getBuildServerController().getBuildAgent(), request);
            log.info("This is usually not a problem, but some residual files/folders cane be there");
        }
    }

    @Override
    public void execute(BuildRequestDTO request) {
        log.info("Server {} is starting execution of request {}", getBuildServerController().getBuildAgent().getName(), request);
        SSHClient tryClient = null;
        StopWatch stopWatch = new StopWatch(request.getId().toString());
        try {
            stopWatch.start(PROVISIONING);
            log("Build is starting");
            log("Initializing environment");
            log("Connecting to remote agent");
            tryClient = getConnectedSshClient();
            final SSHClient client = tryClient;


            log("Connected");
            String remoteRequestBuildRoot = getRemoteRequestRootDir(request);
            Session session;

            log.debug("Creating build dir");
            log("Creating build directory");
            session = client.startSession();
            // Create the remote dir where all build will occur
            final Session.Command mkdirCommand = session.exec("mkdir -p " + remoteRequestBuildRoot);
            mkdirCommand.join(getTimeoutForRequest(), TimeUnit.SECONDS);
            log.debug("Command output: {}", IOUtils.readFully(mkdirCommand.getInputStream()).toString());
            if (!hasExecutedSuccessfully(mkdirCommand)) {
                failed("Could not create necessary directories to execute the build");
                return;
            }

            File compressedInput = getBuildChainCompressedInput();
            log("Uploading build payload to build agent");
            log.debug("Uploading payload {} to {} ", compressedInput, remoteRequestBuildRoot);
            try {
                //upload the payload
                SCPFileTransfer fileTransfer = client.newSCPFileTransfer();
                fileTransfer.upload(compressedInput.getAbsolutePath(), remoteRequestBuildRoot);
                log("Upload finished");
            } catch (IOException e) {
                log.warn("Error uploading payload", e);
                failed("Error uploading the payload to the build server");
                return;
            }

            // create all necessary sub folders inside the buildRoot dir
            log("Creating necessary directories to execute the build");
            Stream.of(getAppverseBuilderProperties().getBuild().getInputDirName()).forEach(dir -> {
                try {
                    final Session mkdirSession = client.startSession();
                    mkdirSession.exec("mkdir -p " + remoteRequestBuildRoot + getAgentFileSeparator() + dir); //TODO find a way to determine the remote separator
                } catch (ConnectionException | TransportException e) {
                    failed("Could not create necessary build directories on the build server");
                }
            });
            stopWatch.stop();
            stopWatch.start(EXTRACTING_PAYLOAD);
            log.debug("Extracting payload");
            log("Extracting payload");
            session = client.startSession();
            //TODO externalize the unzip command by agent property
            //TODO determine remote separator
            //TODO use a Command builder class/interface to create the extract command
            String remoteRequestInputDir = remoteRequestBuildRoot + getAgentFileSeparator() + getAppverseBuilderProperties().getBuild().getInputDirName();
            final Session.Command unzipCommand = session.exec("unzip " + remoteRequestBuildRoot + getAgentFileSeparator() + compressedInput.getName() + "  -d " + remoteRequestInputDir);
            BufferedReader unzipCommandReader = new BufferedReader(new InputStreamReader(unzipCommand.getInputStream()));
            unzipCommandReader.lines().forEachOrdered(s -> log.debug("UNZIP-RESULT: {}", s));
            unzipCommand.join(getTimeoutForRequest(), TimeUnit.SECONDS);
            if (!hasExecutedSuccessfully(unzipCommand)) {
                failed("Could not extract the payload on the build server");
                return;
            }
            stopWatch.stop();
            stopWatch.start(BUILD);
            session = client.startSession();
            Optional<BuildCommand> commandToExecute = buildExecutionCommand(request);
            if (!commandToExecute.isPresent()) {
                failed("Could not build the command to execute the request");
                log.warn("Build command not found for request {} on agent {} ", request, getBuildServerController().getBuildAgent());
                return;
            }
            BuildCommand buildCommand = commandToExecute.get();
            if (buildCommand.isCreateScript()) {
                File buildScript = createBuildScript(buildCommand, getBuildRequestRootDir());
                SFTPClient sftpClient = client.newSFTPClient();
                sftpClient.put(new FileSystemFile(buildScript), remoteRequestInputDir);
                sftpClient.chmod(remoteRequestInputDir + getAgentFileSeparator() + buildScript.getName(), EXEC_PERMS);
                sftpClient.close();
                //Make sure file has execution permission
                client.startSession().exec("chmod " + EXEC_PERMS + " " + remoteRequestInputDir + getAgentFileSeparator() + buildScript.getName()).join();
            }
            log.debug("Executing the build command {} ", buildCommand);
            Session.Command buildProcess = session.exec(buildCommand.asString());
            redirectInputStream(buildProcess.getInputStream());
            redirectErrorStream(buildProcess.getErrorStream());
            log.debug("[{}] Joining build execution command", getCurrentBuildRequest().getId());
            buildProcess.join(getTimeoutForRequest(), TimeUnit.SECONDS);
            log.debug("[{}] Build command execution finished ", getCurrentBuildRequest().getId());
            if (!hasExecutedSuccessfully(buildProcess)) {
                log("Exit message: " + buildProcess.getExitErrorMessage());
                failed("Error executing the build command");
                return;
            }
            stopWatch.stop();
            stopWatch.start(DOWNLOAD_ARTIFACTS);
            log.debug("[{}] Build execution finished ", getCurrentBuildRequest().getId());
            Optional.ofNullable(request.getVariables().get(ARTIFACT_REGEX)).ifPresent(artifactsRegex -> {
                getTemporaryArtifactsDir().ifPresent(localArtifactsDir -> {
                    try {
                        SFTPClient sftpClient = client.newSFTPClient();
                        List<String> artifacts = getArtifacts(client, remoteRequestInputDir, artifactsRegex);
                        if (artifacts.isEmpty()) {
                            logError("Could not find any artifact with the specified artifactRegex {} ", artifactsRegex);
                        } else {
                            log("downloading {} artifacts from agent", artifacts.size());
                            artifacts
                                .forEach(remoteFile -> {
                                    try {
                                        sftpClient.get(remoteFile, localArtifactsDir.getAbsolutePath());
                                    } catch (IOException e) {
                                        log.warn("Could not download remote artifact {} from agent {}", remoteFile, getBuildServerController().getBuildAgent());
                                    }
                                });
                        }
                    } catch (Throwable e) {
                        log.warn("Error downloading artifacts", e);
                        logError("Error downloading artifacts");
                    }
                });
            });
            stopWatch.stop();
            log(stopWatch.prettyPrint());
        } catch (Throwable e) {
            logError("BUILD FAILURE: {}", e.getMessage());
            failed("Build has failed, check logs");
        } finally {
            if (tryClient != null) {
                try {
                    tryClient.close();
                } catch (IOException e) {
                    log.warn("could not close client", e);
                }
            }
        }
    }

    private String getAgentFileSeparator() {
        return Optional.ofNullable(getBuildAgentProperty(BuildAgentService.FILE_SEPARATOR))
            .orElse(getAppverseBuilderProperties().getAgent().getDefaultFileSeparator());
    }


    private List<String> getArtifacts(SSHClient client, String path, String artifactsRegex) {
        log("Finding artifacts in agent");
        Pattern pattern = Pattern.compile(artifactsRegex);
        try {
            Session session = client.startSession();
            Session.Command find = session.exec(FIND + path);
            List<String> artifacts;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(find.getInputStream()))) {
                artifacts = reader.lines().filter(file -> pattern.matcher(file).find()).collect(Collectors.toList());
            }
            find.join(10L, TimeUnit.SECONDS);
            return artifacts;
            //if (hasExecutedSuccessfully(find)) {

            //}
        } catch (Throwable e) {
            log.warn("Could not get artifacts from path [{}] ", path, e);

        }
        return Collections.emptyList();
    }


    private SSHClient getConnectedSshClient() throws IOException {
        DefaultConfig config = new DefaultConfig();
        config.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
        final SSHClient ssh = new SSHClient(config);
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.loadKnownHosts();
        ssh.connect(getEndpoint(), getSshPort());
        try {
            ssh.authPublickey(getSshUser());
        } catch (Exception e) {
            log.debug("Could not connect using user's public key. Will try with agent specific parameters");
            ssh.auth(getSshUser(), getAuthMethods());
        }
        return ssh;
    }

    private boolean hasExecutedSuccessfully(Session.Command command) {
        return command.getExitStatus() != null && command.getExitStatus() == 0;
    }

    private AuthMethod[] getAuthMethods() {
        List<AuthMethod> authMethods = new ArrayList<>();
        Optional.ofNullable(getBuildAgentProperty(SSH_PASSWORD)).ifPresent(password -> authMethods.add(new AuthPassword(new PasswordFinder() {
            @Override
            public char[] reqPassword(Resource<?> resource) {
                return password.toCharArray();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        })));
        Optional.ofNullable(getBuildAgentProperty(SSH_KEY_LOCATION)).ifPresent(location -> {
            OpenSSHKeyFile openSSHKeyFile = new OpenSSHKeyFile();
            openSSHKeyFile.init(new File(location));
            authMethods.add(new AuthPublickey(openSSHKeyFile));
        });
        return authMethods.toArray(new AuthMethod[authMethods.size()]);
    }

    private String getSshUser() {
        return Optional.ofNullable(getBuildServerController().getBuildAgent().getProperties().get(SSH_USER))
            .orElse(getAppverseBuilderProperties().getAgent().getDefaultAgentUser());

    }

    private int getSshPort() {
        return Optional.ofNullable(getBuildServerController().getBuildAgent().getProperties().get(SSH_PORT))
            .map(Integer::valueOf)
            .orElse(getAppverseBuilderProperties().getAgent().getDefaultSshPort());
    }


}
