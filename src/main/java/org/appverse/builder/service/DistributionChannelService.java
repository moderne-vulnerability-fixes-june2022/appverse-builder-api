package org.appverse.builder.service;

import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.domain.DistributionChannel;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.dto.DistributionChannelDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing DistributionChannel.
 */
public interface DistributionChannelService {

    /**
     * Save a distributionChannel.
     *
     * @return the persisted entity
     */
    public DistributionChannelDTO save(DistributionChannelDTO distributionChannelDTO);

    /**
     * get all the distributionChannels.
     *
     * @return the list of entities
     */
    public Page<DistributionChannel> findAll(Pageable pageable);

    /**
     * get the "id" distributionChannel.
     *
     * @return the entity
     */
    public DistributionChannelDTO findOne(Long id);

    /**
     * delete the "id" distributionChannel.
     */
    public void delete(Long id);


    /**
     * Save artifacts for a given request
     *
     * @param buildRequest
     * @param artifacts
     */
    void distributeArtifacts(BuildRequestDTO buildRequest, List<Artifact> artifacts);

    /**
     * Get the artifacts of a given build request.
     * This is a overload, @see getRequestArtifacts
     * @param buildRequest
     * @return
     */
    List<Artifact> getRequestArtifacts(BuildRequestDTO buildRequest);

    /**
     * Get the artifacts of a given build request converting local artifacts URI to a downloadable public uri
     *
     * @param buildRequestDTO
     * @param convertLocalToDownloadable
     * @return
     */
    List<Artifact> getRequestArtifacts(BuildRequestDTO buildRequestDTO, boolean convertLocalToDownloadable);


    /**
     * Distribute the log file to the distribution channels
     *
     * @param currentBuildRequest
     * @param buildLogFile
     */
    void distributeLog(BuildRequestDTO currentBuildRequest, File buildLogFile);


    /**
     * Retrieve the build request logs as InputStream
     *
     * @param buildRequest
     * @return
     */
    Optional<InputStream> getLogAsStream(BuildRequestDTO buildRequest);

    /**
     * Retrieve the build artifact by name and distribution channel id
     *
     * @param buildRequestDTO
     * @param distributionId
     * @param name
     * @return
     */
    Optional<Artifact> getBuildArtifactByName(BuildRequestDTO buildRequestDTO, Long distributionId, String name);
}
