package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.domain.EngineVariable;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.service.EngineVariableService;
import org.appverse.builder.web.rest.dto.EngineVariableDTO;
import org.appverse.builder.web.rest.mapper.EngineVariableMapper;
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
 * REST controller for managing EngineVariable.
 */
@RestController
@RequestMapping("/api")
public class EngineVariableResource {

    private final Logger log = LoggerFactory.getLogger(EngineVariableResource.class);

    @Inject
    private EngineVariableService engineVariableService;

    @Inject
    private EngineVariableMapper engineVariableMapper;

    /**
     * POST  /engineVariables -> Create a new engineVariable.
     */
    @RequestMapping(value = "/engineVariables",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<EngineVariableDTO> createEngineVariable(@Valid @RequestBody EngineVariableDTO engineVariableDTO) throws URISyntaxException {
        log.debug("REST request to save EngineVariable : {}", engineVariableDTO);
        if (engineVariableDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("engineVariable", "idexists", "A new engineVariable cannot already have an ID")).body(null);
        }
        EngineVariableDTO result = engineVariableService.save(engineVariableDTO);
        return ResponseEntity.created(new URI("/api/engineVariables/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("engineVariable", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /engineVariables -> Updates an existing engineVariable.
     */
    @RequestMapping(value = "/engineVariables",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<EngineVariableDTO> updateEngineVariable(@Valid @RequestBody EngineVariableDTO engineVariableDTO) throws URISyntaxException {
        log.debug("REST request to update EngineVariable : {}", engineVariableDTO);
        if (engineVariableDTO.getId() == null) {
            return createEngineVariable(engineVariableDTO);
        }
        EngineVariableDTO result = engineVariableService.save(engineVariableDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("engineVariable", engineVariableDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /engineVariables -> get all the engineVariables.
     */
    @RequestMapping(value = "/engineVariables",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<EngineVariableDTO>> getAllEngineVariables(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of EngineVariables");
        Page<EngineVariable> page = engineVariableService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/engineVariables");
        return new ResponseEntity<>(page.getContent().stream()
            .map(engineVariableMapper::engineVariableToEngineVariableDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /engineVariables/:id -> get the "id" engineVariable.
     */
    @RequestMapping(value = "/engineVariables/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<EngineVariableDTO> getEngineVariable(@PathVariable Long id) {
        log.debug("REST request to get EngineVariable : {}", id);
        EngineVariableDTO engineVariableDTO = engineVariableService.findOne(id);
        return Optional.ofNullable(engineVariableDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /engineVariables/:id -> delete the "id" engineVariable.
     */
    @RequestMapping(value = "/engineVariables/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> deleteEngineVariable(@PathVariable Long id) {
        log.debug("REST request to delete EngineVariable : {}", id);
        engineVariableService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("engineVariable", id.toString())).build();
    }
}
