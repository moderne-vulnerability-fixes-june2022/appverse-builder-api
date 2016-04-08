package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.domain.NotificationChannel;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.service.NotificationChannelService;
import org.appverse.builder.web.rest.dto.NotificationChannelDTO;
import org.appverse.builder.web.rest.mapper.NotificationChannelMapper;
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
 * REST controller for managing NotificationChannel.
 */
@RestController
@RequestMapping("/api")
@Secured(AuthoritiesConstants.ADMIN)
public class NotificationChannelResource {

    private final Logger log = LoggerFactory.getLogger(NotificationChannelResource.class);

    @Inject
    private NotificationChannelService notificationChannelService;

    @Inject
    private NotificationChannelMapper notificationChannelMapper;

    /**
     * POST  /notificationChannels -> Create a new notificationChannel.
     */
    @RequestMapping(value = "/notificationChannels",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<NotificationChannelDTO> createNotificationChannel(@Valid @RequestBody NotificationChannelDTO notificationChannelDTO) throws URISyntaxException {
        log.debug("REST request to save NotificationChannel : {}", notificationChannelDTO);
        if (notificationChannelDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("notificationChannel", "idexists", "A new notificationChannel cannot already have an ID")).body(null);
        }
        NotificationChannelDTO result = notificationChannelService.save(notificationChannelDTO);
        return ResponseEntity.created(new URI("/api/notificationChannels/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("notificationChannel", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /notificationChannels -> Updates an existing notificationChannel.
     */
    @RequestMapping(value = "/notificationChannels",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<NotificationChannelDTO> updateNotificationChannel(@Valid @RequestBody NotificationChannelDTO notificationChannelDTO) throws URISyntaxException {
        log.debug("REST request to update NotificationChannel : {}", notificationChannelDTO);
        if (notificationChannelDTO.getId() == null) {
            return createNotificationChannel(notificationChannelDTO);
        }
        NotificationChannelDTO result = notificationChannelService.save(notificationChannelDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("notificationChannel", notificationChannelDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /notificationChannels -> get all the notificationChannels.
     */
    @RequestMapping(value = "/notificationChannels",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<NotificationChannelDTO>> getAllNotificationChannels(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of NotificationChannels");
        Page<NotificationChannel> page = notificationChannelService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/notificationChannels");
        return new ResponseEntity<>(page.getContent().stream()
            .map(notificationChannelMapper::notificationChannelToNotificationChannelDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /notificationChannels/:id -> get the "id" notificationChannel.
     */
    @RequestMapping(value = "/notificationChannels/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<NotificationChannelDTO> getNotificationChannel(@PathVariable Long id) {
        log.debug("REST request to get NotificationChannel : {}", id);
        NotificationChannelDTO notificationChannelDTO = notificationChannelService.findOne(id);
        return Optional.ofNullable(notificationChannelDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /notificationChannels/:id -> delete the "id" notificationChannel.
     */
    @RequestMapping(value = "/notificationChannels/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteNotificationChannel(@PathVariable Long id) {
        log.debug("REST request to delete NotificationChannel : {}", id);
        notificationChannelService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("notificationChannel", id.toString())).build();
    }
}
