package org.appverse.builder.service.impl;

import org.appverse.builder.domain.Engine;
import org.appverse.builder.repository.EngineRepository;
import org.appverse.builder.service.EngineService;
import org.appverse.builder.web.rest.dto.EngineDTO;
import org.appverse.builder.web.rest.mapper.EngineMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Service Implementation for managing Engine.
 */
@Service
@Transactional
public class EngineServiceImpl implements EngineService {

    private final Logger log = LoggerFactory.getLogger(EngineServiceImpl.class);

    @Inject
    private EngineRepository engineRepository;

    @Inject
    private EngineMapper engineMapper;

    /**
     * Save a engine.
     *
     * @return the persisted entity
     */
    public EngineDTO save(EngineDTO engineDTO) {
        log.debug("Request to save Engine : {}", engineDTO);
        Engine engine = engineMapper.engineDTOToEngine(engineDTO);
        engine = engineRepository.save(engine);
        EngineDTO result = engineMapper.engineToEngineDTO(engine);
        return result;
    }

    /**
     * get all the engines.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Engine> findAll(Pageable pageable) {
        log.debug("Request to get all Engines");
        Page<Engine> result = engineRepository.findAll(pageable);
        return result;
    }

    /**
     * get one engine by id.
     *
     * @return the entity
     */
    @Transactional(readOnly = true)
    public EngineDTO findOne(Long id) {
        log.debug("Request to get Engine : {}", id);
        Engine engine = engineRepository.findOne(id);
        EngineDTO engineDTO = engineMapper.engineToEngineDTO(engine);
        return engineDTO;
    }

    /**
     * delete the  engine by id.
     */
    public void delete(Long id) {
        log.debug("Request to delete Engine : {}", id);
        engineRepository.delete(id);
    }

    @Override
    public Optional<EngineDTO> findByNameAndVersion(String name, String version) {
        return engineRepository.findByNameAndVersionAndEnabledTrue(name, version).map(engine -> engineMapper.engineToEngineDTO(engine));
    }


}
