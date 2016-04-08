package org.appverse.builder.service;

import org.appverse.builder.domain.EnginePlatform;
import org.appverse.builder.web.rest.dto.EnginePlatformDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing EnginePlatform.
 */
public interface EnginePlatformService {

    /**
     * Save a enginePlatform.
     *
     * @return the persisted entity
     */
    public EnginePlatformDTO save(EnginePlatformDTO enginePlatformDTO);

    /**
     * get all the enginePlatforms.
     *
     * @return the list of entities
     */
    public Page<EnginePlatform> findAll(Pageable pageable);

    /**
     * get the "id" enginePlatform.
     *
     * @return the entity
     */
    public EnginePlatformDTO findOne(Long id);

    /**
     * delete the "id" enginePlatform.
     */
    public void delete(Long id);

    /**
     * @param engineName
     * @param platformName
     * @param version
     * @return
     */
    Optional<EnginePlatformDTO> findByEngineNameAndNameAndVersion(String engineName, String platformName, String version);
}
