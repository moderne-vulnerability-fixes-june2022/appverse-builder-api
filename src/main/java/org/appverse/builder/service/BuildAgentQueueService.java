package org.appverse.builder.service;

import org.appverse.builder.build.BuildAgentQueueController;
import org.appverse.builder.build.BuildExecutorWorker;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.springframework.scheduling.annotation.Async;

import java.util.Optional;

/**
 * Created by panthro on 15/01/16.
 */
public interface BuildAgentQueueService {

    /**
     * Add the given build request to the build agent queue
     *
     * @param buildAgentDTO
     * @param buildRequestDTO
     */
    void queue(BuildAgentDTO buildAgentDTO, BuildRequestDTO buildRequestDTO);

    /**
     * get the build agent current load, it's an int that represents the amount of tasks being executed plus the current queue
     * running requests + queued requests
     *
     * @param agent
     * @return
     */
    int getCurrentLoad(BuildAgentDTO agent);

    Optional<BuildExecutorWorker> findWorkerExecutingRequest(BuildRequestDTO buildRequestDTO);


    BuildAgentQueueController getOrCreateBuildAgentQueueController(BuildAgentDTO buildAgentDTO);

    /**
     * This method should asynchronously remove any controller associated with this build agent and stop it.
     *
     * @param buildAgent
     */
    @Async
    void stopAndRemoveControllerForAgent(BuildAgentDTO buildAgent);
}
