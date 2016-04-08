package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.domain.EnginePlatformVariable;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.service.EnginePlatformVariableService;
import org.appverse.builder.web.rest.dto.EnginePlatformVariableDTO;
import org.appverse.builder.web.rest.mapper.EnginePlatformVariableMapper;
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
 * REST controller for managing EnginePlatformVariable.
 */
@RestController
@RequestMapping("/api")
public class EnginePlatformVariableResource {

    private final Logger log = LoggerFactory.getLogger(EnginePlatformVariableResource.class);

    @Inject
    private EnginePlatformVariableService enginePlatformVariableService;

    @Inject
    private EnginePlatformVariableMapper enginePlatformVariableMapper;

    /**
     * POST  /enginePlatformVariables -> Create a new enginePlatformVariable.
     */
    @RequestMapping(value = "/enginePlatformVariables",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<EnginePlatformVariableDTO> createEnginePlatformVariable(@Valid @RequestBody EnginePlatformVariableDTO enginePlatformVariableDTO) throws URISyntaxException {
        log.debug("REST request to save EnginePlatformVariable : {}", enginePlatformVariableDTO);
        if (enginePlatformVariableDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("enginePlatformVariable", "idexists", "A new enginePlatformVariable cannot already have an ID")).body(null);
        }
        EnginePlatformVariableDTO result = enginePlatformVariableService.save(enginePlatformVariableDTO);
        return ResponseEntity.created(new URI("/api/enginePlatformVariables/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("enginePlatformVariable", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /enginePlatformVariables -> Updates an existing enginePlatformVariable.
     */
    @RequestMapping(value = "/enginePlatformVariables",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<EnginePlatformVariableDTO> updateEnginePlatformVariable(@Valid @RequestBody EnginePlatformVariableDTO enginePlatformVariableDTO) throws URISyntaxException {
        log.debug("REST request to update EnginePlatformVariable : {}", enginePlatformVariableDTO);
        if (enginePlatformVariableDTO.getId() == null) {
            return createEnginePlatformVariable(enginePlatformVariableDTO);
        }
        EnginePlatformVariableDTO result = enginePlatformVariableService.save(enginePlatformVariableDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("enginePlatformVariable", enginePlatformVariableDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /enginePlatformVariables -> get all the enginePlatformVariables.
     */
    @RequestMapping(value = "/enginePlatformVariables",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<EnginePlatformVariableDTO>> getAllEnginePlatformVariables(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of EnginePlatformVariables");
        Page<EnginePlatformVariable> page = enginePlatformVariableService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/enginePlatformVariables");
        return new ResponseEntity<>(page.getContent().stream()
            .map(enginePlatformVariableMapper::enginePlatformVariableToEnginePlatformVariableDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /enginePlatformVariables/:id -> get the "id" enginePlatformVariable.
     */
    @RequestMapping(value = "/enginePlatformVariables/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<EnginePlatformVariableDTO> getEnginePlatformVariable(@PathVariable Long id) {
        log.debug("REST request to get EnginePlatformVariable : {}", id);
        EnginePlatformVariableDTO enginePlatformVariableDTO = enginePlatformVariableService.findOne(id);
        return Optional.ofNullable(enginePlatformVariableDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /enginePlatformVariables/:id -> delete the "id" enginePlatformVariable.
     */
    @RequestMapping(value = "/enginePlatformVariables/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> deleteEnginePlatformVariable(@PathVariable Long id) {
        log.debug("REST request to delete EnginePlatformVariable : {}", id);
        enginePlatformVariableService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("enginePlatformVariable", id.toString())).build();
    }
}
