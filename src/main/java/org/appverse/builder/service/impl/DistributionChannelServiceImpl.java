package org.appverse.builder.service.impl;

import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.distribution.ArtifactsManager;
import org.appverse.builder.distribution.LocalFileSystemArtifactsManager;
import org.appverse.builder.domain.DistributionChannel;
import org.appverse.builder.repository.DistributionChannelRepository;
import org.appverse.builder.service.DistributionChannelService;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.dto.DistributionChannelDTO;
import org.appverse.builder.web.rest.mapper.DistributionChannelMapper;
import org.appverse.builder.web.rest.util.ArtifactDownloadUrlCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service Implementation for managing DistributionChannel.
 */
@Service
@Transactional
public class DistributionChannelServiceImpl implements DistributionChannelService {

    private final Logger log = LoggerFactory.getLogger(DistributionChannelServiceImpl.class);

    @Inject
    private DistributionChannelRepository distributionChannelRepository;

    @Inject
    private DistributionChannelMapper distributionChannelMapper;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private ArtifactDownloadUrlCreator artifactDownloadUrlCreator;

    /**
     * Save a distributionChannel.
     *
     * @return the persisted entity
     */
    public DistributionChannelDTO save(DistributionChannelDTO distributionChannelDTO) {
        log.debug("Request to save DistributionChannel : {}", distributionChannelDTO);
        DistributionChannel distributionChannel = distributionChannelMapper.distributionChannelDTOToDistributionChannel(distributionChannelDTO);
        distributionChannel = distributionChannelRepository.save(distributionChannel);
        DistributionChannelDTO result = distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel);
        return result;
    }

    /**
     * get all the distributionChannels.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<DistributionChannel> findAll(Pageable pageable) {
        log.debug("Request to get all DistributionChannels");
        Page<DistributionChannel> result = distributionChannelRepository.findAll(pageable);
        return result;
    }

    /**
     * get one distributionChannel by id.
     *
     * @return the entity
     */
    @Transactional(readOnly = true)
    public DistributionChannelDTO findOne(Long id) {
        log.debug("Request to get DistributionChannel : {}", id);
        DistributionChannel distributionChannel = distributionChannelRepository.findOne(id);
        DistributionChannelDTO distributionChannelDTO = distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel);
        return distributionChannelDTO;
    }

    /**
     * delete the  distributionChannel by id.
     */
    public void delete(Long id) {
        log.debug("Request to delete DistributionChannel : {}", id);
        distributionChannelRepository.delete(id);
    }


    @Override
    public void distributeArtifacts(BuildRequestDTO buildRequest, List<Artifact> artifacts) {
        distributionChannelRepository.findByEnabledTrue().stream().forEach(distributionChannel -> {
            try {
                getArtifactsManagerByDistributionChannel(distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel)).ifPresent(artifactsManager -> {
                    if (!artifactsManager.distribute(artifacts, buildRequest)) {
                        log.info("Not all artifacts could be saved to the artifactsManager {}", artifactsManager);
                    }
                });
            } catch (Exception e) {
                log.warn("Exception saving artifacts into distribution channel {} ", distributionChannel, e);
            }
        });
    }

    private Optional<ArtifactsManager> getArtifactsManagerByDistributionChannel(DistributionChannelDTO distributionChannel) {
        switch (distributionChannel.getType()) {
            case FILESYSTEM:
                ArtifactsManager artifactsManager = new LocalFileSystemArtifactsManager();
                artifactsManager.setDistributionChannel(distributionChannel);
                return Optional.of(artifactsManager);
            default:
                log.info("Distribution channel type {} is not supported yet", distributionChannel.getType());
                return Optional.empty();
        }
    }

    @Override
    public List<Artifact> getRequestArtifacts(BuildRequestDTO buildRequest, boolean convertLocalFiles) {
        final List<Artifact> artifacts = new ArrayList<>();
        distributionChannelRepository.findByEnabledTrue().forEach(distributionChannel -> {
            getArtifactsManagerByDistributionChannel(distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel)).ifPresent(artifactsManager -> artifacts.addAll(artifactsManager.retrieve(buildRequest)));
        });

        if (convertLocalFiles) {
            //Cleanup uris for local artifacts to avoid "file://" exposure
            artifacts.forEach(artifact -> {
                if (artifact.isLocal()) {
                    artifact.setUri(URI.create(artifactDownloadUrlCreator.createArtifactDownloadPath(buildRequest, artifact)));
                }
            });
        }
        return artifacts;
    }

    @Override
    public List<Artifact> getRequestArtifacts(BuildRequestDTO buildRequest) {
        return getRequestArtifacts(buildRequest, false);
    }

    @Override
    public void distributeLog(BuildRequestDTO buildRequest, File buildLogFile) {
        distributeArtifacts(buildRequest, Stream.of(buildLogFile).map(file ->
            new Artifact(file.toURI(), appverseBuilderProperties.getBuild().getLogFileName(), file.length())
        ).collect(Collectors.toList()));
    }

    @Override
    public Optional<InputStream> getLogAsStream(BuildRequestDTO buildRequest) {
        return getRequestArtifacts(buildRequest).stream()
            .filter(artifact -> artifact.getName().equals(appverseBuilderProperties.getBuild().getLogFileName()))
            .map(artifact -> {
                if (artifact.isLocal()) {
                    try {
                        return new FileInputStream(new File(artifact.getUri()));
                    } catch (FileNotFoundException e) {
                        log.warn("Could not find the log file located at: {}", artifact.getUri());
                    }
                } else {
                    try {
                        return artifact.getUri().toURL().openStream();
                    } catch (IOException e) {
                        log.warn("Could not open stream of artifact located at: {}", artifact.getUri());
                    }
                }
                return null;
            }).findAny();
    }

    @Override
    public Optional<Artifact> getBuildArtifactByName(BuildRequestDTO buildRequestDTO, Long distributionId, String name) {
        return Optional.ofNullable(distributionChannelRepository.findOne(distributionId))
            .map(distributionChannel -> {
                DistributionChannelDTO distributionChannelDTO = distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel);
                return getArtifactsManagerByDistributionChannel(distributionChannelDTO)
                    .flatMap(artifactsManager -> artifactsManager.retrieve(buildRequestDTO, name))
                    .orElse(null);
            });
    }
}
