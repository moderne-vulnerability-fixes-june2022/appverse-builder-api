package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.io.IOUtils;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.domain.BuildRequest;
import org.appverse.builder.security.SecurityUtils;
import org.appverse.builder.service.BuildRequestService;
import org.appverse.builder.service.DistributionChannelService;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.mapper.BuildRequestMapper;
import org.appverse.builder.web.rest.util.HeaderUtil;
import org.appverse.builder.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing BuildRequest.
 */
@RestController
@RequestMapping("/api")
public class BuildRequestResource {

    private final Logger log = LoggerFactory.getLogger(BuildRequestResource.class);

    @Inject
    private BuildRequestService buildRequestService;

    @Inject
    private BuildRequestMapper buildRequestMapper;

    @Inject
    private DistributionChannelService distributionChannelService;


    /**
     * POST  /buildRequests -> Create a new buildRequest.
     */
    @RequestMapping(value = "/buildRequests",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BuildRequestDTO> createBuildRequest(@Valid @RequestBody BuildRequestDTO buildRequestDTO) throws URISyntaxException {
        log.debug("REST request to save BuildRequest : {}", buildRequestDTO);
        if (buildRequestDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("buildRequest", "idexists", "A new buildRequest cannot already have an ID")).body(null);
        }
        BuildRequestDTO result = buildRequestService.save(buildRequestDTO);
        return ResponseEntity.created(new URI("/api/buildRequests/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("buildRequest", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /buildRequests -> Updates an existing buildRequest.
     */
    @RequestMapping(value = "/buildRequests",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BuildRequestDTO> updateBuildRequest(@Valid @RequestBody BuildRequestDTO buildRequestDTO) throws URISyntaxException {
        log.debug("REST request to update BuildRequest : {}", buildRequestDTO);
        if (buildRequestDTO.getId() == null) {
            return createBuildRequest(buildRequestDTO);
        }
        if (buildRequestService.hasAccess(buildRequestService.findOne(buildRequestDTO.getId()))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        BuildRequestDTO result = buildRequestService.save(buildRequestDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("buildRequest", buildRequestDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /buildRequests -> get all the buildRequests.
     */
    @RequestMapping(value = "/buildRequests",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<BuildRequestDTO>> getAllBuildRequests(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of BuildRequests");
        Page<BuildRequest> page = SecurityUtils.isCurrentUserAdmin() ? buildRequestService.findAll(pageable) : buildRequestService.findByCurrentUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/buildRequests");
        return new ResponseEntity<>(page.getContent().stream()
            .map(buildRequestMapper::buildRequestToBuildRequestDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /buildRequests/:id -> get the "id" buildRequest.
     */
    @RequestMapping(value = "/buildRequests/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<BuildRequestDTO> getBuildRequest(@PathVariable Long id) {
        log.debug("REST request to get BuildRequest : {}", id);
        BuildRequestDTO buildRequestDTO = buildRequestService.findOne(id);
        return Optional.ofNullable(buildRequestDTO)
            .map(result -> {
                if (!SecurityUtils.isCurrentUserAdmin() && !SecurityUtils.getCurrentUserLogin().equals(result.getRequesterLogin())) {
                    return new ResponseEntity<BuildRequestDTO>(HttpStatus.UNAUTHORIZED);
                }
                return new ResponseEntity<>(
                    result,
                    HttpStatus.OK);
            })
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /buildRequests/:id -> get the "id" buildRequest.
     */
    @RequestMapping(value = "/buildRequests/{id}/log",
        method = RequestMethod.GET,
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public void getLogs(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        PrintWriter writer = new PrintWriter(response.getOutputStream(), true);
        BufferedReader logReader = null;
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        try {
            final Optional<BuildRequestDTO> requestDTOOptional = Optional.ofNullable(buildRequestService.findOne(id));
            if (!requestDTOOptional.isPresent()) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                writer.println(MessageFormat.format("Request {0} not found", id.toString()));
                return;
            }

            if (buildRequestService.hasAccess(requestDTOOptional.get())) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                writer.println("UNAUTHORIZED");
                return;
            }
            Optional<InputStream> request = buildRequestService.getLogs(id);
            if (request.isPresent()) {
                //This disables nginx proxy buffering, required for this request to printout continuously
                response.addHeader("X-Accel-Buffering", "no");
                response.setStatus(HttpStatus.OK.value());
                response.flushBuffer();
                logReader = new BufferedReader(new InputStreamReader(request.get()));
                logReader.lines().forEachOrdered((line) -> {
                    try {
                        writer.println(line);
                        writer.flush();
                    } catch (Throwable t) {
                        log.info("Error writing log to the client");
                    }
                });
            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                writer.println(MessageFormat.format("Request {0} logs not found", id.toString()));
            }
        } catch (IOException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            writer.println("There was an error reading/writing the logs, please try again later");
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(logReader);
        }
    }


    /**
     * GET  /buildRequests/:id/artifacts -> get the "id" buildRequest artifacts.
     */
    @RequestMapping(value = "/buildRequests/{id}/artifacts",
        method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Timed
    public ResponseEntity<Artifact[]> getArtifacts(@PathVariable("id") Long id) throws IOException {

        return Optional.ofNullable(buildRequestService.findOne(id)).map(buildRequestDTO -> {
            if (buildRequestService.hasAccess(buildRequestDTO)) {
                return new ResponseEntity<Artifact[]>(HttpStatus.UNAUTHORIZED);
            }
            List<Artifact> artifacts = distributionChannelService.getRequestArtifacts(buildRequestDTO, true);
            return artifacts.isEmpty() ? new ResponseEntity<Artifact[]>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(artifacts.toArray(new Artifact[artifacts.size()]), HttpStatus.OK);

        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    /**
     * GET  /buildRequests/:id/artifact/:name -> get the "name" artifact for the "id" buildRequest.
     */
    @RequestMapping(value = "/buildRequests/{id}/artifact/{distributionId}/{name:.+}",
        method = RequestMethod.GET, produces = MediaType.ALL_VALUE)
    @Timed
    public ResponseEntity getArtifactByName(@PathVariable("id") Long id, @PathVariable("distributionId") Long distributionId, @PathVariable("name") String name) throws IOException {
        return Optional.ofNullable(buildRequestService.findOne(id)).map(buildRequestDTO -> {
            if (buildRequestService.hasAccess(buildRequestDTO)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            return distributionChannelService.getBuildArtifactByName(buildRequestDTO, distributionId, name)
                .map(ArtifactDownloadResource::getFileSystemResourceResponseEntityForArtifact)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); //could not find the artifact
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); //could not find the request
    }

    /**
     * GET  /buildRequests/:id/artifacts/compressed -> get the "id" buildRequest artifacts in a compressed file.
     */
    @RequestMapping(value = "/buildRequests/{id}/artifacts/compressed",
        method = RequestMethod.GET, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @Timed
    public ResponseEntity getCompressedArtifacts(@PathVariable("id") Long id) throws IOException {

        return Optional.ofNullable(buildRequestService.findOne(id)).map(buildRequestDTO -> {
            if (buildRequestService.hasAccess(buildRequestDTO)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            try {
                return buildRequestService.getCompressedArtifacts(buildRequestDTO.getId())
                    .map(BuildRequestResource::getFileSystemResourceResponseEntity)
                    .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
            } catch (IOException e) {
                log.warn("Error creating artifacts zip for request {}", buildRequestDTO, e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    public static ResponseEntity<FileSystemResource> getFileSystemResourceResponseEntity(File file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(file.length());
        headers.add("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        return new ResponseEntity<>(new FileSystemResource(file), headers, HttpStatus.OK);
    }

    /**
     * DELETE  /buildRequests/:id -> delete the "id" buildRequest.
     */
    @RequestMapping(value = "/buildRequests/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteBuildRequest(@PathVariable Long id) {
        log.debug("REST request to delete BuildRequest : {}", id);
        final Optional<BuildRequestDTO> optional = Optional.ofNullable(buildRequestService.findOne(id));
        if (!optional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        if (buildRequestService.hasAccess(optional.get())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        buildRequestService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("buildRequest", id.toString())).build();
    }
}
