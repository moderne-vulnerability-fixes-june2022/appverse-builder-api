package org.appverse.builder.service;

import org.appverse.builder.Application;
import org.appverse.builder.build.BuildAgentQueueController;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.domain.enumeration.AgentType;
import org.appverse.builder.repository.BuildAgentRepository;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.assertj.core.api.StrictAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by panthro on 10/03/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class BuildAgentQueueServiceTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String DEFAULT_AGENT_NAME = "LOCAL-TEST-BA";
    public static final String UPDATED_LOCAL_TEST_NAME = "UPDATED-LOCAL-TEST-NAME";
    public static final String NEW_PROP = "NEW-PROP";
    public static final String NEW_PROP_VALUE = "NEW-PROP-VALUE";
    public static final String INITIAL_PROP_VALUE = "initial-prop-value";
    public static final String INITIAL_PROP_NAME = "initial-prop-name";
    public static final String UPDATED_PROP_VALUE = "updated-prop-value";
    private BuildAgentDTO buildAgent;

    @Inject
    private BuildAgentRepository buildAgentRepository;

    @Inject
    private BuildAgentQueueService queueService;

    @Inject
    private BuildAgentService buildAgentService;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;


    @Before
    public void setUp() throws Exception {
        buildAgent = new BuildAgentDTO();
        buildAgent.setAgentType(AgentType.LOCAL);
        buildAgent.setEnabled(true);
        buildAgent.setName(DEFAULT_AGENT_NAME);
        buildAgent.getProperties().put(INITIAL_PROP_NAME, INITIAL_PROP_VALUE);

    }

    @After
    public void tearDown() throws Exception {

    }


    @Test(timeout = 5 * 1000 * 60)
    //2 minutes max
    @Transactional
    public void testBuildAgentQueueControllerIsRefreshed() throws InterruptedException, ExecutionException, TimeoutException {
        buildAgent = buildAgentService.save(buildAgent);

        assertThat(buildAgent.getId()).isNotNull();

        long defaultAgentRefreshTimeInSeconds = appverseBuilderProperties.getAgent().getDefaultAgentRefreshTimeInSeconds();
        appverseBuilderProperties.getAgent().setDefaultAgentRefreshTimeInSeconds(1);

        BuildAgentQueueController buildAgentQueueController = queueService.getOrCreateBuildAgentQueueController(buildAgent);

        StrictAssertions.assertThat(buildAgentQueueController.getBuildAgent().getId()).isEqualTo(buildAgent.getId());

        BuildAgentDTO updatedBuildAgent = buildAgentService.findOne(buildAgent.getId());
        updatedBuildAgent.setName(UPDATED_LOCAL_TEST_NAME);
        updatedBuildAgent.getProperties().put(NEW_PROP, NEW_PROP_VALUE);
        updatedBuildAgent.getProperties().put(INITIAL_PROP_NAME, UPDATED_PROP_VALUE);
        buildAgentService.save(updatedBuildAgent);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            boolean updated = false;
            while (!Thread.interrupted() && !updated) {
                try {
                    Thread.sleep(1000);
                    BuildAgentQueueController controller = queueService.getOrCreateBuildAgentQueueController(updatedBuildAgent);
                    boolean sameAgent = !buildAgentQueueController.equals(controller);
                    boolean name = controller.getBuildAgent().getName().equals(UPDATED_LOCAL_TEST_NAME);
                    boolean initialProp = Optional.ofNullable(controller.getBuildAgent().getProperties().get(INITIAL_PROP_NAME)).map(UPDATED_PROP_VALUE::equals).orElse(false);
                    boolean newProp = Optional.ofNullable(controller.getBuildAgent().getProperties().get(NEW_PROP)).map(NEW_PROP_VALUE::equals).orElse(false);
                    updated = name && initialProp && newProp && sameAgent;
                    if (updated) {
                        future.complete(true);
                    }
                } catch (InterruptedException e) {
                    log.warn("Interrupted");
                } finally {
                    log.info("Agent got updated: {}", updated);
                }
            }
        });

        executorService.shutdown();

        assertThat(future.get(defaultAgentRefreshTimeInSeconds, TimeUnit.SECONDS)).isTrue();

    }
}
