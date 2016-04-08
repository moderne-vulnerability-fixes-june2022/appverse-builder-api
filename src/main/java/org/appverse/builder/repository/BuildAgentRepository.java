package org.appverse.builder.repository;

import org.appverse.builder.domain.BuildAgent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the BuildAgent entity.
 */
public interface BuildAgentRepository extends JpaRepository<BuildAgent, Long> {

}
