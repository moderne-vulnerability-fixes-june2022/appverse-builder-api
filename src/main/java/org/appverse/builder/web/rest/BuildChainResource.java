package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.domain.BuildChain;
import org.appverse.builder.security.SecurityUtils;
import org.appverse.builder.service.BuildChainService;
import org.appverse.builder.service.BuildRequestService;
import org.appverse.builder.web.rest.dto.BuildChainDTO;
import org.appverse.builder.web.rest.mapper.BuildChainMapper;
import org.appverse.builder.web.rest.util.HeaderUtil;
import org.appverse.builder.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST controller for managing BuildChain.
 */
@RestController
@RequestMapping("/api")
public class BuildChainResource {

    public static final String OPTION_SPLITTER = "=";
    private final Logger log = LoggerFactory.getLogger(BuildChainResource.class);

    @Inject
    private BuildChainService buildChainService;

    @Inject
    private BuildRequestService buildRequestService;

    @Inject
    private BuildChainMapper buildChainMapper;


    /**
     * POST  /buildChains -> Create a new buildChain.
     */
    @RequestMapping(value = "/buildChains/payload",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Timed
    @ApiOperation(value = "/api/buildChains/payload", notes = "Options have to be passed in the format value=key")
    public ResponseEntity<BuildChainDTO> createBuildChain(@RequestParam("payload") MultipartFile payload, @RequestParam(required = false) String[] options, @RequestParam(required = false) String flavor) throws URISyntaxException {
        Map<String, String> optionsMap = new HashMap<>();
        Optional.ofNullable(options).ifPresent(strings -> Stream.of(strings).forEach(option -> {
            String[] optionArray = option.split(OPTION_SPLITTER);
            if (optionArray.length > 1) {
                optionsMap.put(optionArray[0], optionArray[1]);
            } else if (optionArray.length > 0) {
                optionsMap.put(optionArray[0], null);
            }
        }));
        BuildChainDTO buildChain = buildChainService.createFromPayload(payload, optionsMap, Optional.ofNullable(flavor));
        buildChain.getRequests().forEach(requestDTO -> buildRequestService.schedule(requestDTO));
        return ResponseEntity.created(new URI("/api/buildChains/" + buildChain.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("buildChain", buildChain.getId().toString()))
            .body(buildChain);
    }


    /**
     * POST  /buildChains -> Create a new buildChain.
     */
    @RequestMapping(value = "/buildChains",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BuildChainDTO> createBuildChain(@Valid @RequestBody BuildChainDTO buildChainDTO) throws URISyntaxException {
        log.debug("REST request to save BuildChain : {}", buildChainDTO);
        if (buildChainDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("buildChain", "idexists", "A new buildChain cannot already have an ID")).body(null);
        }
        BuildChainDTO result = buildChainService.save(buildChainDTO);
        return ResponseEntity.created(new URI("/api/buildChains/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("buildChain", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /buildChains -> Updates an existing buildChain.
     */
    @RequestMapping(value = "/buildChains",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BuildChainDTO> updateBuildChain(@Valid @RequestBody BuildChainDTO buildChainDTO) throws URISyntaxException {
        log.debug("REST request to update BuildChain : {}", buildChainDTO);
        if (buildChainDTO.getId() == null) {
            return createBuildChain(buildChainDTO);
        }
        if (!SecurityUtils.isCurrentUserAdmin() && !buildChainService.findOne(buildChainDTO.getId()).getRequesterLogin().equals(SecurityUtils.getCurrentUserLogin())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        BuildChainDTO result = buildChainService.save(buildChainDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("buildChain", buildChainDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /buildChains -> get all the buildChains.
     */
    @RequestMapping(value = "/buildChains",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<BuildChainDTO>> getAllBuildChains(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of BuildChains");
        Page<BuildChain> page = SecurityUtils.isCurrentUserAdmin() ? buildChainService.findAll(pageable) : buildChainService.findByCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/buildChains");
        return new ResponseEntity<>(page.getContent().stream()
            .map(buildChainMapper::buildChainToBuildChainDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /buildChains/:id -> get the "id" buildChain.
     */
    @RequestMapping(value = "/buildChains/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BuildChainDTO> getBuildChain(@PathVariable Long id) {
        log.debug("REST request to get BuildChain : {}", id);
        BuildChainDTO buildChainDTO = buildChainService.findOne(id);
        return Optional.ofNullable(buildChainDTO)
            .map(result -> {
                if (!SecurityUtils.isCurrentUserAdmin() && !SecurityUtils.getCurrentUserLogin().equals(result.getRequesterLogin())) {
                    return new ResponseEntity<BuildChainDTO>(HttpStatus.UNAUTHORIZED);
                }
                return new ResponseEntity<>(
                    result,
                    HttpStatus.OK);

            })
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /buildChains/:id -> delete the "id" buildChain.
     */
    @RequestMapping(value = "/buildChains/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteBuildChain(@PathVariable Long id) {
        log.debug("REST request to delete BuildChain : {}", id);
        final Optional<BuildChainDTO> optional = Optional.ofNullable(buildChainService.findOne(id));
        if (!SecurityUtils.isCurrentUserAdmin() && (optional.isPresent() && !optional.get().getRequesterLogin().equals(SecurityUtils.getCurrentUserLogin()))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        buildChainService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("buildChain", id.toString())).build();
    }
}
