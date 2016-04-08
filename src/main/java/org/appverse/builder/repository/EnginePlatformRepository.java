package org.appverse.builder.repository;

import org.appverse.builder.domain.EnginePlatform;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the EnginePlatform entity.
 */
public interface EnginePlatformRepository extends JpaRepository<EnginePlatform, Long> {

    EnginePlatform findByEngineNameAndNameAndVersionAndEnabledTrue(String engineName, String platformName, String version);
}
