package org.appverse.builder.build;

import org.appverse.builder.Application;
import org.appverse.builder.domain.*;
import org.appverse.builder.domain.enumeration.AgentType;
import org.appverse.builder.repository.BuildAgentRepository;
import org.appverse.builder.repository.EnginePlatformRepository;
import org.appverse.builder.repository.EngineRepository;
import org.appverse.builder.security.SecurityTestUtils;
import org.appverse.builder.service.BuildChainService;
import org.appverse.builder.service.BuildRequestService;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.appverse.builder.domain.enumeration.BuildStatus.CANCELLED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by panthro on 17/01/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class BuildRequestServiceTest {


    private Logger log = LoggerFactory.getLogger(getClass());

    @Rule
    public Timeout globalTimeout = Timeout.builder().withTimeout(10, TimeUnit.MINUTES).withLookingForStuckThread(false).build();

    @Inject
    private EngineRepository engineRepository;

    @Inject
    private EnginePlatformRepository enginePlatformRepository;

    @Inject
    private BuildAgentRepository buildAgentRepository;

    @Inject
    private BuildChainService buildChainService;

    @Inject
    private BuildRequestService buildRequestService;

    @Inject
    private BuildRequestTestUtils buildRequestTestUtils;

    private Engine engine;
    private EnginePlatform enginePlatform;
    private BuildAgent buildAgent;

    @Rule
    public GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);


    @Before
    public void initTest() {
        SecurityTestUtils.loginAsRegularUser();
        engine = new Engine();
        engine.setName("native");
        engine.setEnabled(true);
        engine.setDescription("Native build engine");
        engine.setVersion("1.0");
        engineRepository.save(engine);

        enginePlatform = new EnginePlatform();
        enginePlatform.setEnabled(true);
        enginePlatform.setEngine(engine);
        enginePlatform.setName("android");
        enginePlatform.setVersion("23");
        enginePlatform.setImageName("java_android_23");
        enginePlatformRepository.save(enginePlatform);

        buildAgent = new BuildAgent();
        buildAgent.setEnabled(true);
        buildAgent.setName("localhost");
        buildAgent.setAgentType(AgentType.LOCAL);
        buildAgentRepository.save(buildAgent);

        greenMail.reset();

    }

    @After
    public void tearDown() {
        enginePlatformRepository.deleteAll();
        engineRepository.deleteAll();
        buildAgentRepository.deleteAll();
    }


    @Test
    public void noEngineVersionFoundTest() throws IOException, ZipException, InterruptedException {
        engine.setVersion(RandomStringUtils.randomAlphanumeric(10));
        engineRepository.save(engine);
        final BuildRequestDTO buildRequestDTO = createAndroidRequestAndSchedule();

        buildRequestTestUtils.waitFinish(buildRequestDTO);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getStatus()).isEqualTo(CANCELLED);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getMessage()).containsIgnoringCase("engine");

    }

    @Test
    public void noEngineFoundTest() throws IOException, ZipException, InterruptedException {
        engine.setName(RandomStringUtils.randomAlphanumeric(10));
        engineRepository.save(engine);
        final BuildRequestDTO buildRequestDTO = createAndroidRequestAndSchedule();

        buildRequestTestUtils.waitFinish(buildRequestDTO);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getStatus()).isEqualTo(CANCELLED);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getMessage()).containsIgnoringCase("engine");

        assertThat(greenMail.waitForIncomingEmail(2000, 1));

    }

    @Test
    public void missingRequiredEngineVariablesTest() throws IOException, ZipException, InterruptedException {
        EngineVariable engineVariable = new EngineVariable();
        engineVariable.setDescription("Engine variable required");
        engineVariable.setName(RandomStringUtils.randomAlphanumeric(10));
        engineVariable.setRequired(true);
        engineVariable.setEngine(engine);
        engine.getEngineVariables().add(engineVariable);
        engineRepository.save(engine);
        final BuildRequestDTO buildRequestDTO = createAndroidRequestAndSchedule();

        buildRequestTestUtils.waitFinish(buildRequestDTO);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getStatus()).isEqualTo(CANCELLED);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getMessage()).containsIgnoringCase("engine variable");

        assertThat(greenMail.waitForIncomingEmail(2000, 1));

    }

    @Test
    public void missingRequiredPlatformVariablesTest() throws IOException, ZipException, InterruptedException {
        EnginePlatformVariable platformVariable = new EnginePlatformVariable();
        platformVariable.setDescription("Platform variable required");
        platformVariable.setName(RandomStringUtils.randomAlphanumeric(10));
        platformVariable.setRequired(true);
        platformVariable.setEnginePlatform(enginePlatform);
        enginePlatform.getEnginePlatformVariables().add(platformVariable);
        enginePlatformRepository.save(enginePlatform);
        final BuildRequestDTO buildRequestDTO = createAndroidRequestAndSchedule();

        buildRequestTestUtils.waitFinish(buildRequestDTO);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getStatus()).isEqualTo(CANCELLED);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getMessage()).containsIgnoringCase("platform variable");

        assertThat(greenMail.waitForIncomingEmail(2000, 1));

    }


    @Test
    public void noEnginePlatformFoundTest() throws IOException, ZipException, InterruptedException {
        enginePlatform.setName(RandomStringUtils.randomAlphanumeric(10));
        enginePlatformRepository.save(enginePlatform);
        final BuildRequestDTO buildRequestDTO = createAndroidRequestAndSchedule();

        buildRequestTestUtils.waitFinish(buildRequestDTO);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getStatus()).isEqualTo(CANCELLED);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getMessage()).containsIgnoringCase("platform");


    }

    @Test
    public void noEnginePlatformVersionFoundTest() throws IOException, ZipException, InterruptedException {
        enginePlatform.setVersion(RandomStringUtils.randomAlphanumeric(10));
        enginePlatformRepository.save(enginePlatform);
        final BuildRequestDTO buildRequestDTO = createAndroidRequestAndSchedule();

        buildRequestTestUtils.waitFinish(buildRequestDTO);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getStatus()).isEqualTo(CANCELLED);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getMessage()).containsIgnoringCase("platform");

    }


    private BuildRequestDTO createAndroidRequestAndSchedule() throws IOException, ZipException {
        return buildRequestTestUtils.createAndSchedule("demo/native/android-hello-world");
    }


    @Test
    public void agentRequirementsUnmetTest() throws IOException, ZipException, InterruptedException {
        enginePlatform.getAgentRequirements().put("UNMET_REQ", "UNMET_REQ");
        enginePlatformRepository.save(enginePlatform);
        final BuildRequestDTO buildRequestDTO = createAndroidRequestAndSchedule();

        buildRequestTestUtils.waitFinish(buildRequestDTO);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getStatus()).isEqualTo(CANCELLED);

        assertThat(buildRequestService.findOne(buildRequestDTO.getId()).getMessage()).containsIgnoringCase("agent");

    }


}
