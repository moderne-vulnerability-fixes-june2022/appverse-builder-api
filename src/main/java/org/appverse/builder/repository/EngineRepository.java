package org.appverse.builder.repository;

import org.appverse.builder.domain.Engine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Engine entity.
 */
public interface EngineRepository extends JpaRepository<Engine, Long> {

    Optional<Engine> findByNameAndVersionAndEnabledTrue(String name, String version);
}
