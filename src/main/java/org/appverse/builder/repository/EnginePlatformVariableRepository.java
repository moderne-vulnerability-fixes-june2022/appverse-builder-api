package org.appverse.builder.repository;

import org.appverse.builder.domain.EnginePlatformVariable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the EnginePlatformVariable entity.
 */
public interface EnginePlatformVariableRepository extends JpaRepository<EnginePlatformVariable, Long> {

    List<EnginePlatformVariable> findByEnginePlatformId(Long id);
}
