package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.domain.DistributionChannel;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.service.DistributionChannelService;
import org.appverse.builder.web.rest.dto.DistributionChannelDTO;
import org.appverse.builder.web.rest.mapper.DistributionChannelMapper;
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
 * REST controller for managing DistributionChannel.
 */
@RestController
@RequestMapping("/api")
@Secured(AuthoritiesConstants.ADMIN)
public class DistributionChannelResource {

    private final Logger log = LoggerFactory.getLogger(DistributionChannelResource.class);

    @Inject
    private DistributionChannelService distributionChannelService;

    @Inject
    private DistributionChannelMapper distributionChannelMapper;

    /**
     * POST  /distributionChannels -> Create a new distributionChannel.
     */
    @RequestMapping(value = "/distributionChannels",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<DistributionChannelDTO> createDistributionChannel(@Valid @RequestBody DistributionChannelDTO distributionChannelDTO) throws URISyntaxException {
        log.debug("REST request to save DistributionChannel : {}", distributionChannelDTO);
        if (distributionChannelDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("distributionChannel", "idexists", "A new distributionChannel cannot already have an ID")).body(null);
        }
        DistributionChannelDTO result = distributionChannelService.save(distributionChannelDTO);
        return ResponseEntity.created(new URI("/api/distributionChannels/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("distributionChannel", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /distributionChannels -> Updates an existing distributionChannel.
     */
    @RequestMapping(value = "/distributionChannels",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<DistributionChannelDTO> updateDistributionChannel(@Valid @RequestBody DistributionChannelDTO distributionChannelDTO) throws URISyntaxException {
        log.debug("REST request to update DistributionChannel : {}", distributionChannelDTO);
        if (distributionChannelDTO.getId() == null) {
            return createDistributionChannel(distributionChannelDTO);
        }
        DistributionChannelDTO result = distributionChannelService.save(distributionChannelDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("distributionChannel", distributionChannelDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /distributionChannels -> get all the distributionChannels.
     */
    @RequestMapping(value = "/distributionChannels",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<DistributionChannelDTO>> getAllDistributionChannels(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of DistributionChannels");
        Page<DistributionChannel> page = distributionChannelService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/distributionChannels");
        return new ResponseEntity<>(page.getContent().stream()
            .map(distributionChannelMapper::distributionChannelToDistributionChannelDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /distributionChannels/:id -> get the "id" distributionChannel.
     */
    @RequestMapping(value = "/distributionChannels/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<DistributionChannelDTO> getDistributionChannel(@PathVariable Long id) {
        log.debug("REST request to get DistributionChannel : {}", id);
        DistributionChannelDTO distributionChannelDTO = distributionChannelService.findOne(id);
        return Optional.ofNullable(distributionChannelDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /distributionChannels/:id -> delete the "id" distributionChannel.
     */
    @RequestMapping(value = "/distributionChannels/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteDistributionChannel(@PathVariable Long id) {
        log.debug("REST request to delete DistributionChannel : {}", id);
        distributionChannelService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("distributionChannel", id.toString())).build();
    }
}
