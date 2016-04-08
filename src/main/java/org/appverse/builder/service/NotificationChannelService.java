package org.appverse.builder.service;

import org.appverse.builder.domain.NotificationChannel;
import org.appverse.builder.notification.Notification;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.dto.NotificationChannelDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

/**
 * Service Interface for managing NotificationChannel.
 */
public interface NotificationChannelService {

    /**
     * Save a notificationChannel.
     *
     * @return the persisted entity
     */
    public NotificationChannelDTO save(NotificationChannelDTO notificationChannelDTO);

    /**
     * get all the notificationChannels.
     *
     * @return the list of entities
     */
    public Page<NotificationChannel> findAll(Pageable pageable);

    /**
     * get the "id" notificationChannel.
     *
     * @return the entity
     */
    public NotificationChannelDTO findOne(Long id);

    /**
     * delete the "id" notificationChannel.
     */
    public void delete(Long id);

    @Async
    void sendNotificationToAllChannels(Notification notification);

    Notification buildBuildRequestNotification(BuildRequestDTO buildRequestDTO, Notification.Event event);
}
