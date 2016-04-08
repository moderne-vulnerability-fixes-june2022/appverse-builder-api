package org.appverse.builder.repository;

import org.appverse.builder.domain.DistributionChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the DistributionChannel entity.
 */
public interface DistributionChannelRepository extends JpaRepository<DistributionChannel, Long> {

    List<DistributionChannel> findByEnabledTrue();
}
