package org.appverse.builder.web.rest;

import org.appverse.builder.Application;
import org.appverse.builder.domain.Engine;
import org.appverse.builder.domain.EnginePlatform;
import org.appverse.builder.domain.enumeration.ImageType;
import org.appverse.builder.repository.EnginePlatformRepository;
import org.appverse.builder.repository.EngineRepository;
import org.appverse.builder.service.EnginePlatformService;
import org.appverse.builder.web.rest.dto.EnginePlatformDTO;
import org.appverse.builder.web.rest.mapper.EnginePlatformMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the EnginePlatformResource REST controller.
 *
 * @see EnginePlatformResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class EnginePlatformResourceIntTest {

    private static final String DEFAULT_NAME = "AAA";
    private static final String UPDATED_NAME = "BBB";
    private static final String DEFAULT_VERSION = "AAAAA";
    private static final String UPDATED_VERSION = "BBBBB";
    private static final String DEFAULT_IMAGE_NAME = "AAAAA";
    private static final String UPDATED_IMAGE_NAME = "BBBBB";

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;
    public static final String AGENT_REQ_1 = "REQ1";
    public static final String AGENT_REQ_1_VALUE = "REQ1_VALUE";

    private static final ImageType DEFAULT_IMAGE_TYPE = ImageType.DOCKER;
    private static final ImageType UPDATED_IMAGE_TYPE = ImageType.VAGRANT;

    @Inject
    private EnginePlatformRepository enginePlatformRepository;

    @Inject
    private EngineRepository engineRepository;

    @Inject
    private EnginePlatformMapper enginePlatformMapper;

    @Inject
    private EnginePlatformService enginePlatformService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restEnginePlatformMockMvc;

    private EnginePlatform enginePlatform;

    private Engine engine;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EnginePlatformResource enginePlatformResource = new EnginePlatformResource();
        ReflectionTestUtils.setField(enginePlatformResource, "enginePlatformService", enginePlatformService);
        ReflectionTestUtils.setField(enginePlatformResource, "enginePlatformMapper", enginePlatformMapper);
        this.restEnginePlatformMockMvc = MockMvcBuilders.standaloneSetup(enginePlatformResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        enginePlatform = new EnginePlatform();
        enginePlatform.setName(DEFAULT_NAME);
        enginePlatform.setVersion(DEFAULT_VERSION);
        enginePlatform.setImageName(DEFAULT_IMAGE_NAME);
        enginePlatform.setEnabled(DEFAULT_ENABLED);
        enginePlatform.setImageType(DEFAULT_IMAGE_TYPE);
        enginePlatform.getAgentRequirements().put(AGENT_REQ_1, AGENT_REQ_1_VALUE);

        engine = new Engine();
        engine.setName("ENGINE1");
        engine.setEnabled(true);
        engine.setDescription("Engine description 1");
        engine.setVersion("alpha");
        engineRepository.save(engine);
        enginePlatform.setEngine(engine);

    }

    @Test
    @Transactional
    public void createEnginePlatform() throws Exception {
        engineRepository.save(engine);
        int databaseSizeBeforeCreate = enginePlatformRepository.findAll().size();

        // Create the EnginePlatform
        EnginePlatformDTO enginePlatformDTO = enginePlatformMapper.enginePlatformToEnginePlatformDTO(enginePlatform);

        restEnginePlatformMockMvc.perform(post("/api/enginePlatforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformDTO)))
            .andExpect(status().isCreated());

        // Validate the EnginePlatform in the database
        List<EnginePlatform> enginePlatforms = enginePlatformRepository.findAll();
        assertThat(enginePlatforms).hasSize(databaseSizeBeforeCreate + 1);
        EnginePlatform testEnginePlatform = enginePlatforms.get(enginePlatforms.size() - 1);
        assertThat(testEnginePlatform.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testEnginePlatform.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testEnginePlatform.getImageName()).isEqualTo(DEFAULT_IMAGE_NAME);
        assertThat(testEnginePlatform.getEnabled()).isEqualTo(DEFAULT_ENABLED);
        assertThat(testEnginePlatform.getImageType()).isEqualTo(DEFAULT_IMAGE_TYPE);
        assertThat(testEnginePlatform.getAgentRequirements()).isNotEmpty();
        assertThat(testEnginePlatform.getAgentRequirements()).containsKey(AGENT_REQ_1);
        assertThat(testEnginePlatform.getAgentRequirements().get(AGENT_REQ_1)).isEqualTo(AGENT_REQ_1_VALUE);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = enginePlatformRepository.findAll().size();
        // set the field null
        enginePlatform.setName(null);

        // Create the EnginePlatform, which fails.
        EnginePlatformDTO enginePlatformDTO = enginePlatformMapper.enginePlatformToEnginePlatformDTO(enginePlatform);

        restEnginePlatformMockMvc.perform(post("/api/enginePlatforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformDTO)))
            .andExpect(status().isBadRequest());

        List<EnginePlatform> enginePlatforms = enginePlatformRepository.findAll();
        assertThat(enginePlatforms).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = enginePlatformRepository.findAll().size();
        // set the field null
        enginePlatform.setVersion(null);

        // Create the EnginePlatform, which fails.
        EnginePlatformDTO enginePlatformDTO = enginePlatformMapper.enginePlatformToEnginePlatformDTO(enginePlatform);

        restEnginePlatformMockMvc.perform(post("/api/enginePlatforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformDTO)))
            .andExpect(status().isBadRequest());

        List<EnginePlatform> enginePlatforms = enginePlatformRepository.findAll();
        assertThat(enginePlatforms).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEnabledIsRequired() throws Exception {
        int databaseSizeBeforeTest = enginePlatformRepository.findAll().size();
        // set the field null
        enginePlatform.setEnabled(null);
        enginePlatform.setEngine(engine);
        // Create the EnginePlatform, which fails.
        EnginePlatformDTO enginePlatformDTO = enginePlatformMapper.enginePlatformToEnginePlatformDTO(enginePlatform);

        restEnginePlatformMockMvc.perform(post("/api/enginePlatforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformDTO)))
            .andExpect(status().isBadRequest());

        List<EnginePlatform> enginePlatforms = enginePlatformRepository.findAll();
        assertThat(enginePlatforms).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkImageTypeIsDefaultIsDocker() throws Exception {
        int databaseSizeBeforeTest = enginePlatformRepository.findAll().size();
        // set the field null
        enginePlatform.setImageType(null);

        // Create the EnginePlatform, which fails.
        EnginePlatformDTO enginePlatformDTO = enginePlatformMapper.enginePlatformToEnginePlatformDTO(enginePlatform);

        restEnginePlatformMockMvc.perform(post("/api/enginePlatforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformDTO)))
            .andExpect(status().isCreated());

        List<EnginePlatform> enginePlatforms = enginePlatformRepository.findAll();
        assertThat(enginePlatforms).hasSize(databaseSizeBeforeTest + 1);
        EnginePlatform testEnginePlatform = enginePlatforms.get(enginePlatforms.size() - 1);
        assertThat(testEnginePlatform.getImageType()).isEqualTo(ImageType.DOCKER);
    }

    @Test
    @Transactional
    public void getAllEnginePlatforms() throws Exception {
        // Initialize the database
        engineRepository.save(engine);
        enginePlatformRepository.saveAndFlush(enginePlatform);

        // Get all the enginePlatforms
        restEnginePlatformMockMvc.perform(get("/api/enginePlatforms?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(enginePlatform.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION.toString())))
            .andExpect(jsonPath("$.[*].imageName").value(hasItem(DEFAULT_IMAGE_NAME.toString())))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED.booleanValue())))
            .andExpect(jsonPath("$.[*].imageType").value(hasItem(DEFAULT_IMAGE_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getEnginePlatform() throws Exception {
        // Initialize the database
        engineRepository.save(engine);
        enginePlatformRepository.saveAndFlush(enginePlatform);

        // Get the enginePlatform
        restEnginePlatformMockMvc.perform(get("/api/enginePlatforms/{id}", enginePlatform.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(enginePlatform.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION.toString()))
            .andExpect(jsonPath("$.imageName").value(DEFAULT_IMAGE_NAME.toString()))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED.booleanValue()))
            .andExpect(jsonPath("$.imageType").value(DEFAULT_IMAGE_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEnginePlatform() throws Exception {
        // Get the enginePlatform
        restEnginePlatformMockMvc.perform(get("/api/enginePlatforms/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEnginePlatform() throws Exception {
        // Initialize the database
        engineRepository.save(engine);
        enginePlatformRepository.saveAndFlush(enginePlatform);

        int databaseSizeBeforeUpdate = enginePlatformRepository.findAll().size();

        // Update the enginePlatform
        enginePlatform.setName(UPDATED_NAME);
        enginePlatform.setVersion(UPDATED_VERSION);
        enginePlatform.setImageName(UPDATED_IMAGE_NAME);
        enginePlatform.setEnabled(UPDATED_ENABLED);
        enginePlatform.setImageType(UPDATED_IMAGE_TYPE);
        EnginePlatformDTO enginePlatformDTO = enginePlatformMapper.enginePlatformToEnginePlatformDTO(enginePlatform);

        restEnginePlatformMockMvc.perform(put("/api/enginePlatforms")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformDTO)))
            .andExpect(status().isOk());

        // Validate the EnginePlatform in the database
        List<EnginePlatform> enginePlatforms = enginePlatformRepository.findAll();
        assertThat(enginePlatforms).hasSize(databaseSizeBeforeUpdate);
        EnginePlatform testEnginePlatform = enginePlatforms.get(enginePlatforms.size() - 1);
        assertThat(testEnginePlatform.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testEnginePlatform.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testEnginePlatform.getImageName()).isEqualTo(UPDATED_IMAGE_NAME);
        assertThat(testEnginePlatform.getEnabled()).isEqualTo(UPDATED_ENABLED);
        assertThat(testEnginePlatform.getImageType()).isEqualTo(UPDATED_IMAGE_TYPE);
    }

    @Test
    @Transactional
    public void deleteEnginePlatform() throws Exception {
        // Initialize the database
        engineRepository.save(engine);
        enginePlatformRepository.saveAndFlush(enginePlatform);

        int databaseSizeBeforeDelete = enginePlatformRepository.findAll().size();

        // Get the enginePlatform
        restEnginePlatformMockMvc.perform(delete("/api/enginePlatforms/{id}", enginePlatform.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<EnginePlatform> enginePlatforms = enginePlatformRepository.findAll();
        assertThat(enginePlatforms).hasSize(databaseSizeBeforeDelete - 1);
    }
}
