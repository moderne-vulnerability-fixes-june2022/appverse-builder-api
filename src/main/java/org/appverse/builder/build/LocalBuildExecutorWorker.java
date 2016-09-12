package org.appverse.builder.build;

import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;
import org.appverse.builder.build.comand.BuildCommand;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by panthro on 22/02/16.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LocalBuildExecutorWorker extends BuildExecutorWorker {

    private static final Logger log = LoggerFactory.getLogger(LocalBuildExecutorWorker.class);

    public LocalBuildExecutorWorker(BuildAgentQueueController buildServerController) {
        super(buildServerController);
    }

    @Override
    protected void cleanup(BuildRequestDTO request) {
        log("Cleaning up agent");
        getTemporaryArtifactsDir().ifPresent(FileUtils::deleteQuietly);
        log("Cleanup finished");
    }

    @Override
    protected void execute(BuildRequestDTO request) {

        log("Build is starting");
        log("Initializing environment");

        String remoteRequestBuildPath = getRemoteRequestRootDir(request);


        log.debug("Creating build dir");
        log("Creating build directory");

        File remoteRequestBuildRoot = new File(remoteRequestBuildPath);
        if (!remoteRequestBuildRoot.exists() && !remoteRequestBuildRoot.mkdirs()) {
            failed("Could not create necessary directories to execute the build");
            return;
        }


        File compressedInput = getBuildChainCompressedInput();
        /*
        log("Uploading build payload to build agent");
        log.debug("Uploading payload {} to {} ", compressedInput, remoteRequestBuildRoot);


        try {
            FileUtils.copyFileToDirectory(compressedInput, remoteRequestBuildRoot);
            log("Upload finished");
        } catch (IOException e) {
            log.warn("Error uploading payload", e);
            failed("Error uploading the payload to the build server");
            return;
        }
        */

        // create all necessary sub folders inside the buildRoot dir
        log("Creating necessary directories to execute the build");
        File inputDir = new File(remoteRequestBuildRoot, getAppverseBuilderProperties().getBuild().getInputDirName());
        if (!inputDir.exists() && !inputDir.mkdirs()) {
            failed("Could not create necessary build directories on the build server");
            return;
        }

        log.debug("Extracting payload");
        log("Extracting payload");
        try {
            ZipFile zipFile = new ZipFile(compressedInput);
            zipFile.extractAll(inputDir.getAbsolutePath());
            log("Payload extracted");
        } catch (Exception e) {
            logError("Error extracting compressed file");
            failed(e.getMessage());
        }

        Optional<BuildCommand> commandToExecute = buildExecutionCommand(request);
        if (!commandToExecute.isPresent()) {
            failed("Could not build the command to execute the request");
            log.warn("Build command not found for request {} on agent {} ", request, getBuildServerController().getBuildAgent());
            return;
        }
        BuildCommand buildCommand = commandToExecute.get();
        log.debug("Executing the build command [{}] ", buildCommand);
        try {
            if (buildCommand.isCreateScript()) {
                createBuildScript(buildCommand, inputDir);
            }

            Process buildProcess = Runtime.getRuntime().exec(buildCommand.asArray());
            redirectInputStream(buildProcess.getInputStream());
            redirectErrorStream(buildProcess.getErrorStream());
            if (!buildProcess.waitFor(getTimeoutForRequest(), TimeUnit.SECONDS) || buildProcess.exitValue() != 0) {
                failed("Error executing the build command");
                return;
            }
        } catch (IOException | InterruptedException e) {
            logError("Error executing the build command {}", e.getMessage());
            failed("Error executing the build command");
            return;
        }


        Optional.ofNullable(request.getVariables().get(ARTIFACT_REGEX)).ifPresent(artifactsRegex -> {
            getTemporaryArtifactsDir().ifPresent(localArtifactsDir -> {
                try {
                    Files.walkFileTree(inputDir.toPath(), new LocalArtifactFileVisitor(Pattern.compile(artifactsRegex), localArtifactsDir.toPath()));
                } catch (Throwable e) {
                    log.warn("Error getting artifacts from localArtifactsDir {} ", localArtifactsDir, e);
                }
            });
        });

    }

    private class LocalArtifactFileVisitor extends SimpleFileVisitor<Path> {

        private Pattern artifactRegex;
        private Path localArtifactsDir;

        public LocalArtifactFileVisitor(Pattern artifactRegex, Path localArtifactsDir) {
            this.artifactRegex = artifactRegex;
            this.localArtifactsDir = localArtifactsDir;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (Files.exists(file) && artifactRegex.matcher(file.toString()).find()) {
                Files.copy(file, localArtifactsDir.resolve(file.getFileName()));
            }
            return super.visitFile(file, attrs);
        }
    }
}

