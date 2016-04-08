package org.appverse.builder.build;

import com.icegreen.greenmail.util.ServerSetupTest;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.appverse.builder.Application;
import org.appverse.builder.build.comand.docker.DockerBuildCommandBuilder;
import org.appverse.builder.build.comand.dockgrant.DockgrantBuildCommandBuilder;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.distribution.LocalFileSystemArtifactsManager;
import org.appverse.builder.domain.*;
import org.appverse.builder.domain.enumeration.AgentType;
import org.appverse.builder.domain.enumeration.DistributionChannelType;
import org.appverse.builder.domain.enumeration.ImageType;
import org.appverse.builder.domain.enumeration.NotificationChannelType;
import org.appverse.builder.notification.email.EmailNotificationSender;
import org.appverse.builder.repository.*;
import org.appverse.builder.security.SecurityTestUtils;
import org.appverse.builder.service.BuildAgentService;
import org.appverse.builder.service.BuildChainService;
import org.appverse.builder.service.BuildRequestService;
import org.appverse.builder.service.DistributionChannelService;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.appverse.builder.domain.enumeration.BuildStatus.*;
import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by panthro on 23/02/16.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class BuildIntegrationTest {

    private static final String TEST_TXT = "test.txt";
    public static final String ECHOED_MESSAGE = RandomStringUtils.randomAlphabetic(20);
    public static final String UBUNTU_SCRIPT = "echo " + ECHOED_MESSAGE + " > " + TEST_TXT + " && cat " + TEST_TXT;
    private Logger log = LoggerFactory.getLogger(this.getClass());

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

    @Inject
    private DistributionChannelRepository distributionChannelRepository;

    @Inject
    private DistributionChannelService distributionChannelService;

    private DistributionChannel distributionChannel;


    @Inject
    private NotificationChannelRepository notificationChannelRepository;

    private NotificationChannel notificationChannel;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder(FileUtils.getUserDirectory());

    @Rule
    public TestRule imageTypeRule = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            if (is(BuildTest.TestType.DOCKER_TEST) && !isCommandAvailable(new String[]{"docker", "info"})) {
                String bannerLocation = "/banners/docker-not-running.txt";
                logBanner(bannerLocation);
            } else if (is(BuildTest.TestType.DOCKGRANT_TEST) && !isCommandAvailable(new String[]{"dockgrant", "--help"})) {
                String bannerLocation = "/banners/dockgrant-not-running.txt";
                logBanner(bannerLocation);
            } else {
                base.evaluate();
            }
        }

        void logBanner(String bannerLocation) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(bannerLocation)))) {
                reader.lines().forEachOrdered(log::warn);
            } catch (Throwable t) {
                log.warn("Could not open banner {}", bannerLocation, t);
            }
        }

        private boolean isCommandAvailable(String[] command) throws InterruptedException {
            try {
                Process exec = Runtime.getRuntime().exec(command);
                exec.waitFor();
                new BufferedReader(new InputStreamReader(exec.getInputStream())).lines().forEachOrdered(line -> log.info(line));
                new BufferedReader(new InputStreamReader(exec.getErrorStream())).lines().forEachOrdered(line -> log.info(line));
                return exec.exitValue() == 0;
            } catch (IOException e) {
                log.warn("Error checking command {} ", command, e);
                return false;
            }
        }

        private boolean is(BuildTest.TestType testType) {
            return description.getAnnotations().stream().anyMatch(annotation -> annotation instanceof BuildTest && ((BuildTest) annotation).testType().equals(testType));
        }


    };

    @PostConstruct
    private void setup() {
        notificationChannel = new NotificationChannel();
        notificationChannel.setEnabled(true);
        notificationChannel.setType(NotificationChannelType.EMAIL);
        notificationChannel.setName("localhost");
        notificationChannel.setDescription("default notification channel");
        Map<String, String> properties = new HashMap<>();
        properties.put(EmailNotificationSender.SMTP_HOST, "localhost");
        properties.put(EmailNotificationSender.SMTP_PORT, String.valueOf(ServerSetupTest.SMTP.getPort()));
        notificationChannel.setProperties(properties);
        notificationChannelRepository.save(notificationChannel);
    }

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
        enginePlatform.setName("ubuntu");
        enginePlatform.setVersion("14");
        enginePlatform.setImageName("ubuntu:latest");
        enginePlatformRepository.save(enginePlatform);

        buildAgent = new BuildAgent();
        buildAgent.setEnabled(true);
        buildAgent.setName("localhost");
        buildAgent.setAgentType(AgentType.LOCAL);
        buildAgentRepository.save(buildAgent);

    }

    @After
    public void tearDown() {
        enginePlatformRepository.deleteAll();
        engineRepository.deleteAll();
        buildAgentRepository.deleteAll();
    }


    private void setupLocalDistributionChannel() throws IOException {
        distributionChannel = new DistributionChannel();
        distributionChannel.setType(DistributionChannelType.FILESYSTEM);
        distributionChannel.setEnabled(true);
        distributionChannel.setName("localhost");
        distributionChannel.getProperties().put(LocalFileSystemArtifactsManager.FILESYSTEM_ROOT, tempDir.newFolder("artifacts").getAbsolutePath());
        distributionChannelRepository.save(distributionChannel);
    }

    private void setupLocalBuildAgent() throws IOException {
        buildAgent.getProperties().put(BuildAgentService.WORK_DIR, tempDir.newFolder("build").getAbsolutePath());
        buildAgent.getProperties().put(DockerBuildCommandBuilder.DOCKER_AGENT_KEY, "true");
        buildAgent.getProperties().put(DockgrantBuildCommandBuilder.DOCKGRANT_AGENT_KEY, "true");
        buildAgent.setAgentType(AgentType.LOCAL);
        buildAgentRepository.save(buildAgent);
    }


    private BuildRequestDTO createUbuntuRequestAndSchedule() throws IOException, ZipException {
        return buildRequestTestUtils.createAndSchedule("demo/native/ubuntu");
    }


    @Test
    @BuildTest(testType = BuildTest.TestType.DOCKER_TEST)
    public void dockerUbuntuSampleTest() throws IOException, ZipException, InterruptedException {

        setupLocalBuildAgent();

        setupLocalDistributionChannel();
        initiateUbuntuEnginePlatform();
        enginePlatformRepository.save(enginePlatform);

        BuildRequestDTO buildRequest = createUbuntuRequestAndSchedule();

        assertThat(buildRequestService.findOne(buildRequest.getId()).isFinished()).isFalse();

        buildRequestTestUtils.waitStart(buildRequest);

        log.info("buildRequest has started: {}", buildRequestService.findOne(buildRequest.getId()));

        Assertions.assertThat(buildRequestService.findOne(buildRequest.getId()).getStatus()).isNotIn(FAILED, CANCELLED);
        final Optional<InputStream> logs = buildRequestService.getLogs(buildRequest.getId());
        assertThat(logs).isPresent();

        assertLogsContainsEchoedMessage(logs.get());

        buildRequestTestUtils.waitFinish(buildRequest);
        Assertions.assertThat(buildRequestService.findOne(buildRequest.getId()).getStatus()).isEqualTo(SUCCESSFUL);
        List<Artifact> buildArtifacts = distributionChannelService.getRequestArtifacts(buildRequest);
        Assertions.assertThat(buildArtifacts).isNotEmpty();
        //Assertions.assertThat(buildArtifacts.stream().map(Artifact::getName).collect(Collectors.toList())).contains(TEST_TXT);

    }

    public void assertLogsContainsEchoedMessage(InputStream logs) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(logs));
        List<String> lines = new ArrayList<String>() {
            @Override
            public boolean add(String o) {
                log.debug("BUILD-LOG-LINE: {}", o);
                return super.add(o);
            }
        };
        reader.lines().forEachOrdered(lines::add);
        assertThat(lines.stream().anyMatch(line -> line.contains(ECHOED_MESSAGE))).isTrue();
    }

    @Test
    @BuildTest(testType = BuildTest.TestType.DOCKGRANT_TEST)
    public void dockgrantUbuntuSampleTest() throws IOException, ZipException, InterruptedException {


        setupLocalBuildAgent();

        setupLocalDistributionChannel();
        initiateUbuntuEnginePlatform();
        enginePlatform.setImageName("hashicorp/precise64");
        enginePlatform.setImageType(ImageType.VAGRANT);
        enginePlatformRepository.save(enginePlatform);

        BuildRequestDTO buildRequest = createUbuntuRequestAndSchedule();

        assertThat(buildRequestService.findOne(buildRequest.getId()).isFinished()).isFalse();

        buildRequestTestUtils.waitStart(buildRequest);

        log.info("buildRequest has started: {}", buildRequestService.findOne(buildRequest.getId()));

        Assertions.assertThat(buildRequestService.findOne(buildRequest.getId()).getStatus()).isNotIn(FAILED, CANCELLED);
        final Optional<InputStream> logs = buildRequestService.getLogs(buildRequest.getId());
        assertThat(logs).isPresent();

        assertLogsContainsEchoedMessage(logs.get());

        buildRequestTestUtils.waitFinish(buildRequest);
        Assertions.assertThat(buildRequestService.findOne(buildRequest.getId()).getStatus()).isEqualTo(SUCCESSFUL);
        List<Artifact> buildArtifacts = distributionChannelService.getRequestArtifacts(buildRequest);
        Assertions.assertThat(buildArtifacts).isNotEmpty();
        //Assertions.assertThat(buildArtifacts.stream().map(Artifact::getName).collect(Collectors.toList())).contains(TEST_TXT);
    }

    public void initiateUbuntuEnginePlatform() {
        enginePlatform.setName("ubuntu");
        enginePlatform.setVersion("14");
        enginePlatform.setImageName("ubuntu:latest");
        enginePlatform.getEnginePlatformVariables().add(new EnginePlatformVariable("script", UBUNTU_SCRIPT, false, enginePlatform));
        enginePlatform.getEnginePlatformVariables().add(new EnginePlatformVariable("artifactRegex", "^.*\\.txt$", false, enginePlatform));
    }
}
