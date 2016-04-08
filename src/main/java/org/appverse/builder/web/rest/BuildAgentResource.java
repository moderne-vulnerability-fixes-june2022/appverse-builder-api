package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.domain.BuildAgent;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.service.BuildAgentQueueService;
import org.appverse.builder.service.BuildAgentService;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.mapper.BuildAgentMapper;
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
 * REST controller for managing BuildAgent.
 */
@RestController
@RequestMapping("/api")
@Secured(AuthoritiesConstants.ADMIN)
public class BuildAgentResource {

    private final Logger log = LoggerFactory.getLogger(BuildAgentResource.class);

    @Inject
    private BuildAgentService buildAgentService;

    @Inject
    private BuildAgentMapper buildAgentMapper;

    @Inject
    private BuildAgentQueueService buildAgentQueueService;

    /**
     * POST  /buildAgents -> Create a new buildAgent.
     */
    @RequestMapping(value = "/buildAgents",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BuildAgentDTO> createBuildAgent(@Valid @RequestBody BuildAgentDTO buildAgentDTO) throws URISyntaxException {
        log.debug("REST request to save BuildAgent : {}", buildAgentDTO);
        if (buildAgentDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("buildAgent", "idexists", "A new buildAgent cannot already have an ID")).body(null);
        }
        BuildAgentDTO result = buildAgentService.save(buildAgentDTO);
        return ResponseEntity.created(new URI("/api/buildAgents/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("buildAgent", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /buildAgents -> Updates an existing buildAgent.
     */
    @RequestMapping(value = "/buildAgents",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BuildAgentDTO> updateBuildAgent(@Valid @RequestBody BuildAgentDTO buildAgentDTO) throws URISyntaxException {
        log.debug("REST request to update BuildAgent : {}", buildAgentDTO);
        if (buildAgentDTO.getId() == null) {
            return createBuildAgent(buildAgentDTO);
        }
        BuildAgentDTO result = buildAgentService.save(buildAgentDTO);
        buildAgentQueueService.stopAndRemoveControllerForAgent(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("buildAgent", buildAgentDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /buildAgents -> get all the buildAgents.
     */
    @RequestMapping(value = "/buildAgents",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<BuildAgentDTO>> getAllBuildAgents(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of BuildAgents");
        Page<BuildAgent> page = buildAgentService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/buildAgents");
        return new ResponseEntity<>(page.getContent().stream()
            .map(buildAgentMapper::buildAgentToBuildAgentDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /buildAgents/:id -> get the "id" buildAgent.
     */
    @RequestMapping(value = "/buildAgents/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BuildAgentDTO> getBuildAgent(@PathVariable Long id) {
        log.debug("REST request to get BuildAgent : {}", id);
        BuildAgentDTO buildAgentDTO = buildAgentService.findOne(id);
        return Optional.ofNullable(buildAgentDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /buildAgents/:id -> delete the "id" buildAgent.
     */
    @RequestMapping(value = "/buildAgents/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteBuildAgent(@PathVariable Long id) {
        log.debug("REST request to delete BuildAgent : {}", id);
        Optional.ofNullable(buildAgentService.findOne(id)).ifPresent(buildAgentDTO -> buildAgentQueueService.stopAndRemoveControllerForAgent(buildAgentDTO));
        buildAgentService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("buildAgent", id.toString())).build();
    }
}
