package org.appverse.builder.service.impl;

import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.domain.NotificationChannel;
import org.appverse.builder.notification.Notification;
import org.appverse.builder.notification.NotificationSender;
import org.appverse.builder.repository.NotificationChannelRepository;
import org.appverse.builder.service.DistributionChannelService;
import org.appverse.builder.service.NotificationChannelService;
import org.appverse.builder.service.UserService;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.dto.NotificationChannelDTO;
import org.appverse.builder.web.rest.mapper.NotificationChannelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Set;

/**
 * Service Implementation for managing NotificationChannel.
 */
@Service
@Transactional
public class NotificationChannelServiceImpl implements NotificationChannelService {

    private final Logger log = LoggerFactory.getLogger(NotificationChannelServiceImpl.class);

    @Inject
    private NotificationChannelRepository notificationChannelRepository;

    @Inject
    private NotificationChannelMapper notificationChannelMapper;

    @Autowired(required = false)
    private Set<NotificationSender> notificationSenders;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private UserService userService;

    @Inject
    private DistributionChannelService distributionChannelService;

    /**
     * Save a notificationChannel.
     *
     * @return the persisted entity
     */
    public NotificationChannelDTO save(NotificationChannelDTO notificationChannelDTO) {
        log.debug("Request to save NotificationChannel : {}", notificationChannelDTO);
        NotificationChannel notificationChannel = notificationChannelMapper.notificationChannelDTOToNotificationChannel(notificationChannelDTO);
        notificationChannel = notificationChannelRepository.save(notificationChannel);
        NotificationChannelDTO result = notificationChannelMapper.notificationChannelToNotificationChannelDTO(notificationChannel);
        return result;
    }

    /**
     * get all the notificationChannels.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<NotificationChannel> findAll(Pageable pageable) {
        log.debug("Request to get all NotificationChannels");
        Page<NotificationChannel> result = notificationChannelRepository.findAll(pageable);
        return result;
    }

    /**
     * get one notificationChannel by id.
     *
     * @return the entity
     */
    @Transactional(readOnly = true)
    public NotificationChannelDTO findOne(Long id) {
        log.debug("Request to get NotificationChannel : {}", id);
        NotificationChannel notificationChannel = notificationChannelRepository.findOne(id);
        NotificationChannelDTO notificationChannelDTO = notificationChannelMapper.notificationChannelToNotificationChannelDTO(notificationChannel);
        return notificationChannelDTO;
    }

    /**
     * delete the  notificationChannel by id.
     */
    public void delete(Long id) {
        log.debug("Request to delete NotificationChannel : {}", id);
        notificationChannelRepository.delete(id);
    }

    @Override
    public void sendNotificationToAllChannels(Notification notification) {
        if (notificationSenders != null && !notificationSenders.isEmpty()) {
            notificationSenders.forEach(notificationSender -> notificationChannelRepository.findByTypeAndEnabledTrue(notificationSender.getSupportedNotificationChannelType()).forEach(notificationChannel -> {
                notificationSender.sendNotification(notification, notificationChannelMapper.notificationChannelToNotificationChannelDTO(notificationChannel));
            }));
        }
    }

    @Override
    public Notification buildBuildRequestNotification(BuildRequestDTO buildRequest, Notification.Event event) {
        Notification notification = new Notification();
        notification.setEvent(event);
        userService.findByLogin(buildRequest.getRequesterLogin()).ifPresent(notification::setUser);
        notification.setTitle("[Notification] Build Request #" + buildRequest.getId());
        notification.getModelMap().put("baseUrl", appverseBuilderProperties.getBaseUrl());
        notification.getModelMap().put("request", buildRequest);
        switch (event) {
            case BUILD_REQUEST_FINISHED:
            case BUILD_REQUEST_FAILED:
                notification.getModelMap().put("artifacts", distributionChannelService.getRequestArtifacts(buildRequest, true));

        }
        notification.getModelMap().putAll(buildRequest.getVariables());
        return notification;
    }
}
