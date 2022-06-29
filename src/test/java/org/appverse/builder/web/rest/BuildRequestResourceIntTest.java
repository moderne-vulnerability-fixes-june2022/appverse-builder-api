package org.appverse.builder.web.rest;

import org.apache.commons.io.FileUtils;
import org.appverse.builder.Application;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.distribution.LocalFileSystemArtifactsManager;
import org.appverse.builder.domain.BuildChain;
import org.appverse.builder.domain.BuildRequest;
import org.appverse.builder.domain.DistributionChannel;
import org.appverse.builder.domain.enumeration.BuildStatus;
import org.appverse.builder.repository.BuildChainRepository;
import org.appverse.builder.repository.BuildRequestRepository;
import org.appverse.builder.repository.DistributionChannelRepository;
import org.appverse.builder.security.SecurityTestUtils;
import org.appverse.builder.service.BuildRequestService;
import org.appverse.builder.service.DistributionChannelService;
import org.appverse.builder.service.UserService;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.mapper.BuildRequestMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.appverse.builder.domain.enumeration.BuildStatus.*;
import static org.appverse.builder.domain.enumeration.DistributionChannelType.FILESYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the BuildRequestResource REST controller.
 *
 * @see BuildRequestResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class BuildRequestResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("Z"));

    private static final String DEFAULT_ENGINE = "AAAAA";
    private static final String UPDATED_ENGINE = "BBBBB";
    private static final String DEFAULT_PLATFORM = "AAAAA";
    private static final String UPDATED_PLATFORM = "BBBBB";
    private static final String DEFAULT_FLAVOR = "AAAAA";
    private static final String UPDATED_FLAVOR = "BBBBB";


    private static final BuildStatus DEFAULT_STATUS = BuildStatus.QUEUED;
    private static final BuildStatus UPDATED_STATUS = RUNNING;

    private static final ZonedDateTime DEFAULT_START_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_START_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_START_TIME_STR = dateTimeFormatter.format(DEFAULT_START_TIME);

    private static final ZonedDateTime DEFAULT_END_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_END_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_END_TIME_STR = dateTimeFormatter.format(DEFAULT_END_TIME);
    private static final String DEFAULT_MESSAGE = "AAAAA";
    private static final String UPDATED_MESSAGE = "BBBBB";
    private static final String USER_LOGIN = "admin";
    private static final String USER_PASSWORD = "admin";

    @Inject
    private BuildRequestRepository buildRequestRepository;

    @Inject
    private BuildChainRepository buildChainRepository;

    @Inject
    private BuildRequestMapper buildRequestMapper;

    @Inject
    private BuildRequestService buildRequestService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private DistributionChannelRepository distributionChannelRepository;

    @Inject
    private DistributionChannelService distributionChannelService;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private UserService userService;

    private MockMvc restBuildRequestMockMvc;

    private BuildRequest buildRequest;

    private BuildChain buildChain;

    private File tempDistributionChannelRoot;

    @PostConstruct
    public void setup() {
        SecurityTestUtils.loginAs(USER_LOGIN, USER_PASSWORD);
        MockitoAnnotations.initMocks(this);
        BuildRequestResource buildRequestResource = new BuildRequestResource();
        ReflectionTestUtils.setField(buildRequestResource, "buildRequestService", buildRequestService);
        ReflectionTestUtils.setField(buildRequestResource, "buildRequestMapper", buildRequestMapper);
        ReflectionTestUtils.setField(buildRequestResource, "distributionChannelService", distributionChannelService);
        this.restBuildRequestMockMvc = MockMvcBuilders.standaloneSetup(buildRequestResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(new ResourceHttpMessageConverter(), jacksonMessageConverter).build();
    }

    @Before
    public void initTest() throws IOException {

        //Setup distribution channel
        tempDistributionChannelRoot = Files.createTempDirectory("temp" + Long.toString(System.nanoTime())).toFile();


        DistributionChannel distributionChannel = new DistributionChannel();
        distributionChannel.setType(FILESYSTEM);
        distributionChannel.setEnabled(true);
        distributionChannel.setName("Local Test Filesystem");
        distributionChannel.getProperties().put(LocalFileSystemArtifactsManager.FILESYSTEM_ROOT, tempDistributionChannelRoot.getAbsolutePath());
        distributionChannelRepository.save(distributionChannel);

        buildChain = new BuildChain();
        buildChain.setRequester(userService.getUserWithAuthorities());
        buildChain.setCreatedDate(ZonedDateTime.now());
        buildChainRepository.saveAndFlush(buildChain);

        buildRequest = new BuildRequest();
        buildRequest.setEngine(DEFAULT_ENGINE);
        buildRequest.setPlatform(DEFAULT_PLATFORM);
        buildRequest.setFlavor(DEFAULT_FLAVOR);
        buildRequest.setStatus(DEFAULT_STATUS);
        buildRequest.setStartTime(DEFAULT_START_TIME);
        buildRequest.setEndTime(DEFAULT_END_TIME);
        buildRequest.setMessage(DEFAULT_MESSAGE);
        buildRequest.setChain(buildChain);
    }

    @After
    public void tearDown() throws IOException {
        if (tempDistributionChannelRoot != null && tempDistributionChannelRoot.exists()) {
            FileUtils.deleteDirectory(tempDistributionChannelRoot);
        }
    }

    @Test
    @Transactional
    public void createBuildRequest() throws Exception {
        int databaseSizeBeforeCreate = buildRequestRepository.findAll().size();

        // Create the BuildRequest
        BuildRequestDTO buildRequestDTO = buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest);

        restBuildRequestMockMvc.perform(post("/api/buildRequests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildRequestDTO)))
            .andExpect(status().isCreated());

        // Validate the BuildRequest in the database
        List<BuildRequest> buildRequests = buildRequestRepository.findAll();
        assertThat(buildRequests).hasSize(databaseSizeBeforeCreate + 1);
        BuildRequest testBuildRequest = buildRequests.get(buildRequests.size() - 1);
        assertThat(testBuildRequest.getEngine()).isEqualTo(DEFAULT_ENGINE);
        assertThat(testBuildRequest.getPlatform()).isEqualTo(DEFAULT_PLATFORM);
        assertThat(testBuildRequest.getFlavor()).isEqualTo(DEFAULT_FLAVOR);
        assertThat(testBuildRequest.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testBuildRequest.getStartTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(testBuildRequest.getEndTime()).isEqualTo(DEFAULT_END_TIME);
        assertThat(testBuildRequest.getMessage()).isEqualTo(DEFAULT_MESSAGE);
    }

    @Test
    @Transactional
    public void checkEngineIsRequired() throws Exception {
        int databaseSizeBeforeTest = buildRequestRepository.findAll().size();
        // set the field null
        buildRequest.setEngine(null);

        // Create the BuildRequest, which fails.
        BuildRequestDTO buildRequestDTO = buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest);

        restBuildRequestMockMvc.perform(post("/api/buildRequests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildRequestDTO)))
            .andExpect(status().isBadRequest());

        List<BuildRequest> buildRequests = buildRequestRepository.findAll();
        assertThat(buildRequests).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPlatformIsRequired() throws Exception {
        int databaseSizeBeforeTest = buildRequestRepository.findAll().size();
        // set the field null
        buildRequest.setPlatform(null);

        // Create the BuildRequest, which fails.
        BuildRequestDTO buildRequestDTO = buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest);

        restBuildRequestMockMvc.perform(post("/api/buildRequests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildRequestDTO)))
            .andExpect(status().isBadRequest());

        List<BuildRequest> buildRequests = buildRequestRepository.findAll();
        assertThat(buildRequests).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkFlavorIsRequired() throws Exception {
        int databaseSizeBeforeTest = buildRequestRepository.findAll().size();
        // set the field null
        buildRequest.setFlavor(null);

        // Create the BuildRequest, which fails.
        BuildRequestDTO buildRequestDTO = buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest);

        restBuildRequestMockMvc.perform(post("/api/buildRequests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildRequestDTO)))
            .andExpect(status().isBadRequest());

        List<BuildRequest> buildRequests = buildRequestRepository.findAll();
        assertThat(buildRequests).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = buildRequestRepository.findAll().size();
        // set the field null
        buildRequest.setStatus(null);

        // Create the BuildRequest, which fails.
        BuildRequestDTO buildRequestDTO = buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest);

        restBuildRequestMockMvc.perform(post("/api/buildRequests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildRequestDTO)))
            .andExpect(status().isBadRequest());

        List<BuildRequest> buildRequests = buildRequestRepository.findAll();
        assertThat(buildRequests).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllBuildRequests() throws Exception {
        // Initialize the database
        buildRequestRepository.saveAndFlush(buildRequest);

        // Get all the buildRequests
        restBuildRequestMockMvc.perform(get("/api/buildRequests?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(buildRequest.getId().intValue())))
            .andExpect(jsonPath("$.[*].engine").value(hasItem(DEFAULT_ENGINE.toString())))
            .andExpect(jsonPath("$.[*].platform").value(hasItem(DEFAULT_PLATFORM.toString())))
            .andExpect(jsonPath("$.[*].flavor").value(hasItem(DEFAULT_FLAVOR.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME_STR)))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME_STR)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE.toString())));
    }

    @Test
    @Transactional
    public void getAllBuildRequestsIsEmptyForNewUser() throws Exception {
        // Initialize the database
        buildRequestRepository.saveAndFlush(buildRequest);

        SecurityTestUtils.loginAs("bad", "guy");
        // Get all the buildRequests
        restBuildRequestMockMvc.perform(get("/api/buildRequests?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getBuildRequest() throws Exception {
        // Initialize the database
        buildChainRepository.save(buildChain);
        buildRequestRepository.saveAndFlush(buildRequest);

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}", buildRequest.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(jsonPath("$.id").value(buildRequest.getId().intValue()))
            .andExpect(jsonPath("$.chainId").isNumber())
            .andExpect(jsonPath("$.chainId").value(buildRequest.getChain().getId().intValue()))
            .andExpect(jsonPath("$.engine").value(DEFAULT_ENGINE.toString()))
            .andExpect(jsonPath("$.platform").value(DEFAULT_PLATFORM.toString()))
            .andExpect(jsonPath("$.flavor").value(DEFAULT_FLAVOR.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.startTime").value(DEFAULT_START_TIME_STR))
            .andExpect(jsonPath("$.endTime").value(DEFAULT_END_TIME_STR))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE.toString()));
    }

    @Test
    @Transactional
    public void onlyOwnerCanGetBuildRequest() throws Exception {
        // Initialize the database
        buildChainRepository.save(buildChain);
        buildRequestRepository.saveAndFlush(buildRequest);

        SecurityTestUtils.loginAs("bad", "guy");

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}", buildRequest.getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void getBuildRequestLog() throws Exception {
        // Initialize the database
        buildRequestRepository.saveAndFlush(buildRequest);

        // Get the buildRequest
        MvcResult mvcResult = restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}/log", buildRequest.getId()).accept(MediaType.TEXT_PLAIN))
            .andDo(print())
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();

    }


    @Test
    @Transactional
    public void onlyOwnerCanGetBuildRequestLog() throws Exception {
        // Initialize the database
        buildChainRepository.save(buildChain);
        buildRequestRepository.saveAndFlush(buildRequest);

        SecurityTestUtils.loginAs("bad", "guy");

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}/log", buildRequest.getId()))
            .andExpect(status().isUnauthorized());
    }


    @Test
    @Transactional
    public void getBuildRequestRunningArtifactsIsEmpty() throws Exception {
        // Initialize the database
        buildRequest.start();
        buildRequestRepository.saveAndFlush(buildRequest);

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}/artifacts", buildRequest.getId()))
            .andDo(print())
            .andExpect(status().isNoContent());
    }


    @Test
    @Transactional
    public void getBuildRequestCancelledArtifactsIsEmpty() throws Exception {
        // Initialize the database
        buildRequest.start();
        buildRequest.finish(CANCELLED, "CANCELLED");
        buildRequestRepository.saveAndFlush(buildRequest);

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}/artifacts", buildRequest.getId()))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    @Transactional
    public void getBuildRequestFailedArtifactsIsEmpty() throws Exception {

        // Initialize the database
        buildRequest.start();
        buildRequest.finish(FAILED, "FAILED");
        buildRequestRepository.saveAndFlush(buildRequest);

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}/artifacts", buildRequest.getId()))
            .andDo(print())
            .andExpect(status().isNoContent());
    }


    @Test
    @Transactional
    public void onlyOwnerCanGetBuildRequestArtifacts() throws Exception {
        // Initialize the database
        buildChainRepository.save(buildChain);
        buildRequestRepository.saveAndFlush(buildRequest);

        SecurityTestUtils.loginAs("bad", "guy");

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}/artifacts/compressed", buildRequest.getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void getBuildRequestSuccessfulArtifactsIsZip() throws Exception {
        // Initialize the database
        buildRequest.start();
        buildRequest.finish(SUCCESSFUL, "SUCCESSFUL");
        buildRequestRepository.saveAndFlush(buildRequest);

        //Add the artifact to the distribution channel
        File tempFile = File.createTempFile("artifact", ".tmp");
        FileWriter writer = new FileWriter(tempFile);
        writer.write("This is a sample artifact\nIt doesn't have much content\nBut it's useful for testing");
        writer.close();
        Artifact tempArtifact = new Artifact(tempFile.toURI(), tempFile.getName(), tempFile.length());
        distributionChannelService.distributeArtifacts(buildRequestService.findOne(buildRequest.getId()), Stream.of(tempArtifact).collect(Collectors.toList()));

        // Get the buildRequest
        MvcResult mvcResult = restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}/artifacts/compressed", buildRequest.getId()))
            .andDo(print())
            .andExpect(content().contentType("application/zip"))
            .andExpect(status().isOk())
            .andReturn();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(mvcResult.getResponse().getContentAsByteArray());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        List<String> zipFiles = new ArrayList<>();
        ZipEntry zipEntry = null;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            zipFiles.add(zipEntry.getName());
        }
        assertThat(zipFiles).contains(tempArtifact.getName());

    }


    @Test
    @Transactional
    public void getBuildRequestNonExistent() throws Exception {

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}/artifacts", Integer.MAX_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void getBuildRequestLogNotFound() throws Exception {

        // Get the buildRequest
        MvcResult mvcResult = restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}/log", Integer.MAX_VALUE).accept(MediaType.TEXT_PLAIN))
            .andDo(print())
            .andExpect(content().contentType(MediaType.TEXT_PLAIN))
            .andExpect(status().isNotFound())
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isNotEmpty();
    }

    @Test
    @Transactional
    public void getNonExistingBuildRequest() throws Exception {
        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void onlyOwnerCanUpdateBuildRequest() throws Exception {
        // Initialize the database
        buildRequestRepository.saveAndFlush(buildRequest);

        // Update the buildRequest
        buildRequest.setEngine(UPDATED_ENGINE);
        buildRequest.setPlatform(UPDATED_PLATFORM);
        buildRequest.setFlavor(UPDATED_FLAVOR);
        buildRequest.setStatus(UPDATED_STATUS);
        buildRequest.setStartTime(UPDATED_START_TIME);
        buildRequest.setEndTime(UPDATED_END_TIME);
        buildRequest.setMessage(UPDATED_MESSAGE);
        BuildRequestDTO buildRequestDTO = buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest);

        SecurityTestUtils.loginAs("bad", "guy");

        restBuildRequestMockMvc.perform(put("/api/buildRequests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildRequestDTO)))
            .andExpect(status().isUnauthorized());
    }


    @Test
    @Transactional
    public void updateBuildRequest() throws Exception {
        // Initialize the database
        buildRequestRepository.saveAndFlush(buildRequest);

        int databaseSizeBeforeUpdate = buildRequestRepository.findAll().size();

        // Update the buildRequest
        buildRequest.setEngine(UPDATED_ENGINE);
        buildRequest.setPlatform(UPDATED_PLATFORM);
        buildRequest.setFlavor(UPDATED_FLAVOR);
        buildRequest.setStatus(UPDATED_STATUS);
        buildRequest.setStartTime(UPDATED_START_TIME);
        buildRequest.setEndTime(UPDATED_END_TIME);
        buildRequest.setMessage(UPDATED_MESSAGE);
        BuildRequestDTO buildRequestDTO = buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest);

        restBuildRequestMockMvc.perform(put("/api/buildRequests")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildRequestDTO)))
            .andExpect(status().isOk());

        // Validate the BuildRequest in the database
        List<BuildRequest> buildRequests = buildRequestRepository.findAll();
        assertThat(buildRequests).hasSize(databaseSizeBeforeUpdate);
        BuildRequest testBuildRequest = buildRequests.get(buildRequests.size() - 1);
        assertThat(testBuildRequest.getEngine()).isEqualTo(UPDATED_ENGINE);
        assertThat(testBuildRequest.getPlatform()).isEqualTo(UPDATED_PLATFORM);
        assertThat(testBuildRequest.getFlavor()).isEqualTo(UPDATED_FLAVOR);
        assertThat(testBuildRequest.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testBuildRequest.getStartTime()).isEqualTo(UPDATED_START_TIME);
        assertThat(testBuildRequest.getEndTime()).isEqualTo(UPDATED_END_TIME);
        assertThat(testBuildRequest.getMessage()).isEqualTo(UPDATED_MESSAGE);
    }

    @Test
    @Transactional
    public void onlyOwnerCanDeleteBuildRequest() throws Exception {
        // Initialize the database
        buildRequestRepository.saveAndFlush(buildRequest);

        int databaseSizeBeforeDelete = buildRequestRepository.findAll().size();
        SecurityTestUtils.loginAs("bad", "guy");

        // Get the buildRequest
        restBuildRequestMockMvc.perform(delete("/api/buildRequests/{id}", buildRequest.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isUnauthorized());

        // Validate the database is empty
        List<BuildRequest> buildRequests = buildRequestRepository.findAll();
        assertThat(buildRequests).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void deleteBuildRequest() throws Exception {
        // Initialize the database
        buildRequestRepository.saveAndFlush(buildRequest);

        int databaseSizeBeforeDelete = buildRequestRepository.findAll().size();

        // Get the buildRequest
        restBuildRequestMockMvc.perform(delete("/api/buildRequests/{id}", buildRequest.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<BuildRequest> buildRequests = buildRequestRepository.findAll();
        assertThat(buildRequests).hasSize(databaseSizeBeforeDelete - 1);
    }
}
