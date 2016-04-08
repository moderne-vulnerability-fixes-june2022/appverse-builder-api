package org.appverse.builder.service.impl;

import org.appverse.builder.domain.EnginePlatform;
import org.appverse.builder.repository.EnginePlatformRepository;
import org.appverse.builder.service.EnginePlatformService;
import org.appverse.builder.web.rest.dto.EnginePlatformDTO;
import org.appverse.builder.web.rest.mapper.EnginePlatformMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Service Implementation for managing EnginePlatform.
 */
@Service
@Transactional
public class EnginePlatformServiceImpl implements EnginePlatformService {

    private final Logger log = LoggerFactory.getLogger(EnginePlatformServiceImpl.class);

    @Inject
    private EnginePlatformRepository enginePlatformRepository;

    @Inject
    private EnginePlatformMapper enginePlatformMapper;

    /**
     * Save a enginePlatform.
     *
     * @return the persisted entity
     */
    public EnginePlatformDTO save(EnginePlatformDTO enginePlatformDTO) {
        log.debug("Request to save EnginePlatform : {}", enginePlatformDTO);
        EnginePlatform enginePlatform = enginePlatformMapper.enginePlatformDTOToEnginePlatform(enginePlatformDTO);
        enginePlatform = enginePlatformRepository.save(enginePlatform);
        EnginePlatformDTO result = enginePlatformMapper.enginePlatformToEnginePlatformDTO(enginePlatform);
        return result;
    }

    /**
     * get all the enginePlatforms.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EnginePlatform> findAll(Pageable pageable) {
        log.debug("Request to get all EnginePlatforms");
        Page<EnginePlatform> result = enginePlatformRepository.findAll(pageable);
        return result;
    }

    /**
     * get one enginePlatform by id.
     *
     * @return the entity
     */
    @Transactional(readOnly = true)
    public EnginePlatformDTO findOne(Long id) {
        log.debug("Request to get EnginePlatform : {}", id);
        EnginePlatform enginePlatform = enginePlatformRepository.findOne(id);
        EnginePlatformDTO enginePlatformDTO = enginePlatformMapper.enginePlatformToEnginePlatformDTO(enginePlatform);
        return enginePlatformDTO;
    }

    /**
     * delete the  enginePlatform by id.
     */
    public void delete(Long id) {
        log.debug("Request to delete EnginePlatform : {}", id);
        enginePlatformRepository.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnginePlatformDTO> findByEngineNameAndNameAndVersion(String engineName, String platformName, String version) {
        return Optional.ofNullable(enginePlatformMapper.enginePlatformToEnginePlatformDTO(enginePlatformRepository.findByEngineNameAndNameAndVersionAndEnabledTrue(engineName, platformName, version)));
    }
}
