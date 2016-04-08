package org.appverse.builder.service.impl;

import org.appverse.builder.domain.EngineVariable;
import org.appverse.builder.repository.EngineVariableRepository;
import org.appverse.builder.service.EngineVariableService;
import org.appverse.builder.web.rest.dto.EngineVariableDTO;
import org.appverse.builder.web.rest.mapper.EngineVariableMapper;
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
 * Service Implementation for managing EngineVariable.
 */
@Service
@Transactional
public class EngineVariableServiceImpl implements EngineVariableService {

    private final Logger log = LoggerFactory.getLogger(EngineVariableServiceImpl.class);

    @Inject
    private EngineVariableRepository engineVariableRepository;

    @Inject
    private EngineVariableMapper engineVariableMapper;

    /**
     * Save a engineVariable.
     *
     * @return the persisted entity
     */
    public EngineVariableDTO save(EngineVariableDTO engineVariableDTO) {
        log.debug("Request to save EngineVariable : {}", engineVariableDTO);
        EngineVariable engineVariable = engineVariableMapper.engineVariableDTOToEngineVariable(engineVariableDTO);
        engineVariable = engineVariableRepository.save(engineVariable);
        EngineVariableDTO result = engineVariableMapper.engineVariableToEngineVariableDTO(engineVariable);
        return result;
    }

    /**
     * get all the engineVariables.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EngineVariable> findAll(Pageable pageable) {
        log.debug("Request to get all EngineVariables");
        Page<EngineVariable> result = engineVariableRepository.findAll(pageable);
        return result;
    }

    /**
     * get one engineVariable by id.
     *
     * @return the entity
     */
    @Transactional(readOnly = true)
    public EngineVariableDTO findOne(Long id) {
        log.debug("Request to get EngineVariable : {}", id);
        EngineVariable engineVariable = engineVariableRepository.findOne(id);
        EngineVariableDTO engineVariableDTO = engineVariableMapper.engineVariableToEngineVariableDTO(engineVariable);
        return engineVariableDTO;
    }

    /**
     * delete the  engineVariable by id.
     */
    public void delete(Long id) {
        log.debug("Request to delete EngineVariable : {}", id);
        engineVariableRepository.delete(id);
    }

    @Override
    public List<EngineVariableDTO> findByEngineId(Long engineId) {
        return engineVariableRepository.findByEngineId(engineId).stream().map(engineVariable -> engineVariableMapper.engineVariableToEngineVariableDTO(engineVariable)).collect(Collectors.toList());
    }
}
