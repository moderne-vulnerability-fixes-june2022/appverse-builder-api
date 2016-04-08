package org.appverse.builder.web.rest.mapper;

import org.appverse.builder.domain.NotificationChannel;
import org.appverse.builder.web.rest.dto.NotificationChannelDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity NotificationChannel and its DTO NotificationChannelDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface NotificationChannelMapper {

    NotificationChannelDTO notificationChannelToNotificationChannelDTO(NotificationChannel notificationChannel);

    NotificationChannel notificationChannelDTOToNotificationChannel(NotificationChannelDTO notificationChannelDTO);
}
