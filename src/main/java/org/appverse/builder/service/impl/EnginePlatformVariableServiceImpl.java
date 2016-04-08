package org.appverse.builder.service.impl;

import org.appverse.builder.domain.EnginePlatformVariable;
import org.appverse.builder.repository.EnginePlatformVariableRepository;
import org.appverse.builder.service.EnginePlatformVariableService;
import org.appverse.builder.web.rest.dto.EnginePlatformVariableDTO;
import org.appverse.builder.web.rest.mapper.EnginePlatformVariableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing EnginePlatformVariable.
 */
@Service
@Transactional
public class EnginePlatformVariableServiceImpl implements EnginePlatformVariableService {

    private final Logger log = LoggerFactory.getLogger(EnginePlatformVariableServiceImpl.class);

    @Inject
    private EnginePlatformVariableRepository enginePlatformVariableRepository;

    @Inject
    private EnginePlatformVariableMapper enginePlatformVariableMapper;

    /**
     * Save a enginePlatformVariable.
     *
     * @return the persisted entity
     */
    public EnginePlatformVariableDTO save(EnginePlatformVariableDTO enginePlatformVariableDTO) {
        log.debug("Request to save EnginePlatformVariable : {}", enginePlatformVariableDTO);
        EnginePlatformVariable enginePlatformVariable = enginePlatformVariableMapper.enginePlatformVariableDTOToEnginePlatformVariable(enginePlatformVariableDTO);
        enginePlatformVariable = enginePlatformVariableRepository.save(enginePlatformVariable);
        EnginePlatformVariableDTO result = enginePlatformVariableMapper.enginePlatformVariableToEnginePlatformVariableDTO(enginePlatformVariable);
        return result;
    }

    /**
     * get all the enginePlatformVariables.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EnginePlatformVariable> findAll(Pageable pageable) {
        log.debug("Request to get all EnginePlatformVariables");
        Page<EnginePlatformVariable> result = enginePlatformVariableRepository.findAll(pageable);
        return result;
    }

    /**
     * get one enginePlatformVariable by id.
     *
     * @return the entity
     */
    @Transactional(readOnly = true)
    public EnginePlatformVariableDTO findOne(Long id) {
        log.debug("Request to get EnginePlatformVariable : {}", id);
        EnginePlatformVariable enginePlatformVariable = enginePlatformVariableRepository.findOne(id);
        EnginePlatformVariableDTO enginePlatformVariableDTO = enginePlatformVariableMapper.enginePlatformVariableToEnginePlatformVariableDTO(enginePlatformVariable);
        return enginePlatformVariableDTO;
    }

    /**
     * delete the  enginePlatformVariable by id.
     */
    public void delete(Long id) {
        log.debug("Request to delete EnginePlatformVariable : {}", id);
        enginePlatformVariableRepository.delete(id);
    }

    @Override
    public List<EnginePlatformVariableDTO> findByEnginePlatformId(Long id) {
        return enginePlatformVariableRepository.findByEnginePlatformId(id).stream().map(enginePlatformVariable -> enginePlatformVariableMapper.enginePlatformVariableToEnginePlatformVariableDTO(enginePlatformVariable)).collect(Collectors.toList());
    }
}
