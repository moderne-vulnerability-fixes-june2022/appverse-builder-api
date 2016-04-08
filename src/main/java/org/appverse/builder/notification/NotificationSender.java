package org.appverse.builder.notification;

import org.appverse.builder.domain.enumeration.NotificationChannelType;
import org.appverse.builder.web.rest.dto.NotificationChannelDTO;

/**
 * Created by panthro on 01/02/16.
 */
public interface NotificationSender {


    /**
     * get the supported NotificationChannelType
     *
     * @return
     */
    NotificationChannelType getSupportedNotificationChannelType();


    /**
     * Sends the notification to the user using the given NotificationChannel
     *
     * @param notification
     * @param notificationChannel
     */
    void sendNotification(Notification notification, NotificationChannelDTO notificationChannel);
}
