package org.appverse.builder.web.rest;

import org.apache.commons.lang.RandomStringUtils;
import org.appverse.builder.Application;
import org.appverse.builder.domain.Engine;
import org.appverse.builder.domain.EngineVariable;
import org.appverse.builder.repository.EngineRepository;
import org.appverse.builder.repository.EngineVariableRepository;
import org.appverse.builder.service.EngineService;
import org.appverse.builder.web.rest.dto.EngineDTO;
import org.appverse.builder.web.rest.mapper.EngineMapper;
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
 * Test class for the EngineResource REST controller.
 *
 * @see EngineResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class EngineResourceIntTest {

    private static final String DEFAULT_NAME = "AAA";
    private static final String UPDATED_NAME = "BBB";
    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";
    private static final String DEFAULT_VERSION = "AAAAA";
    private static final String UPDATED_VERSION = "BBBBB";

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;

    @Inject
    private EngineRepository engineRepository;

    @Inject
    private EngineMapper engineMapper;

    @Inject
    private EngineService engineService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;
    @Inject
    private EngineVariableRepository engineVariableRepository;

    private MockMvc restEngineMockMvc;

    private Engine engine;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        EngineResource engineResource = new EngineResource();
        ReflectionTestUtils.setField(engineResource, "engineService", engineService);
        ReflectionTestUtils.setField(engineResource, "engineMapper", engineMapper);
        this.restEngineMockMvc = MockMvcBuilders.standaloneSetup(engineResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        engine = new Engine();
        engine.setName(DEFAULT_NAME);
        engine.setDescription(DEFAULT_DESCRIPTION);
        engine.setVersion(DEFAULT_VERSION);
        engine.setEnabled(DEFAULT_ENABLED);
    }

    @Test
    @Transactional
    public void createEngine() throws Exception {
        int databaseSizeBeforeCreate = engineRepository.findAll().size();

        // Create the Engine
        EngineDTO engineDTO = engineMapper.engineToEngineDTO(engine);

        restEngineMockMvc.perform(post("/api/engines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(engineDTO)))
            .andExpect(status().isCreated());

        // Validate the Engine in the database
        List<Engine> engines = engineRepository.findAll();
        assertThat(engines).hasSize(databaseSizeBeforeCreate + 1);
        Engine testEngine = engines.get(engines.size() - 1);
        assertThat(testEngine.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testEngine.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testEngine.getVersion()).isEqualTo(DEFAULT_VERSION);
        assertThat(testEngine.getEnabled()).isEqualTo(DEFAULT_ENABLED);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = engineRepository.findAll().size();
        // set the field null
        engine.setName(null);

        // Create the Engine, which fails.
        EngineDTO engineDTO = engineMapper.engineToEngineDTO(engine);

        restEngineMockMvc.perform(post("/api/engines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(engineDTO)))
            .andExpect(status().isBadRequest());

        List<Engine> engines = engineRepository.findAll();
        assertThat(engines).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkVersionIsRequired() throws Exception {
        int databaseSizeBeforeTest = engineRepository.findAll().size();
        // set the field null
        engine.setVersion(null);

        // Create the Engine, which fails.
        EngineDTO engineDTO = engineMapper.engineToEngineDTO(engine);

        restEngineMockMvc.perform(post("/api/engines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(engineDTO)))
            .andExpect(status().isBadRequest());

        List<Engine> engines = engineRepository.findAll();
        assertThat(engines).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEnabledIsRequired() throws Exception {
        int databaseSizeBeforeTest = engineRepository.findAll().size();
        // set the field null
        engine.setEnabled(null);

        // Create the Engine, which fails.
        EngineDTO engineDTO = engineMapper.engineToEngineDTO(engine);

        restEngineMockMvc.perform(post("/api/engines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(engineDTO)))
            .andExpect(status().isBadRequest());

        List<Engine> engines = engineRepository.findAll();
        assertThat(engines).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllEngines() throws Exception {
        // Initialize the database
        engineRepository.saveAndFlush(engine);

        // Get all the engines
        restEngineMockMvc.perform(get("/api/engines?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(engine.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION.toString())))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED.booleanValue())));
    }

    @Test
    @Transactional
    public void getEngine() throws Exception {
        // Initialize the database
        engineRepository.saveAndFlush(engine);

        // Get the engine
        restEngineMockMvc.perform(get("/api/engines/{id}", engine.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(engine.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION.toString()))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED.booleanValue()));
    }


    @Test
    @Transactional
    public void getEngineVariablesByEngine() throws Exception {
        // Initialize the database
        engineRepository.saveAndFlush(engine);

        EngineVariable engineVariable = new EngineVariable();
        engineVariable.setName(RandomStringUtils.randomAlphabetic(10));
        engineVariable.setDefaultValue(RandomStringUtils.random(10));
        engineVariable.setRequired(true);
        engineVariable.setDescription(RandomStringUtils.random(50));
        engineVariable.setEngine(engine);

        engineVariable = engineVariableRepository.save(engineVariable);


        // Get the engine
        restEngineMockMvc.perform(get("/api/engines/variables/{id}", engine.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[*].id").value(engineVariable.getId().intValue()))
            .andExpect(jsonPath("$[*].name").value(engineVariable.getName()))
            .andExpect(jsonPath("$[*].description").value(engineVariable.getDescription()))
            .andExpect(jsonPath("$[*].defaultValue").value(engineVariable.getDefaultValue()))
            .andExpect(jsonPath("$[*].required").value(engineVariable.getRequired()));
    }

    @Test
    @Transactional
    public void getNonExistingEngine() throws Exception {
        // Get the engine
        restEngineMockMvc.perform(get("/api/engines/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateEngine() throws Exception {
        // Initialize the database
        engineRepository.saveAndFlush(engine);

        int databaseSizeBeforeUpdate = engineRepository.findAll().size();

        // Update the engine
        engine.setName(UPDATED_NAME);
        engine.setDescription(UPDATED_DESCRIPTION);
        engine.setVersion(UPDATED_VERSION);
        engine.setEnabled(UPDATED_ENABLED);
        EngineDTO engineDTO = engineMapper.engineToEngineDTO(engine);

        restEngineMockMvc.perform(put("/api/engines")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(engineDTO)))
            .andExpect(status().isOk());

        // Validate the Engine in the database
        List<Engine> engines = engineRepository.findAll();
        assertThat(engines).hasSize(databaseSizeBeforeUpdate);
        Engine testEngine = engines.get(engines.size() - 1);
        assertThat(testEngine.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testEngine.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testEngine.getVersion()).isEqualTo(UPDATED_VERSION);
        assertThat(testEngine.getEnabled()).isEqualTo(UPDATED_ENABLED);
    }

    @Test
    @Transactional
    public void deleteEngine() throws Exception {
        // Initialize the database
        engineRepository.saveAndFlush(engine);

        int databaseSizeBeforeDelete = engineRepository.findAll().size();

        // Get the engine
        restEngineMockMvc.perform(delete("/api/engines/{id}", engine.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Engine> engines = engineRepository.findAll();
        assertThat(engines).hasSize(databaseSizeBeforeDelete - 1);
    }
}
