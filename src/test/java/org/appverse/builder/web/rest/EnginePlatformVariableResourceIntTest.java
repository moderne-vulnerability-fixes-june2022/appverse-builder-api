package org.appverse.builder.web.rest;

import org.appverse.builder.Application;
import org.appverse.builder.domain.Engine;
import org.appverse.builder.domain.EnginePlatform;
import org.appverse.builder.domain.EnginePlatformVariable;
import org.appverse.builder.repository.EnginePlatformRepository;
import org.appverse.builder.repository.EnginePlatformVariableRepository;
import org.appverse.builder.repository.EngineRepository;
import org.appverse.builder.service.EnginePlatformVariableService;
import org.appverse.builder.web.rest.dto.EnginePlatformVariableDTO;
import org.appverse.builder.web.rest.mapper.EnginePlatformVariableMapper;
import org.appverse.builder.domain.enumeration.ImageType;
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
 * Test class for the EnginePlatformVariableResource REST controller.
 *
 * @see EnginePlatformVariableResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class EnginePlatformVariableResourceIntTest {

    private static final String DEFAULT_NAME = "AA";
    private static final String UPDATED_NAME = "BB";
    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";

    private static final Boolean DEFAULT_REQUIRED = false;
    private static final Boolean UPDATED_REQUIRED = true;
    private static final String DEFAULT_DEFAULT_VALUE = "AAAAA";
    private static final String UPDATED_DEFAULT_VALUE = "BBBBB";

    @Inject
    private EnginePlatformVariableRepository enginePlatformVariableRepository;

    @Inject
    private EnginePlatformVariableMapper enginePlatformVariableMapper;

    @Inject
    private EnginePlatformVariableService enginePlatformVariableService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EnginePlatformRepository enginePlatformRepository;

    @Inject
    private EngineRepository engineRepository;

    private MockMvc restEnginePlatformVariableMockMvc;

    private EnginePlatform enginePlatform;

    private Engine engine;

    private EnginePlatformVariable enginePlatformVariable;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EnginePlatformVariableResource enginePlatformVariableResource = new EnginePlatformVariableResource();
        ReflectionTestUtils.setField(enginePlatformVariableResource, "enginePlatformVariableService", enginePlatformVariableService);
        ReflectionTestUtils.setField(enginePlatformVariableResource, "enginePlatformVariableMapper", enginePlatformVariableMapper);
        this.restEnginePlatformVariableMockMvc = MockMvcBuilders.standaloneSetup(enginePlatformVariableResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        enginePlatformVariable = new EnginePlatformVariable();
        enginePlatformVariable.setName(DEFAULT_NAME);
        enginePlatformVariable.setDescription(DEFAULT_DESCRIPTION);
        enginePlatformVariable.setRequired(DEFAULT_REQUIRED);
        enginePlatformVariable.setDefaultValue(DEFAULT_DEFAULT_VALUE);
        engine = new Engine();
        engine.setName("ENGINE1");
        engine.setVersion("alpha");
        engine.setDescription("Engine description");
        engine.setEnabled(true);
        enginePlatform = new EnginePlatform();
        enginePlatform.setName("Engine platform");
        enginePlatform.setVersion("beta");
        enginePlatform.setEnabled(true);
        enginePlatform.setImageName("Image1");
        enginePlatform.setImageType(ImageType.DOCKER);
        enginePlatform.setEngine(engine);
        enginePlatformVariable.setEnginePlatform(enginePlatform);
        engineRepository.save(engine);
        enginePlatformRepository.save(enginePlatform);
    }

    @Test
    @Transactional
    public void createEnginePlatformVariable() throws Exception {

        int databaseSizeBeforeCreate = enginePlatformVariableRepository.findAll().size();

        // Create the EnginePlatformVariable
        EnginePlatformVariableDTO enginePlatformVariableDTO = enginePlatformVariableMapper.enginePlatformVariableToEnginePlatformVariableDTO(enginePlatformVariable);

        restEnginePlatformVariableMockMvc.perform(post("/api/enginePlatformVariables")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformVariableDTO)))
            .andExpect(status().isCreated());

        // Validate the EnginePlatformVariable in the database
        List<EnginePlatformVariable> enginePlatformVariables = enginePlatformVariableRepository.findAll();
        assertThat(enginePlatformVariables).hasSize(databaseSizeBeforeCreate + 1);
        EnginePlatformVariable testEnginePlatformVariable = enginePlatformVariables.get(enginePlatformVariables.size() - 1);
        assertThat(testEnginePlatformVariable.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testEnginePlatformVariable.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testEnginePlatformVariable.getRequired()).isEqualTo(DEFAULT_REQUIRED);
        assertThat(testEnginePlatformVariable.getDefaultValue()).isEqualTo(DEFAULT_DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = enginePlatformVariableRepository.findAll().size();
        // set the field null
        enginePlatformVariable.setName(null);

        // Create the EnginePlatformVariable, which fails.
        EnginePlatformVariableDTO enginePlatformVariableDTO = enginePlatformVariableMapper.enginePlatformVariableToEnginePlatformVariableDTO(enginePlatformVariable);

        restEnginePlatformVariableMockMvc.perform(post("/api/enginePlatformVariables")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformVariableDTO)))
            .andExpect(status().isBadRequest());

        List<EnginePlatformVariable> enginePlatformVariables = enginePlatformVariableRepository.findAll();
        assertThat(enginePlatformVariables).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRequiredIsRequired() throws Exception {
        int databaseSizeBeforeTest = enginePlatformVariableRepository.findAll().size();
        // set the field null
        enginePlatformVariable.setRequired(null);

        // Create the EnginePlatformVariable, which fails.
        EnginePlatformVariableDTO enginePlatformVariableDTO = enginePlatformVariableMapper.enginePlatformVariableToEnginePlatformVariableDTO(enginePlatformVariable);

        restEnginePlatformVariableMockMvc.perform(post("/api/enginePlatformVariables")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformVariableDTO)))
            .andExpect(status().isBadRequest());

        List<EnginePlatformVariable> enginePlatformVariables = enginePlatformVariableRepository.findAll();
        assertThat(enginePlatformVariables).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEnginePlatformVariables() throws Exception {
        // Initialize the database
        enginePlatformVariableRepository.saveAndFlush(enginePlatformVariable);

        // Get all the enginePlatformVariables
        restEnginePlatformVariableMockMvc.perform(get("/api/enginePlatformVariables?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(enginePlatformVariable.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].required").value(hasItem(DEFAULT_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].defaultValue").value(hasItem(DEFAULT_DEFAULT_VALUE.toString())));
    }

    @Test
    @Transactional
    public void getEnginePlatformVariable() throws Exception {
        // Initialize the database
        enginePlatformVariableRepository.saveAndFlush(enginePlatformVariable);

        // Get the enginePlatformVariable
        restEnginePlatformVariableMockMvc.perform(get("/api/enginePlatformVariables/{id}", enginePlatformVariable.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(enginePlatformVariable.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.required").value(DEFAULT_REQUIRED.booleanValue()))
            .andExpect(jsonPath("$.defaultValue").value(DEFAULT_DEFAULT_VALUE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEnginePlatformVariable() throws Exception {
        // Get the enginePlatformVariable
        restEnginePlatformVariableMockMvc.perform(get("/api/enginePlatformVariables/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEnginePlatformVariable() throws Exception {
        // Initialize the database
        enginePlatformVariableRepository.saveAndFlush(enginePlatformVariable);

        int databaseSizeBeforeUpdate = enginePlatformVariableRepository.findAll().size();

        // Update the enginePlatformVariable
        enginePlatformVariable.setName(UPDATED_NAME);
        enginePlatformVariable.setDescription(UPDATED_DESCRIPTION);
        enginePlatformVariable.setRequired(UPDATED_REQUIRED);
        enginePlatformVariable.setDefaultValue(UPDATED_DEFAULT_VALUE);
        EnginePlatformVariableDTO enginePlatformVariableDTO = enginePlatformVariableMapper.enginePlatformVariableToEnginePlatformVariableDTO(enginePlatformVariable);

        restEnginePlatformVariableMockMvc.perform(put("/api/enginePlatformVariables")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(enginePlatformVariableDTO)))
            .andExpect(status().isOk());

        // Validate the EnginePlatformVariable in the database
        List<EnginePlatformVariable> enginePlatformVariables = enginePlatformVariableRepository.findAll();
        assertThat(enginePlatformVariables).hasSize(databaseSizeBeforeUpdate);
        EnginePlatformVariable testEnginePlatformVariable = enginePlatformVariables.get(enginePlatformVariables.size() - 1);
        assertThat(testEnginePlatformVariable.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testEnginePlatformVariable.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testEnginePlatformVariable.getRequired()).isEqualTo(UPDATED_REQUIRED);
        assertThat(testEnginePlatformVariable.getDefaultValue()).isEqualTo(UPDATED_DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void deleteEnginePlatformVariable() throws Exception {
        // Initialize the database
        enginePlatformVariableRepository.saveAndFlush(enginePlatformVariable);

        int databaseSizeBeforeDelete = enginePlatformVariableRepository.findAll().size();

        // Get the enginePlatformVariable
        restEnginePlatformVariableMockMvc.perform(delete("/api/enginePlatformVariables/{id}", enginePlatformVariable.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<EnginePlatformVariable> enginePlatformVariables = enginePlatformVariableRepository.findAll();
        assertThat(enginePlatformVariables).hasSize(databaseSizeBeforeDelete - 1);
    }
}
