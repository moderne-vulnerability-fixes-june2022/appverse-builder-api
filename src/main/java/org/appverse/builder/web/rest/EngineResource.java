package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.domain.Engine;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.service.EngineService;
import org.appverse.builder.web.rest.dto.EngineDTO;
import org.appverse.builder.web.rest.mapper.EngineMapper;
import org.appverse.builder.web.rest.util.HeaderUtil;
import org.appverse.builder.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing Engine.
 */
@RestController
@RequestMapping("/api")
public class EngineResource {

    private final Logger log = LoggerFactory.getLogger(EngineResource.class);

    @Inject
    private EngineService engineService;

    @Inject
    private EngineMapper engineMapper;

    /**
     * POST  /engines -> Create a new engine.
     */
    @RequestMapping(value = "/engines",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<EngineDTO> createEngine(@Valid @RequestBody EngineDTO engineDTO) throws URISyntaxException {
        log.debug("REST request to save Engine : {}", engineDTO);
        if (engineDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("engine", "idexists", "A new engine cannot already have an ID")).body(null);
        }
        EngineDTO result = engineService.save(engineDTO);
        return ResponseEntity.created(new URI("/api/engines/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("engine", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /engines -> Updates an existing engine.
     */
    @RequestMapping(value = "/engines",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<EngineDTO> updateEngine(@Valid @RequestBody EngineDTO engineDTO) throws URISyntaxException {
        log.debug("REST request to update Engine : {}", engineDTO);
        if (engineDTO.getId() == null) {
            return createEngine(engineDTO);
        }
        EngineDTO result = engineService.save(engineDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("engine", engineDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /engines -> get all the engines.
     */
    @RequestMapping(value = "/engines",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<EngineDTO>> getAllEngines(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Engines");
        Page<Engine> page = engineService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/engines");
        return new ResponseEntity<>(page.getContent().stream()
            .map(engineMapper::engineToEngineDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /engines/:id -> get the "id" engine.
     */
    @RequestMapping(value = "/engines/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<EngineDTO> getEngine(@PathVariable Long id) {
        log.debug("REST request to get Engine : {}", id);
        EngineDTO engineDTO = engineService.findOne(id);
        return Optional.ofNullable(engineDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /engines/:id -> delete the "id" engine.
     */
    @RequestMapping(value = "/engines/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> deleteEngine(@PathVariable Long id) {
        log.debug("REST request to delete Engine : {}", id);
        engineService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("engine", id.toString())).build();
    }
}
