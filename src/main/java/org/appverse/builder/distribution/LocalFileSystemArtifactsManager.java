package org.appverse.builder.distribution;

import org.appverse.builder.domain.enumeration.DistributionChannelType;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.dto.DistributionChannelDTO;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by panthro on 11/01/16.
 */
public class LocalFileSystemArtifactsManager implements ArtifactsManager {

    public static final String ZIP = ".zip";
    private static Logger log = LoggerFactory.getLogger(LocalFileSystemArtifactsManager.class);

    public static final String FILESYSTEM_ROOT = "fs.root";


    private DistributionChannelDTO distributionChannel;

    private File fileSystemRoot;

    @Override
    public DistributionChannelType getSupportedDistributionChannelType() {
        return DistributionChannelType.FILESYSTEM;
    }

    @Override
    public void setDistributionChannel(DistributionChannelDTO distributionChannel) {
        if (distributionChannel.getType() == null || !distributionChannel.getType().equals(getSupportedDistributionChannelType())) {
            log.warn("Trying to setup with an unsupported distribution channel type {}", distributionChannel.getType());
            throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support Distribution Type " + distributionChannel.getType());
        }
        if (!isValidDistributionChannel(distributionChannel)) {
            log.warn("Invalid properties detected in distribution channel {}", distributionChannel);
            throw new IllegalArgumentException("Distribution Channel [" + distributionChannel.getId() + "] does not have the required properties set");
        }

        this.distributionChannel = distributionChannel;
        this.fileSystemRoot = new File(distributionChannel.getProperties().get(FILESYSTEM_ROOT));

    }

    private boolean isValidDistributionChannel(DistributionChannelDTO distributionChannel) {
        if (distributionChannel.getProperties() == null || distributionChannel.getProperties().isEmpty()) {
            log.warn("distribution channel has no properties set {}", distributionChannel);
            return false;
        }

        if (!distributionChannel.getProperties().containsKey(FILESYSTEM_ROOT)) {
            log.warn("distribution channel does not have a {} property set", FILESYSTEM_ROOT);
            return false;
        }

        File fileSystemRoot = new File(distributionChannel.getProperties().get(FILESYSTEM_ROOT));
        if (!fileSystemRoot.exists() || !fileSystemRoot.canWrite()) {
            log.info("FileSystem root {} does not exists or is not writable. Will try to create it.", fileSystemRoot);
            if (!fileSystemRoot.mkdirs()) {
                log.error("Could not create filesystem root {} ", fileSystemRoot);
                throw new InvalidParameterException("Invalid distribution channel fileSystemRoot " + fileSystemRoot);
            }
        }

        return true;
    }


    private File getBuildRequestPath(BuildRequestDTO buildRequestDTO) {
        File chainDirectory = new File(fileSystemRoot, buildRequestDTO.getChainId().toString());
        return new File(chainDirectory, buildRequestDTO.getId().toString());
    }

    @Override
    public DistributionChannelDTO getDistributionChannel() {
        return distributionChannel;
    }

    @Override
    public boolean distribute(Artifact artifact, BuildRequestDTO buildRequestDTO) {
        checkStatus();

        if (artifact.getUri() == null) {
            throw new IllegalArgumentException("Artifact URI may not be null");
        }

        File buildRequestPath = getBuildRequestPath(buildRequestDTO);
        if (!buildRequestPath.exists()) {
            if (!buildRequestPath.mkdirs()) {
                log.error("Cannot create build request directory on {}", buildRequestPath);
                return false;
            }
        }

        try {

            File artifactFile = new File(artifact.getUri());
            if (!artifactFile.isDirectory()) {
                FileUtils.moveFile(artifactFile, new File(buildRequestPath, artifact.getName()));
            } else {
                ZipFile zipFile = new ZipFile(artifactFile.getAbsolutePath() + ZIP);
                log.info("artifact {} is a directory, compressing to {}", artifact, zipFile.getFile());
                ZipParameters parameters = new ZipParameters();
                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
                parameters.setIncludeRootFolder(false);
                zipFile.addFolder(artifactFile, parameters);
                artifact.setUri(zipFile.getFile().toURI());
                artifact.setName(zipFile.getFile().getName());
                return distribute(artifact, buildRequestDTO);
            }

        } catch (IOException | ZipException e) {
            log.warn("Could not save artifact {} to the destination directory {} ", artifact, buildRequestPath, e);
            return false;
        }

        return true;

    }

    @Override
    public boolean distribute(List<Artifact> artifacts, BuildRequestDTO buildRequestDTO) {
        if (artifacts == null || buildRequestDTO == null) {
            throw new InvalidParameterException("artifacts and buildRequest cannot be null");
        }
        return artifacts.stream().map(file -> distribute(file, buildRequestDTO)).allMatch(Boolean::booleanValue);
    }

    @Override
    public List<Artifact> retrieve(BuildRequestDTO buildRequestDTO) {
        checkStatus();
        File buildRequestPath = getBuildRequestPath(buildRequestDTO);
        if (!buildRequestPath.exists()) {
            log.info("attempting to get artifacts for an non existent build request path {} ", buildRequestPath);
            return Collections.emptyList();
        }
        File[] files = buildRequestPath.listFiles();
        return files == null ? Collections.emptyList() : Stream.of(files).map(file -> new Artifact(file.toURI(), file.getName(), distributionChannel, file.length())).collect(Collectors.toList());
    }

    private void checkStatus() {
        if (distributionChannel == null) {
            throw new IllegalStateException("distribution channel is not set");
        }
    }

    @Override
    public Optional<Artifact> retrieve(BuildRequestDTO buildRequestDTO, String name) {
        checkStatus();
        return retrieve(buildRequestDTO)
            .stream()
            .filter(artifact -> name.equals(artifact.getName()))
            .findAny();
    }
}
