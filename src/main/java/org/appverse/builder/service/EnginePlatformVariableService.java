package org.appverse.builder.service;

import org.appverse.builder.domain.EnginePlatformVariable;
import org.appverse.builder.web.rest.dto.EnginePlatformVariableDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service Interface for managing EnginePlatformVariable.
 */
public interface EnginePlatformVariableService {

    /**
     * Save a enginePlatformVariable.
     *
     * @return the persisted entity
     */
    public EnginePlatformVariableDTO save(EnginePlatformVariableDTO enginePlatformVariableDTO);

    /**
     * get all the enginePlatformVariables.
     *
     * @return the list of entities
     */
    public Page<EnginePlatformVariable> findAll(Pageable pageable);

    /**
     * get the "id" enginePlatformVariable.
     *
     * @return the entity
     */
    public EnginePlatformVariableDTO findOne(Long id);

    /**
     * delete the "id" enginePlatformVariable.
     */
    public void delete(Long id);


    /**
     * @param id
     * @return
     */
    List<EnginePlatformVariableDTO> findByEnginePlatformId(Long id);
}
