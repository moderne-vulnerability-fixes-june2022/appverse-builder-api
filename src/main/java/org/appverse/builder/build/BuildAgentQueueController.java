package org.appverse.builder.build;

import org.appverse.builder.build.ssh.SSHBuildExecutorWorker;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


/**
 * Created by panthro on 17/01/16.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BuildAgentQueueController {

    private static final String QUEUE_SIZE = "queue.size";

    private static final String MAX_CONCURRENT_BUILDS = "max.concurrent.builds";
    private static final String TERMINATION_TIMEOUT = "termination.timeout";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private ApplicationContext context;

    private BuildAgentDTO buildAgent;

    private ArrayBlockingQueue<BuildRequestDTO> requests;

    private ExecutorService executorService;

    private final Set<BuildExecutorWorker> workers = new HashSet<>();
    private long lastReceivedTaskTime;


    public BuildAgentQueueController(BuildAgentDTO buildAgent) {
        this.buildAgent = buildAgent;
    }

    @PostConstruct
    private void init() {
        lastReceivedTaskTime = DateTimeUtils.currentTimeMillis();
        this.requests = new ArrayBlockingQueue<>(getAgentQueueSize(), true);
        this.executorService = Executors.newFixedThreadPool(getAgentMaxConcurrentBuilds(), new CustomizableThreadFactory("agent-" + buildAgent.getId().toString() + "-" + buildAgent.getName()));
        IntStream.range(0, getAgentMaxConcurrentBuilds()).forEach(value -> workers.add(createBuildExecutorWorker()));
        workers.forEach(buildExecutorWorker -> executorService.execute(buildExecutorWorker));
    }

    private BuildExecutorWorker createBuildExecutorWorker() {
        switch (buildAgent.getAgentType()) {
            case SSH:
                return context.getBean(SSHBuildExecutorWorker.class, BuildAgentQueueController.this);
            case LOCAL:
                return context.getBean(LocalBuildExecutorWorker.class, BuildAgentQueueController.this);
            default:
                throw new RuntimeException("Agent type " + buildAgent.getAgentType() + " not supported");
        }

    }

    @PreDestroy
    public void stop() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(getAgentTerminationTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.info("Failed to stop controller for agent {}, forcing shutdown", buildAgent.getName());
            executorService.shutdownNow();
        }
        log.info("Controller for agent {} has stopped successfully", buildAgent);
    }


    private int getAgentTerminationTimeout() {
        return buildAgent.getProperties().containsKey(TERMINATION_TIMEOUT) ? Integer.valueOf(buildAgent.getProperties().get(TERMINATION_TIMEOUT)) : appverseBuilderProperties.getAgent().getDefaultTerminationTimeout();
    }

    private int getAgentMaxConcurrentBuilds() {
        return buildAgent.getProperties().containsKey(MAX_CONCURRENT_BUILDS) ? Integer.valueOf(buildAgent.getProperties().get(MAX_CONCURRENT_BUILDS)) : appverseBuilderProperties.getAgent().getDefaultConcurrentBuilds();
    }

    private int getAgentQueueSize() {
        return buildAgent.getProperties().containsKey(QUEUE_SIZE) ? Integer.valueOf(buildAgent.getProperties().get(QUEUE_SIZE)) : appverseBuilderProperties.getAgent().getDefaultQueueSize();
    }

    public BuildAgentDTO getBuildAgent() {
        return buildAgent;
    }

    public void setBuildAgent(BuildAgentDTO buildAgent) {
        this.buildAgent = buildAgent;
    }

    public void submit(BuildRequestDTO buildRequestDTO) {
        try {
            log.debug("Adding request to the controller {}", buildRequestDTO);
            requests.put(buildRequestDTO);
            lastReceivedTaskTime = DateTimeUtils.currentTimeMillis();
        } catch (InterruptedException e) {
            log.warn("Got interruption signal adding request to queue {}", buildRequestDTO);
        }
    }


    public int getCurrentLoad() {
        return ((Long) workers.stream().filter(BuildExecutorWorker::isExecuting).count()).intValue();
    }

    public BuildRequestDTO getNext() throws InterruptedException {
        return requests.take();
    }

    public boolean isRunning() {
        return !executorService.isShutdown();
    }

    public boolean isStopped() {
        return !isRunning();
    }


    public Set<BuildExecutorWorker> getWorkers() {
        return workers;
    }

    public long getLastReceivedTaskTime() {
        return lastReceivedTaskTime;
    }

    public void setLastReceivedTaskTime(long lastReceivedTaskTime) {
        this.lastReceivedTaskTime = lastReceivedTaskTime;
    }
}
