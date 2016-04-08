package org.appverse.builder.service;

import org.appverse.builder.domain.BuildAgent;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.Optional;

/**
 * Service Interface for managing BuildAgent.
 */
public interface BuildAgentService {

    String FILE_SEPARATOR = "file.separator";
    String WORK_DIR = "work.dir";

    /**
     * Save a buildAgent.
     *
     * @return the persisted entity
     */
    public BuildAgentDTO save(BuildAgentDTO buildAgentDTO);

    /**
     * get all the buildAgents.
     *
     * @return the list of entities
     */
    public Page<BuildAgent> findAll(Pageable pageable);

    /**
     * get the "id" buildAgent.
     *
     * @return the entity
     */
    public BuildAgentDTO findOne(Long id);

    /**
     * delete the "id" buildAgent.
     */
    public void delete(Long id);


    /**
     * Queue the given build request to be build by the less loaded capable agent
     *
     * @param buildRequestDTO
     */
    void queueForExecution(BuildAgentDTO buildAgentDTO, BuildRequestDTO buildRequestDTO);

    /**
     * Finds an available agent capable of building the request
     *
     * @param buildRequestDTO
     * @return
     */
    Optional<BuildAgentDTO> getAvailableBuildAgent(BuildRequestDTO buildRequestDTO);
    /**
     * Get the log of a running build request
     *
     * @param buildRequestDTO
     * @return
     */
    InputStream getRunningLogStream(BuildRequestDTO buildRequestDTO);

    /**
     * Should return a string representing where in the build agent the build request information should be stored
     *
     * @param buildAgent
     * @param buildRequest
     * @return
     */
    String getBuildAgentRemoteRequestDir(BuildAgentDTO buildAgent, BuildRequestDTO buildRequest);

    /**
     * @param buildAgentDTO
     * @param request
     * @return
     */
    String getBuildAgentRemoteRequestInputDir(BuildAgentDTO buildAgentDTO, BuildRequestDTO request);
}
