package org.appverse.builder.service;

import org.appverse.builder.domain.EngineVariable;
import org.appverse.builder.web.rest.dto.EngineVariableDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service Interface for managing EngineVariable.
 */
public interface EngineVariableService {

    /**
     * Save a engineVariable.
     *
     * @return the persisted entity
     */
    public EngineVariableDTO save(EngineVariableDTO engineVariableDTO);

    /**
     * get all the engineVariables.
     *
     * @return the list of entities
     */
    public Page<EngineVariable> findAll(Pageable pageable);

    /**
     * get the "id" engineVariable.
     *
     * @return the entity
     */
    public EngineVariableDTO findOne(Long id);

    /**
     * delete the "id" engineVariable.
     */
    public void delete(Long id);

    List<EngineVariableDTO> findByEngineId(Long engineId);
}
