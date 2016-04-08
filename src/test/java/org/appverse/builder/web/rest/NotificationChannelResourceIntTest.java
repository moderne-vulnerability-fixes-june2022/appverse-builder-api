package org.appverse.builder.web.rest;

import org.appverse.builder.Application;
import org.appverse.builder.domain.NotificationChannel;
import org.appverse.builder.domain.enumeration.NotificationChannelType;
import org.appverse.builder.repository.NotificationChannelRepository;
import org.appverse.builder.service.NotificationChannelService;
import org.appverse.builder.web.rest.dto.NotificationChannelDTO;
import org.appverse.builder.web.rest.mapper.NotificationChannelMapper;
import org.assertj.core.api.Assertions;
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
 * Test class for the NotificationChannelResource REST controller.
 *
 * @see NotificationChannelResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class NotificationChannelResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";
    private static final String DEFAULT_DESCRIPTION = "AAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBB";

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;


    private static final NotificationChannelType DEFAULT_TYPE = NotificationChannelType.EMAIL;
    private static final NotificationChannelType UPDATED_TYPE = NotificationChannelType.EMAIL;

    @Inject
    private NotificationChannelRepository notificationChannelRepository;

    @Inject
    private NotificationChannelMapper notificationChannelMapper;

    @Inject
    private NotificationChannelService notificationChannelService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restNotificationChannelMockMvc;

    private NotificationChannel notificationChannel;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        NotificationChannelResource notificationChannelResource = new NotificationChannelResource();
        ReflectionTestUtils.setField(notificationChannelResource, "notificationChannelService", notificationChannelService);
        ReflectionTestUtils.setField(notificationChannelResource, "notificationChannelMapper", notificationChannelMapper);
        this.restNotificationChannelMockMvc = MockMvcBuilders.standaloneSetup(notificationChannelResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        notificationChannel = new NotificationChannel();
        notificationChannel.setName(DEFAULT_NAME);
        notificationChannel.setDescription(DEFAULT_DESCRIPTION);
        notificationChannel.setEnabled(DEFAULT_ENABLED);
        notificationChannel.setType(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    public void createNotificationChannel() throws Exception {
        int databaseSizeBeforeCreate = notificationChannelRepository.findAll().size();

        // Create the NotificationChannel
        NotificationChannelDTO notificationChannelDTO = notificationChannelMapper.notificationChannelToNotificationChannelDTO(notificationChannel);

        restNotificationChannelMockMvc.perform(post("/api/notificationChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(notificationChannelDTO)))
            .andExpect(status().isCreated());

        // Validate the NotificationChannel in the database
        List<NotificationChannel> notificationChannels = notificationChannelRepository.findAll();
        assertThat(notificationChannels).hasSize(databaseSizeBeforeCreate + 1);
        NotificationChannel testNotificationChannel = notificationChannels.get(notificationChannels.size() - 1);
        assertThat(testNotificationChannel.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testNotificationChannel.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testNotificationChannel.getEnabled()).isEqualTo(DEFAULT_ENABLED);
        Assertions.assertThat(testNotificationChannel.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = notificationChannelRepository.findAll().size();
        // set the field null
        notificationChannel.setName(null);

        // Create the NotificationChannel, which fails.
        NotificationChannelDTO notificationChannelDTO = notificationChannelMapper.notificationChannelToNotificationChannelDTO(notificationChannel);

        restNotificationChannelMockMvc.perform(post("/api/notificationChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(notificationChannelDTO)))
            .andExpect(status().isBadRequest());

        List<NotificationChannel> notificationChannels = notificationChannelRepository.findAll();
        assertThat(notificationChannels).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkEnabledIsRequired() throws Exception {
        int databaseSizeBeforeTest = notificationChannelRepository.findAll().size();
        // set the field null
        notificationChannel.setEnabled(null);

        // Create the NotificationChannel, which fails.
        NotificationChannelDTO notificationChannelDTO = notificationChannelMapper.notificationChannelToNotificationChannelDTO(notificationChannel);

        restNotificationChannelMockMvc.perform(post("/api/notificationChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(notificationChannelDTO)))
            .andExpect(status().isBadRequest());

        List<NotificationChannel> notificationChannels = notificationChannelRepository.findAll();
        assertThat(notificationChannels).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = notificationChannelRepository.findAll().size();
        // set the field null
        notificationChannel.setType(null);

        // Create the NotificationChannel, which fails.
        NotificationChannelDTO notificationChannelDTO = notificationChannelMapper.notificationChannelToNotificationChannelDTO(notificationChannel);

        restNotificationChannelMockMvc.perform(post("/api/notificationChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(notificationChannelDTO)))
            .andExpect(status().isBadRequest());

        List<NotificationChannel> notificationChannels = notificationChannelRepository.findAll();
        assertThat(notificationChannels).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllNotificationChannels() throws Exception {
        // Initialize the database
        notificationChannelRepository.saveAndFlush(notificationChannel);

        // Get all the notificationChannels
        restNotificationChannelMockMvc.perform(get("/api/notificationChannels?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(notificationChannel.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED.booleanValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    public void getNotificationChannel() throws Exception {
        // Initialize the database
        notificationChannelRepository.saveAndFlush(notificationChannel);

        // Get the notificationChannel
        restNotificationChannelMockMvc.perform(get("/api/notificationChannels/{id}", notificationChannel.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(notificationChannel.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED.booleanValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingNotificationChannel() throws Exception {
        // Get the notificationChannel
        restNotificationChannelMockMvc.perform(get("/api/notificationChannels/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateNotificationChannel() throws Exception {
        // Initialize the database
        notificationChannelRepository.saveAndFlush(notificationChannel);

        int databaseSizeBeforeUpdate = notificationChannelRepository.findAll().size();

        // Update the notificationChannel
        notificationChannel.setName(UPDATED_NAME);
        notificationChannel.setDescription(UPDATED_DESCRIPTION);
        notificationChannel.setEnabled(UPDATED_ENABLED);
        notificationChannel.setType(UPDATED_TYPE);
        NotificationChannelDTO notificationChannelDTO = notificationChannelMapper.notificationChannelToNotificationChannelDTO(notificationChannel);

        restNotificationChannelMockMvc.perform(put("/api/notificationChannels")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(notificationChannelDTO)))
            .andExpect(status().isOk());

        // Validate the NotificationChannel in the database
        List<NotificationChannel> notificationChannels = notificationChannelRepository.findAll();
        assertThat(notificationChannels).hasSize(databaseSizeBeforeUpdate);
        NotificationChannel testNotificationChannel = notificationChannels.get(notificationChannels.size() - 1);
        assertThat(testNotificationChannel.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testNotificationChannel.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testNotificationChannel.getEnabled()).isEqualTo(UPDATED_ENABLED);
        Assertions.assertThat(testNotificationChannel.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void deleteNotificationChannel() throws Exception {
        // Initialize the database
        notificationChannelRepository.saveAndFlush(notificationChannel);

        int databaseSizeBeforeDelete = notificationChannelRepository.findAll().size();

        // Get the notificationChannel
        restNotificationChannelMockMvc.perform(delete("/api/notificationChannels/{id}", notificationChannel.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<NotificationChannel> notificationChannels = notificationChannelRepository.findAll();
        assertThat(notificationChannels).hasSize(databaseSizeBeforeDelete - 1);
    }
}
