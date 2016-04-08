package org.appverse.builder.web.rest;

import org.appverse.builder.Application;
import org.appverse.builder.domain.Engine;
import org.appverse.builder.domain.EngineVariable;
import org.appverse.builder.repository.EngineRepository;
import org.appverse.builder.repository.EngineVariableRepository;
import org.appverse.builder.service.EngineVariableService;
import org.appverse.builder.web.rest.dto.EngineVariableDTO;
import org.appverse.builder.web.rest.mapper.EngineVariableMapper;
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
 * Test class for the EngineVariableResource REST controller.
 *
 * @see EngineVariableResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class EngineVariableResourceIntTest {

    private static final String DEFAULT_NAME = "AA";
    private static final String UPDATED_NAME = "BB";
    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";

    private static final Boolean DEFAULT_REQUIRED = false;
    private static final Boolean UPDATED_REQUIRED = true;
    private static final String DEFAULT_DEFAULT_VALUE = "AAAAA";
    private static final String UPDATED_DEFAULT_VALUE = "BBBBB";

    @Inject
    private EngineVariableRepository engineVariableRepository;

    @Inject
    private EngineRepository engineRepository;

    @Inject
    private EngineVariableMapper engineVariableMapper;

    @Inject
    private EngineVariableService engineVariableService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restEngineVariableMockMvc;

    private EngineVariable engineVariable;

    private Engine engine;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EngineVariableResource engineVariableResource = new EngineVariableResource();
        ReflectionTestUtils.setField(engineVariableResource, "engineVariableService", engineVariableService);
        ReflectionTestUtils.setField(engineVariableResource, "engineVariableMapper", engineVariableMapper);
        this.restEngineVariableMockMvc = MockMvcBuilders.standaloneSetup(engineVariableResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        engine = new Engine();
        engine.setEnabled(true);
        engine.setName("Engine1");
        engine.setDescription("Engine description");
        engine.setVersion("alpha");
        engineRepository.save(engine);
        engineVariable = new EngineVariable();
        engineVariable.setName(DEFAULT_NAME);
        engineVariable.setDescription(DEFAULT_DESCRIPTION);
        engineVariable.setRequired(DEFAULT_REQUIRED);
        engineVariable.setDefaultValue(DEFAULT_DEFAULT_VALUE);
        engineVariable.setEngine(engine);
    }

    @Test
    @Transactional
    public void createEngineVariable() throws Exception {
        int databaseSizeBeforeCreate = engineVariableRepository.findAll().size();

        // Create the EngineVariable
        EngineVariableDTO engineVariableDTO = engineVariableMapper.engineVariableToEngineVariableDTO(engineVariable);

        restEngineVariableMockMvc.perform(post("/api/engineVariables")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(engineVariableDTO)))
            .andExpect(status().isCreated());

        // Validate the EngineVariable in the database
        List<EngineVariable> engineVariables = engineVariableRepository.findAll();
        assertThat(engineVariables).hasSize(databaseSizeBeforeCreate + 1);
        EngineVariable testEngineVariable = engineVariables.get(engineVariables.size() - 1);
        assertThat(testEngineVariable.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testEngineVariable.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testEngineVariable.getRequired()).isEqualTo(DEFAULT_REQUIRED);
        assertThat(testEngineVariable.getDefaultValue()).isEqualTo(DEFAULT_DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = engineVariableRepository.findAll().size();
        // set the field null
        engineVariable.setName(null);

        // Create the EngineVariable, which fails.
        EngineVariableDTO engineVariableDTO = engineVariableMapper.engineVariableToEngineVariableDTO(engineVariable);

        restEngineVariableMockMvc.perform(post("/api/engineVariables")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(engineVariableDTO)))
            .andExpect(status().isBadRequest());

        List<EngineVariable> engineVariables = engineVariableRepository.findAll();
        assertThat(engineVariables).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkRequiredIsRequired() throws Exception {
        int databaseSizeBeforeTest = engineVariableRepository.findAll().size();
        // set the field null
        engineVariable.setRequired(null);

        // Create the EngineVariable, which fails.
        EngineVariableDTO engineVariableDTO = engineVariableMapper.engineVariableToEngineVariableDTO(engineVariable);

        restEngineVariableMockMvc.perform(post("/api/engineVariables")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(engineVariableDTO)))
            .andExpect(status().isBadRequest());

        List<EngineVariable> engineVariables = engineVariableRepository.findAll();
        assertThat(engineVariables).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEngineVariables() throws Exception {
        // Initialize the database
        engineVariableRepository.saveAndFlush(engineVariable);

        // Get all the engineVariables
        restEngineVariableMockMvc.perform(get("/api/engineVariables?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(engineVariable.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].required").value(hasItem(DEFAULT_REQUIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].defaultValue").value(hasItem(DEFAULT_DEFAULT_VALUE.toString())));
    }

    @Test
    @Transactional
    public void getEngineVariable() throws Exception {
        // Initialize the database
        engineVariableRepository.saveAndFlush(engineVariable);

        // Get the engineVariable
        restEngineVariableMockMvc.perform(get("/api/engineVariables/{id}", engineVariable.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(engineVariable.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.required").value(DEFAULT_REQUIRED.booleanValue()))
            .andExpect(jsonPath("$.defaultValue").value(DEFAULT_DEFAULT_VALUE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingEngineVariable() throws Exception {
        // Get the engineVariable
        restEngineVariableMockMvc.perform(get("/api/engineVariables/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEngineVariable() throws Exception {
        // Initialize the database
        engineVariableRepository.saveAndFlush(engineVariable);

        int databaseSizeBeforeUpdate = engineVariableRepository.findAll().size();

        // Update the engineVariable
        engineVariable.setName(UPDATED_NAME);
        engineVariable.setDescription(UPDATED_DESCRIPTION);
        engineVariable.setRequired(UPDATED_REQUIRED);
        engineVariable.setDefaultValue(UPDATED_DEFAULT_VALUE);
        EngineVariableDTO engineVariableDTO = engineVariableMapper.engineVariableToEngineVariableDTO(engineVariable);

        restEngineVariableMockMvc.perform(put("/api/engineVariables")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(engineVariableDTO)))
            .andExpect(status().isOk());

        // Validate the EngineVariable in the database
        List<EngineVariable> engineVariables = engineVariableRepository.findAll();
        assertThat(engineVariables).hasSize(databaseSizeBeforeUpdate);
        EngineVariable testEngineVariable = engineVariables.get(engineVariables.size() - 1);
        assertThat(testEngineVariable.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testEngineVariable.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testEngineVariable.getRequired()).isEqualTo(UPDATED_REQUIRED);
        assertThat(testEngineVariable.getDefaultValue()).isEqualTo(UPDATED_DEFAULT_VALUE);
    }

    @Test
    @Transactional
    public void deleteEngineVariable() throws Exception {
        // Initialize the database
        engineVariableRepository.saveAndFlush(engineVariable);

        int databaseSizeBeforeDelete = engineVariableRepository.findAll().size();

        // Get the engineVariable
        restEngineVariableMockMvc.perform(delete("/api/engineVariables/{id}", engineVariable.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<EngineVariable> engineVariables = engineVariableRepository.findAll();
        assertThat(engineVariables).hasSize(databaseSizeBeforeDelete - 1);
    }
}
