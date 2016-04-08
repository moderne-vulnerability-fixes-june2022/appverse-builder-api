package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.domain.EnginePlatform;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.service.EnginePlatformService;
import org.appverse.builder.web.rest.dto.EnginePlatformDTO;
import org.appverse.builder.web.rest.mapper.EnginePlatformMapper;
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
 * REST controller for managing EnginePlatform.
 */
@RestController
@RequestMapping("/api")
public class EnginePlatformResource {

    private final Logger log = LoggerFactory.getLogger(EnginePlatformResource.class);

    @Inject
    private EnginePlatformService enginePlatformService;

    @Inject
    private EnginePlatformMapper enginePlatformMapper;

    /**
     * POST  /enginePlatforms -> Create a new enginePlatform.
     */
    @RequestMapping(value = "/enginePlatforms",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<EnginePlatformDTO> createEnginePlatform(@Valid @RequestBody EnginePlatformDTO enginePlatformDTO) throws URISyntaxException {
        log.debug("REST request to save EnginePlatform : {}", enginePlatformDTO);
        if (enginePlatformDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("enginePlatform", "idexists", "A new enginePlatform cannot already have an ID")).body(null);
        }
        EnginePlatformDTO result = enginePlatformService.save(enginePlatformDTO);
        return ResponseEntity.created(new URI("/api/enginePlatforms/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("enginePlatform", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /enginePlatforms -> Updates an existing enginePlatform.
     */
    @RequestMapping(value = "/enginePlatforms",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<EnginePlatformDTO> updateEnginePlatform(@Valid @RequestBody EnginePlatformDTO enginePlatformDTO) throws URISyntaxException {
        log.debug("REST request to update EnginePlatform : {}", enginePlatformDTO);
        if (enginePlatformDTO.getId() == null) {
            return createEnginePlatform(enginePlatformDTO);
        }
        EnginePlatformDTO result = enginePlatformService.save(enginePlatformDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("enginePlatform", enginePlatformDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /enginePlatforms -> get all the enginePlatforms.
     */
    @RequestMapping(value = "/enginePlatforms",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<EnginePlatformDTO>> getAllEnginePlatforms(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of EnginePlatforms");
        Page<EnginePlatform> page = enginePlatformService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/enginePlatforms");
        return new ResponseEntity<>(page.getContent().stream()
            .map(enginePlatformMapper::enginePlatformToEnginePlatformDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /enginePlatforms/:id -> get the "id" enginePlatform.
     */
    @RequestMapping(value = "/enginePlatforms/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<EnginePlatformDTO> getEnginePlatform(@PathVariable Long id) {
        log.debug("REST request to get EnginePlatform : {}", id);
        EnginePlatformDTO enginePlatformDTO = enginePlatformService.findOne(id);
        return Optional.ofNullable(enginePlatformDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /enginePlatforms/:id -> delete the "id" enginePlatform.
     */
    @RequestMapping(value = "/enginePlatforms/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<Void> deleteEnginePlatform(@PathVariable Long id) {
        log.debug("REST request to delete EnginePlatform : {}", id);
        enginePlatformService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("enginePlatform", id.toString())).build();
    }
}
