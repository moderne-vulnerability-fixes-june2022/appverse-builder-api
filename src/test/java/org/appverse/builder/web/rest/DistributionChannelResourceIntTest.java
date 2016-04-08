package org.appverse.builder.web.rest;

import org.appverse.builder.Application;
import org.appverse.builder.domain.DistributionChannel;
import org.appverse.builder.domain.enumeration.DistributionChannelType;
import org.appverse.builder.repository.DistributionChannelRepository;
import org.appverse.builder.service.DistributionChannelService;
import org.appverse.builder.web.rest.dto.DistributionChannelDTO;
import org.appverse.builder.web.rest.mapper.DistributionChannelMapper;
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
 * Test class for the DistributionChannelResource REST controller.
 *
 * @see DistributionChannelResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class DistributionChannelResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";
    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;


    private static final DistributionChannelType DEFAULT_TYPE = DistributionChannelType.FILESYSTEM;
    private static final DistributionChannelType UPDATED_TYPE = DistributionChannelType.FILESYSTEM;

    @Inject
    private DistributionChannelRepository distributionChannelRepository;

    @Inject
    private DistributionChannelMapper distributionChannelMapper;

    @Inject
    private DistributionChannelService distributionChannelService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restDistributionChannelMockMvc;

    private DistributionChannel distributionChannel;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DistributionChannelResource distributionChannelResource = new DistributionChannelResource();
        ReflectionTestUtils.setField(distributionChannelResource, "distributionChannelService", distributionChannelService);
        ReflectionTestUtils.setField(distributionChannelResource, "distributionChannelMapper", distributionChannelMapper);
        this.restDistributionChannelMockMvc = MockMvcBuilders.standaloneSetup(distributionChannelResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        distributionChannel = new DistributionChannel();
        distributionChannel.setName(DEFAULT_NAME);
        distributionChannel.setDescription(DEFAULT_DESCRIPTION);
        distributionChannel.setEnabled(DEFAULT_ENABLED);
        distributionChannel.setType(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    public void createDistributionChannel() throws Exception {
        int databaseSizeBeforeCreate = distributionChannelRepository.findAll().size();

        // Create the DistributionChannel
        DistributionChannelDTO distributionChannelDTO = distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel);

        restDistributionChannelMockMvc.perform(post("/api/distributionChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(distributionChannelDTO)))
            .andExpect(status().isCreated());

        // Validate the DistributionChannel in the database
        List<DistributionChannel> distributionChannels = distributionChannelRepository.findAll();
        assertThat(distributionChannels).hasSize(databaseSizeBeforeCreate + 1);
        DistributionChannel testDistributionChannel = distributionChannels.get(distributionChannels.size() - 1);
        assertThat(testDistributionChannel.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testDistributionChannel.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testDistributionChannel.getEnabled()).isEqualTo(DEFAULT_ENABLED);
        assertThat(testDistributionChannel.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = distributionChannelRepository.findAll().size();
        // set the field null
        distributionChannel.setName(null);

        // Create the DistributionChannel, which fails.
        DistributionChannelDTO distributionChannelDTO = distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel);

        restDistributionChannelMockMvc.perform(post("/api/distributionChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(distributionChannelDTO)))
            .andExpect(status().isBadRequest());

        List<DistributionChannel> distributionChannels = distributionChannelRepository.findAll();
        assertThat(distributionChannels).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEnabledIsRequired() throws Exception {
        int databaseSizeBeforeTest = distributionChannelRepository.findAll().size();
        // set the field null
        distributionChannel.setEnabled(null);

        // Create the DistributionChannel, which fails.
        DistributionChannelDTO distributionChannelDTO = distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel);

        restDistributionChannelMockMvc.perform(post("/api/distributionChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(distributionChannelDTO)))
            .andExpect(status().isBadRequest());

        List<DistributionChannel> distributionChannels = distributionChannelRepository.findAll();
        assertThat(distributionChannels).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = distributionChannelRepository.findAll().size();
        // set the field null
        distributionChannel.setType(null);

        // Create the DistributionChannel, which fails.
        DistributionChannelDTO distributionChannelDTO = distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel);

        restDistributionChannelMockMvc.perform(post("/api/distributionChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(distributionChannelDTO)))
            .andExpect(status().isBadRequest());

        List<DistributionChannel> distributionChannels = distributionChannelRepository.findAll();
        assertThat(distributionChannels).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllDistributionChannels() throws Exception {
        // Initialize the database
        distributionChannelRepository.saveAndFlush(distributionChannel);

        // Get all the distributionChannels
        restDistributionChannelMockMvc.perform(get("/api/distributionChannels?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(distributionChannel.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED.booleanValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getDistributionChannel() throws Exception {
        // Initialize the database
        distributionChannelRepository.saveAndFlush(distributionChannel);

        // Get the distributionChannel
        restDistributionChannelMockMvc.perform(get("/api/distributionChannels/{id}", distributionChannel.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(distributionChannel.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED.booleanValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingDistributionChannel() throws Exception {
        // Get the distributionChannel
        restDistributionChannelMockMvc.perform(get("/api/distributionChannels/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDistributionChannel() throws Exception {
        // Initialize the database
        distributionChannelRepository.saveAndFlush(distributionChannel);

        int databaseSizeBeforeUpdate = distributionChannelRepository.findAll().size();

        // Update the distributionChannel
        distributionChannel.setName(UPDATED_NAME);
        distributionChannel.setDescription(UPDATED_DESCRIPTION);
        distributionChannel.setEnabled(UPDATED_ENABLED);
        distributionChannel.setType(UPDATED_TYPE);
        DistributionChannelDTO distributionChannelDTO = distributionChannelMapper.distributionChannelToDistributionChannelDTO(distributionChannel);

        restDistributionChannelMockMvc.perform(put("/api/distributionChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(distributionChannelDTO)))
            .andExpect(status().isOk());

        // Validate the DistributionChannel in the database
        List<DistributionChannel> distributionChannels = distributionChannelRepository.findAll();
        assertThat(distributionChannels).hasSize(databaseSizeBeforeUpdate);
        DistributionChannel testDistributionChannel = distributionChannels.get(distributionChannels.size() - 1);
        assertThat(testDistributionChannel.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testDistributionChannel.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testDistributionChannel.getEnabled()).isEqualTo(UPDATED_ENABLED);
        assertThat(testDistributionChannel.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void deleteDistributionChannel() throws Exception {
        // Initialize the database
        distributionChannelRepository.saveAndFlush(distributionChannel);

        int databaseSizeBeforeDelete = distributionChannelRepository.findAll().size();

        // Get the distributionChannel
        restDistributionChannelMockMvc.perform(delete("/api/distributionChannels/{id}", distributionChannel.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<DistributionChannel> distributionChannels = distributionChannelRepository.findAll();
        assertThat(distributionChannels).hasSize(databaseSizeBeforeDelete - 1);
    }
}
