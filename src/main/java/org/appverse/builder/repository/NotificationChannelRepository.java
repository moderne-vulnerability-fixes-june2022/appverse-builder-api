package org.appverse.builder.repository;

import org.appverse.builder.domain.NotificationChannel;
import org.appverse.builder.domain.enumeration.NotificationChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the NotificationChannel entity.
 */
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {

    List<NotificationChannel> findByTypeAndEnabledTrue(NotificationChannelType type);

}
