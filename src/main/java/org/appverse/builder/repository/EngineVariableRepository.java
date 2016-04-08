package org.appverse.builder.repository;

import org.appverse.builder.domain.EngineVariable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the EngineVariable entity.
 */
public interface EngineVariableRepository extends JpaRepository<EngineVariable, Long> {

    List<EngineVariable> findByEngineId(Long engineId);
}
