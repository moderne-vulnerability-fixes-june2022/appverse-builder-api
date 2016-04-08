package org.appverse.builder.service;

import org.appverse.builder.domain.BuildRequest;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Service Interface for managing BuildRequest.
 */
public interface BuildRequestService {

    /**
     * Save a buildRequest.
     *
     * @return the persisted entity
     */
    public BuildRequestDTO save(BuildRequestDTO buildRequestDTO);

    /**
     * get all the buildRequests.
     *
     * @return the list of entities
     */
    public Page<BuildRequest> findAll(Pageable pageable);

    /**
     * get the "id" buildRequest.
     *
     * @return the entity
     */
    public BuildRequestDTO findOne(Long id);

    /**
     * delete the "id" buildRequest.
     */
    public void delete(Long id);

    /**
     * Schedule a build request to start
     *
     * @param buildRequestDTO
     */
    void schedule(BuildRequestDTO buildRequestDTO);

    /**
     * get the logs of a given build request
     *
     * @return optional with the log inputstream
     */
    public Optional<InputStream> getLogs(Long id);

    /**
     * @param id
     * @return
     */
    Optional<File> getCompressedArtifacts(Long id) throws IOException;

    /**
     *
     * @param id
     * @param distributionId
     * @return
     */
    Optional<File> getCompressedArtifacts(Long id, Long distributionId);

    /**
     * @param currentBuildRequest
     * @return
     */
    File getBuildRequestRootDir(BuildRequestDTO currentBuildRequest);

    Page<BuildRequest> findByCurrentUser(Pageable pageable);

    /**
     * Check if the current logged user has access to the given build request
     *
     * @param buildRequestDTO
     * @return
     */
    boolean hasAccess(BuildRequestDTO buildRequestDTO);
}
