package org.appverse.builder.web.rest;

import org.appverse.builder.Application;
import org.appverse.builder.domain.BuildAgent;
import org.appverse.builder.repository.BuildAgentRepository;
import org.appverse.builder.service.BuildAgentQueueService;
import org.appverse.builder.service.BuildAgentService;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.mapper.BuildAgentMapper;
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
 * Test class for the BuildAgentResource REST controller.
 *
 * @see BuildAgentResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class BuildAgentResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";
    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;

    @Inject
    private BuildAgentRepository buildAgentRepository;

    @Inject
    private BuildAgentMapper buildAgentMapper;

    @Inject
    private BuildAgentService buildAgentService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private BuildAgentQueueService buildAgentQueueService;

    private MockMvc restBuildAgentMockMvc;

    private BuildAgent buildAgent;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        BuildAgentResource buildAgentResource = new BuildAgentResource();
        ReflectionTestUtils.setField(buildAgentResource, "buildAgentService", buildAgentService);
        ReflectionTestUtils.setField(buildAgentResource, "buildAgentMapper", buildAgentMapper);
        ReflectionTestUtils.setField(buildAgentResource, "buildAgentQueueService", buildAgentQueueService);
        this.restBuildAgentMockMvc = MockMvcBuilders.standaloneSetup(buildAgentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        buildAgent = new BuildAgent();
        buildAgent.setName(DEFAULT_NAME);
        buildAgent.setDescription(DEFAULT_DESCRIPTION);
        buildAgent.setEnabled(DEFAULT_ENABLED);
    }

    @Test
    @Transactional
    public void createBuildAgent() throws Exception {
        int databaseSizeBeforeCreate = buildAgentRepository.findAll().size();

        // Create the BuildAgent
        BuildAgentDTO buildAgentDTO = buildAgentMapper.buildAgentToBuildAgentDTO(buildAgent);

        restBuildAgentMockMvc.perform(post("/api/buildAgents")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildAgentDTO)))
            .andExpect(status().isCreated());

        // Validate the BuildAgent in the database
        List<BuildAgent> buildAgents = buildAgentRepository.findAll();
        assertThat(buildAgents).hasSize(databaseSizeBeforeCreate + 1);
        BuildAgent testBuildAgent = buildAgents.get(buildAgents.size() - 1);
        assertThat(testBuildAgent.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBuildAgent.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testBuildAgent.getEnabled()).isEqualTo(DEFAULT_ENABLED);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = buildAgentRepository.findAll().size();
        // set the field null
        buildAgent.setName(null);

        // Create the BuildAgent, which fails.
        BuildAgentDTO buildAgentDTO = buildAgentMapper.buildAgentToBuildAgentDTO(buildAgent);

        restBuildAgentMockMvc.perform(post("/api/buildAgents")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildAgentDTO)))
            .andExpect(status().isBadRequest());

        List<BuildAgent> buildAgents = buildAgentRepository.findAll();
        assertThat(buildAgents).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEnabledIsRequired() throws Exception {
        int databaseSizeBeforeTest = buildAgentRepository.findAll().size();
        // set the field null
        buildAgent.setEnabled(null);

        // Create the BuildAgent, which fails.
        BuildAgentDTO buildAgentDTO = buildAgentMapper.buildAgentToBuildAgentDTO(buildAgent);

        restBuildAgentMockMvc.perform(post("/api/buildAgents")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildAgentDTO)))
            .andExpect(status().isBadRequest());

        List<BuildAgent> buildAgents = buildAgentRepository.findAll();
        assertThat(buildAgents).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllBuildAgents() throws Exception {
        // Initialize the database
        buildAgentRepository.saveAndFlush(buildAgent);

        // Get all the buildAgents
        restBuildAgentMockMvc.perform(get("/api/buildAgents?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(buildAgent.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED.booleanValue())));
    }

    @Test
    @Transactional
    public void getBuildAgent() throws Exception {
        // Initialize the database
        buildAgentRepository.saveAndFlush(buildAgent);

        // Get the buildAgent
        restBuildAgentMockMvc.perform(get("/api/buildAgents/{id}", buildAgent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(buildAgent.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingBuildAgent() throws Exception {
        // Get the buildAgent
        restBuildAgentMockMvc.perform(get("/api/buildAgents/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBuildAgent() throws Exception {
        // Initialize the database
        buildAgentRepository.saveAndFlush(buildAgent);

        int databaseSizeBeforeUpdate = buildAgentRepository.findAll().size();

        // Update the buildAgent
        buildAgent.setName(UPDATED_NAME);
        buildAgent.setDescription(UPDATED_DESCRIPTION);
        buildAgent.setEnabled(UPDATED_ENABLED);
        BuildAgentDTO buildAgentDTO = buildAgentMapper.buildAgentToBuildAgentDTO(buildAgent);

        restBuildAgentMockMvc.perform(put("/api/buildAgents")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(buildAgentDTO)))
            .andExpect(status().isOk());

        // Validate the BuildAgent in the database
        List<BuildAgent> buildAgents = buildAgentRepository.findAll();
        assertThat(buildAgents).hasSize(databaseSizeBeforeUpdate);
        BuildAgent testBuildAgent = buildAgents.get(buildAgents.size() - 1);
        assertThat(testBuildAgent.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBuildAgent.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testBuildAgent.getEnabled()).isEqualTo(UPDATED_ENABLED);
    }

    @Test
    @Transactional
    public void deleteBuildAgent() throws Exception {
        // Initialize the database
        buildAgentRepository.saveAndFlush(buildAgent);

        int databaseSizeBeforeDelete = buildAgentRepository.findAll().size();

        // Get the buildAgent
        restBuildAgentMockMvc.perform(delete("/api/buildAgents/{id}", buildAgent.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<BuildAgent> buildAgents = buildAgentRepository.findAll();
        assertThat(buildAgents).hasSize(databaseSizeBeforeDelete - 1);
    }
}
