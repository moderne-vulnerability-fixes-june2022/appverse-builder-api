package org.appverse.builder.web.rest;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.appverse.builder.Application;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.domain.BuildChain;
import org.appverse.builder.domain.enumeration.BuildStatus;
import org.appverse.builder.dto.BuildInfoDTO;
import org.appverse.builder.repository.BuildChainRepository;
import org.appverse.builder.security.SecurityTestUtils;
import org.appverse.builder.service.BuildChainService;
import org.appverse.builder.service.BuildRequestService;
import org.appverse.builder.service.PayloadService;
import org.appverse.builder.service.UserService;
import org.appverse.builder.web.rest.dto.BuildChainDTO;
import org.appverse.builder.web.rest.mapper.BuildChainMapper;
import org.appverse.builder.web.rest.mapper.BuildRequestMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the BuildChainResource REST controller.
 *
 * @see BuildChainResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class BuildChainResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("Z"));


    private static final ZonedDateTime DEFAULT_CREATED_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_CREATED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_CREATED_DATE_STR = dateTimeFormatter.format(DEFAULT_CREATED_DATE);

    @Inject
    private BuildChainRepository buildChainRepository;

    @Inject
    private BuildChainMapper buildChainMapper;

    @Inject
    private BuildChainService buildChainService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restBuildChainMockMvc;

    private BuildChain buildChain;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private PayloadService payloadService;

    @Inject
    private UserService userService;

    private MockMvc restBuildRequestMockMvc;
    @Inject
    private BuildRequestService buildRequestService;
    @Inject
    private BuildRequestMapper buildRequestMapper;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        BuildChainResource buildChainResource = new BuildChainResource();
        ReflectionTestUtils.setField(buildChainResource, "buildChainService", buildChainService);
        ReflectionTestUtils.setField(buildChainResource, "buildChainMapper", buildChainMapper);
        ReflectionTestUtils.setField(buildChainResource, "buildRequestService", buildRequestService);
        this.restBuildChainMockMvc = MockMvcBuilders.standaloneSetup(buildChainResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();

        BuildRequestResource buildRequestResource = new BuildRequestResource();
        ReflectionTestUtils.setField(buildRequestResource, "buildRequestService", buildRequestService);
        ReflectionTestUtils.setField(buildRequestResource, "buildRequestMapper", buildRequestMapper);
        this.restBuildRequestMockMvc = MockMvcBuilders.standaloneSetup(buildRequestResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(new ResourceHttpMessageConverter(), jacksonMessageConverter).build();
    }

    @After
    public void destroy() throws IOException {
        FileUtils.deleteDirectory(appverseBuilderProperties.getBuild().getBuildRoot());
    }

    @Before
    public void initTest() {
        SecurityTestUtils.loginAsRegularUser();
        buildChain = new BuildChain();
        buildChain.setRequester(userService.getUserWithAuthorities());
        buildChain.setCreatedDate(DEFAULT_CREATED_DATE);
    }

    @Test
    @Transactional
    public void createBuildChain() throws Exception {
        int databaseSizeBeforeCreate = buildChainRepository.findAll().size();

        // Create the BuildChain
        BuildChainDTO buildChainDTO = buildChainMapper.buildChainToBuildChainDTO(buildChain);

        restBuildChainMockMvc.perform(post("/api/buildChains")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildChainDTO)))
            .andExpect(status().isCreated());

        // Validate the BuildChain in the database
        List<BuildChain> buildChains = buildChainRepository.findAll();
        assertThat(buildChains).hasSize(databaseSizeBeforeCreate + 1);
        BuildChain testBuildChain = buildChains.get(buildChains.size() - 1);
        assertThat(testBuildChain.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
    }

    @Test
    @Transactional
    public void checkCreatedDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = buildChainRepository.findAll().size();
        // set the field null
        buildChain.setCreatedDate(null);

        // Create the BuildChain, which fails.
        BuildChainDTO buildChainDTO = buildChainMapper.buildChainToBuildChainDTO(buildChain);

        restBuildChainMockMvc.perform(post("/api/buildChains")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildChainDTO)))
            .andExpect(status().isBadRequest());

        List<BuildChain> buildChains = buildChainRepository.findAll();
        assertThat(buildChains).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllBuildChains() throws Exception {
        // Initialize the database
        buildChainRepository.saveAndFlush(buildChain);

        // Get all the buildChains
        restBuildChainMockMvc.perform(get("/api/buildChains?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(buildChain.getId().intValue())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE_STR)));
    }

    @Test
    @Transactional
    public void getAllBuildChainsIsEmptyForNewUser() throws Exception {
        // Initialize the database
        buildChainRepository.saveAndFlush(buildChain);

        SecurityTestUtils.loginAs("bad", "guy");

        // Get all the buildChains
        restBuildChainMockMvc.perform(get("/api/buildChains?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    @Transactional
    public void getBuildChain() throws Exception {
        // Initialize the database
        buildChainRepository.saveAndFlush(buildChain);

        // Get the buildChain
        restBuildChainMockMvc.perform(get("/api/buildChains/{id}", buildChain.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(buildChain.getId().intValue()))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE_STR));
    }

    @Test
    @Transactional
    public void onlyOwnerCanGetBuildChain() throws Exception {
        // Initialize the database
        buildChainRepository.saveAndFlush(buildChain);

        SecurityTestUtils.loginAs("bad", "guy");

        // Get the buildChain
        restBuildChainMockMvc.perform(get("/api/buildChains/{id}", buildChain.getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void getNonExistingBuildChain() throws Exception {
        // Get the buildChain
        restBuildChainMockMvc.perform(get("/api/buildChains/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBuildChain() throws Exception {
        // Initialize the database
        buildChainRepository.saveAndFlush(buildChain);

        int databaseSizeBeforeUpdate = buildChainRepository.findAll().size();

        // Update the buildChain
        buildChain.setCreatedDate(UPDATED_CREATED_DATE);
        BuildChainDTO buildChainDTO = buildChainMapper.buildChainToBuildChainDTO(buildChain);

        restBuildChainMockMvc.perform(put("/api/buildChains")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildChainDTO)))
            .andExpect(status().isOk());

        // Validate the BuildChain in the database
        List<BuildChain> buildChains = buildChainRepository.findAll();
        assertThat(buildChains).hasSize(databaseSizeBeforeUpdate);
        BuildChain testBuildChain = buildChains.get(buildChains.size() - 1);
        assertThat(testBuildChain.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    public void onlyOwnerCanUpdateBuildChain() throws Exception {
        // Initialize the database
        buildChainRepository.saveAndFlush(buildChain);

        // Update the buildChain
        buildChain.setCreatedDate(UPDATED_CREATED_DATE);
        BuildChainDTO buildChainDTO = buildChainMapper.buildChainToBuildChainDTO(buildChain);

        SecurityTestUtils.loginAs("bad", "guy");
        restBuildChainMockMvc.perform(put("/api/buildChains")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildChainDTO)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void deleteBuildChain() throws Exception {
        // Initialize the database
        buildChainRepository.saveAndFlush(buildChain);

        int databaseSizeBeforeDelete = buildChainRepository.findAll().size();

        // Get the buildChain
        restBuildChainMockMvc.perform(delete("/api/buildChains/{id}", buildChain.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<BuildChain> buildChains = buildChainRepository.findAll();
        assertThat(buildChains).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void onlyOwnerCanDeleteBuildChain() throws Exception {
        // Initialize the database
        buildChainRepository.saveAndFlush(buildChain);

        int databaseSizeBeforeDelete = buildChainRepository.findAll().size();

        SecurityTestUtils.loginAs("bad", "guy");
        // Get the buildChain
        restBuildChainMockMvc.perform(delete("/api/buildChains/{id}", buildChain.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isUnauthorized());

        List<BuildChain> buildChains = buildChainRepository.findAll();
        assertThat(buildChains).hasSize(databaseSizeBeforeDelete);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void createBuildChainFromPayload() throws Exception {
        File nativeDemoDir = new ClassPathResource("demo/native").getFile();
        File tempFile = File.createTempFile("payload-test", ".zip");
        tempFile.delete();
        ZipFile zipFile = new ZipFile(tempFile);
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
        parameters.setIncludeRootFolder(false);
        zipFile.createZipFileFromFolder(nativeDemoDir, parameters, false, 0L);
        MockMultipartFile file = new MockMultipartFile("payload", new FileInputStream(zipFile.getFile()));
        Optional<BuildInfoDTO> buildInfoDTO = payloadService.parseBuildInfoFile(nativeDemoDir);

        assertThat(buildInfoDTO.isPresent()).isTrue();

        final List<BuildInfoDTO.FlavorInfoDTO> flavors = new ArrayList<>();
        buildInfoDTO.get().getEngine().getPlatforms().stream().forEach(platformInfoDTO -> flavors.addAll(platformInfoDTO.getFlavors()));

        MvcResult mvcResult = restBuildChainMockMvc.perform(fileUpload("/api/buildChains/payload")
            .file(file))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(jsonPath("$.requesterLogin").value(SecurityTestUtils.USER_LOGIN))
            .andExpect(jsonPath("$.requests").isArray())
            .andExpect(jsonPath("$.requests", hasSize(flavors.size())))
            .andExpect(jsonPath("$.requests.[*].engine").value(hasItems(buildInfoDTO.get().getEngine().getName())))
            .andExpect(jsonPath("$.requests.[*].platform").value(hasItems(buildInfoDTO.get().getEngine().getPlatforms().stream().map(BuildInfoDTO.PlatformInfoDTO::getName).toArray())))
            .andExpect(jsonPath("$.requests[*].flavor").value(hasItems(flavors.stream().map(BuildInfoDTO.FlavorInfoDTO::getName).toArray())))
            .andReturn();

        tempFile.delete();


        BuildChainDTO chainDTO = jacksonMessageConverter.getObjectMapper().reader().forType(BuildChainDTO.class)
            .readValue(mvcResult.getResponse().getContentAsByteArray());

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}", chainDTO.getRequests().stream().findAny().get().getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(jsonPath("$.status").value(BuildStatus.CANCELLED.toString()))
            .andExpect(jsonPath("$.endTime").isNotEmpty());

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void createBuildChainFromPayloadWithOptions() throws Exception {
        File nativeDemoDir = new ClassPathResource("demo/native").getFile();
        File tempFile = File.createTempFile("payload-test", ".zip");
        tempFile.delete();
        ZipFile zipFile = new ZipFile(tempFile);
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
        parameters.setIncludeRootFolder(false);
        zipFile.createZipFileFromFolder(nativeDemoDir, parameters, false, 0L);
        MockMultipartFile file = new MockMultipartFile("payload", new FileInputStream(zipFile.getFile()));
        Optional<BuildInfoDTO> buildInfoDTO = payloadService.parseBuildInfoFile(nativeDemoDir);

        assertThat(buildInfoDTO.isPresent()).isTrue();

        final List<BuildInfoDTO.FlavorInfoDTO> flavors = new ArrayList<>();
        buildInfoDTO.get().getEngine().getPlatforms().stream().forEach(platformInfoDTO -> flavors.addAll(platformInfoDTO.getFlavors()));
        Map<String, String> options = new HashMap<>();
        options.put("option1", "value1");

        MvcResult mvcResult = restBuildChainMockMvc.perform(fileUpload("/api/buildChains/payload")
            .file(file)
            .param("options", options.entrySet().stream().map(entry -> entry.getKey() + BuildChainResource.OPTION_SPLITTER + entry.getValue()).collect(Collectors.toList()).toArray(new String[options.size()])))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(jsonPath("$.requesterLogin").value(SecurityTestUtils.USER_LOGIN))
            .andExpect(jsonPath("$.requests").isArray())
            .andExpect(jsonPath("$.requests", hasSize(flavors.size())))
            .andExpect(jsonPath("$.requests.[*].engine").value(hasItems(buildInfoDTO.get().getEngine().getName())))
            .andExpect(jsonPath("$.requests.[*].platform").value(hasItems(buildInfoDTO.get().getEngine().getPlatforms().stream().map(BuildInfoDTO.PlatformInfoDTO::getName).toArray())))
            .andExpect(jsonPath("$.requests[*].flavor").value(hasItems(flavors.stream().map(BuildInfoDTO.FlavorInfoDTO::getName).toArray())))
            .andReturn();

        tempFile.delete();


        BuildChainDTO chainDTO = jacksonMessageConverter.getObjectMapper().reader().forType(BuildChainDTO.class)
            .readValue(mvcResult.getResponse().getContentAsByteArray());
        assertThat(chainDTO.getOptions()).isEqualTo(options);

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}", chainDTO.getRequests().stream().findAny().get().getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(jsonPath("$.status").value(BuildStatus.CANCELLED.toString()))
            .andExpect(jsonPath("$.endTime").isNotEmpty());

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void createBuildChainFromPayloadWithFlavor() throws Exception {
        File nativeDemoDir = new ClassPathResource("demo/native").getFile();
        File tempFile = File.createTempFile("payload-test", ".zip");
        tempFile.delete();
        ZipFile zipFile = new ZipFile(tempFile);
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
        parameters.setIncludeRootFolder(false);
        zipFile.createZipFileFromFolder(nativeDemoDir, parameters, false, 0L);
        MockMultipartFile file = new MockMultipartFile("payload", new FileInputStream(zipFile.getFile()));
        Optional<BuildInfoDTO> buildInfoDTO = payloadService.parseBuildInfoFile(nativeDemoDir);

        assertThat(buildInfoDTO.isPresent()).isTrue();

        final List<BuildInfoDTO.FlavorInfoDTO> flavors = new ArrayList<>();
        buildInfoDTO.get().getEngine().getPlatforms().stream().forEach(platformInfoDTO -> flavors.addAll(platformInfoDTO.getFlavors()));

        String flavor = buildInfoDTO.get().getEngine().getPlatforms().stream().findAny().get().getFlavors().stream().findAny().get().getName();
        MvcResult mvcResult = restBuildChainMockMvc.perform(fileUpload("/api/buildChains/payload")
            .file(file)
            .param("flavor", flavor)
        )
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(jsonPath("$.requesterLogin").value(SecurityTestUtils.USER_LOGIN))
            .andExpect(jsonPath("$.requests").isArray())
            .andExpect(jsonPath("$.requests", hasSize(1)))
            .andExpect(jsonPath("$.requests.[*].engine").value(hasItems(buildInfoDTO.get().getEngine().getName())))
            .andExpect(jsonPath("$.requests.[*].platform").value(hasItems(buildInfoDTO.get().getEngine().getPlatforms().stream().map(BuildInfoDTO.PlatformInfoDTO::getName).toArray())))
            .andExpect(jsonPath("$.requests[*].flavor").value(flavor))
            .andReturn();

        tempFile.delete();


        BuildChainDTO chainDTO = jacksonMessageConverter.getObjectMapper().reader().forType(BuildChainDTO.class)
            .readValue(mvcResult.getResponse().getContentAsByteArray());

        // Get the buildRequest
        restBuildRequestMockMvc.perform(get("/api/buildRequests/{id}", chainDTO.getRequests().stream().findAny().get().getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(jsonPath("$.status").value(BuildStatus.CANCELLED.toString()))
            .andExpect(jsonPath("$.endTime").isNotEmpty());

    }
}
