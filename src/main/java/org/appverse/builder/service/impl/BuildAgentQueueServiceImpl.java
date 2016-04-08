package org.appverse.builder.service.impl;

import org.appverse.builder.build.BuildAgentQueueController;
import org.appverse.builder.build.BuildExecutorWorker;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.service.BuildAgentQueueService;
import org.appverse.builder.service.BuildAgentService;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by panthro on 15/01/16.
 */
@Service
public class BuildAgentQueueServiceImpl implements BuildAgentQueueService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    private ApplicationContext context;

    private Set<BuildAgentQueueController> controllers = Collections.synchronizedSet(new HashSet<>());

    private ScheduledExecutorService executorService;

    //-- cannot be injected directly because of cyclic reference
    //@Inject
    private BuildAgentService buildAgentService;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @PostConstruct
    private void init() {
        executorService = Executors.newScheduledThreadPool(1, new CustomizableThreadFactory("BUILD-AGENT-WATCHER-"));
        executorService.scheduleWithFixedDelay(() -> {
            if (buildAgentService == null) {
                buildAgentService = context.getBean(BuildAgentService.class);
            }
            if (!controllers.isEmpty()) {
                log.info("Detecting IDLE build agents");
                /**
                 * This checks for idle agents that haven't received tasks and should be shutdown and discarded
                 */
                Set<BuildAgentQueueController> idleControllers = controllers.stream().filter(buildAgentQueueController -> {
                    long lastReceivedTaskTime = buildAgentQueueController.getLastReceivedTaskTime();
                    return buildAgentQueueController.getCurrentLoad() == 0 && DateTime.now().isAfter(lastReceivedTaskTime + appverseBuilderProperties.getAgent().getMaxIdleTimeInSeconds()); //is idle for over the max idle time
                }).collect(Collectors.toSet());

                log.debug("Detected {} idle agent controllers", idleControllers.size());

                controllers.removeAll(idleControllers); //first remove them from the controllers to make sure no new jobs are added
                idleControllers.forEach((buildAgentQueueController) -> {
                        log.info("Stopping IDLE controller {}", buildAgentQueueController);
                        buildAgentQueueController.stop(); //stop all
                    }
                );
                /**
                 * This checks if any build agent has been updated on the server and it' controller needs to be refreshed
                 */
                log.info("Detecting UPDATED build agents");
                Set<BuildAgentQueueController> updatedControllers = controllers.stream()
                    .filter(buildAgentQueueController -> !buildAgentQueueController.getBuildAgent().equals(buildAgentService.findOne(buildAgentQueueController.getBuildAgent().getId()))) //all controllers who their agent are different in the database
                    .collect(Collectors.toSet());
                controllers.removeAll(updatedControllers); //remove all so no new jobs are scheduled
                updatedControllers.forEach((buildAgentQueueController) -> {
                        log.info("Stopping UPDATED controller {}", buildAgentQueueController);
                        buildAgentQueueController.stop(); //stop all
                    }
                );

                controllers.removeIf(BuildAgentQueueController::isStopped); //remove from collection to cleanup and free space (go go go GC!)
                log.info("Finished cleaning up controllers");
            }

        }, appverseBuilderProperties.getAgent().getDefaultAgentRefreshTimeInSeconds(), appverseBuilderProperties.getAgent().getDefaultAgentRefreshTimeInSeconds(), TimeUnit.SECONDS);

    }

    @PreDestroy
    private void shutdown() {
        executorService.shutdownNow();
    }

    @Override
    public void queue(BuildAgentDTO buildAgentDTO, BuildRequestDTO buildRequestDTO) {
        getOrCreateBuildAgentQueueController(buildAgentDTO).submit(buildRequestDTO);
    }

    @Override
    public int getCurrentLoad(BuildAgentDTO agent) {
        return controllers.stream()
            .filter(buildAgentQueueController -> buildAgentQueueController.getBuildAgent().equals(agent))
            .findAny()
            .map(BuildAgentQueueController::getCurrentLoad)
            .orElse(0);
    }

    @Override
    public Optional<BuildExecutorWorker> findWorkerExecutingRequest(BuildRequestDTO buildRequestDTO) {
        return controllers.stream().map(BuildAgentQueueController::getWorkers)
            .filter(buildExecutorWorkers -> !buildExecutorWorkers.isEmpty())
            .map(buildExecutorWorkers -> buildExecutorWorkers.stream()
                .filter(BuildExecutorWorker::isExecuting)
                .filter(buildExecutorWorker -> buildRequestDTO.equals(buildExecutorWorker.getCurrentBuildRequest()))
                .findAny()
            ).filter(Optional::isPresent)
            .map(Optional::get)
            .findAny();
    }

    @Override
    public BuildAgentQueueController getOrCreateBuildAgentQueueController(BuildAgentDTO buildAgentDTO) {
        return controllers.stream()
            .filter(buildAgentQueueController -> buildAgentQueueController.getBuildAgent().equals(buildAgentDTO))
            .findAny()
            .orElseGet(() -> {
                BuildAgentQueueController controller = context.getBean(BuildAgentQueueController.class, buildAgentDTO);
                controllers.add(controller);
                return controller;
            });
    }

    @Override
    @Async
    public void stopAndRemoveControllerForAgent(BuildAgentDTO buildAgent) {
        Optional<BuildAgentQueueController> controllerOptional = controllers.stream().filter(buildAgentQueueController -> buildAgentQueueController.getBuildAgent().getId().equals(buildAgent.getId())).findAny();
        controllerOptional.ifPresent(buildAgentQueueController -> {
            //If the updated is present, remove and stop
            controllers.remove(buildAgentQueueController);
            buildAgentQueueController.stop();
        });
    }
}
