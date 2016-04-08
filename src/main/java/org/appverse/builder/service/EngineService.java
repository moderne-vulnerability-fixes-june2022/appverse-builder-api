package org.appverse.builder.service;

import org.appverse.builder.domain.Engine;
import org.appverse.builder.web.rest.dto.EngineDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing Engine.
 */
public interface EngineService {

    /**
     * Save a engine.
     *
     * @return the persisted entity
     */
    public EngineDTO save(EngineDTO engineDTO);

    /**
     * get all the engines.
     *
     * @return the list of entities
     */
    public Page<Engine> findAll(Pageable pageable);

    /**
     * get the "id" engine.
     *
     * @return the entity
     */
    public EngineDTO findOne(Long id);

    /**
     * delete the "id" engine.
     */
    public void delete(Long id);

    Optional<EngineDTO> findByNameAndVersion(String name, String version);
}
