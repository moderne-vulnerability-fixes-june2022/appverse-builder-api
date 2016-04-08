package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.service.BuildRequestService;
import org.appverse.builder.service.DistributionChannelService;
import org.appverse.builder.service.DownloadTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * Created by panthro on 03/03/16.
 */
@RestController
@RequestMapping(ArtifactDownloadResource.API_BASE)
public class ArtifactDownloadResource {

    public static final String API_BASE = "/api/artifacts/{token}";
    public static final String COMPRESSED_DISTRIBUTION_ID = "/compressed/{distributionId}";
    public static final String COMPRESSED = "/compressed";
    public static final String ARTIFACT_DOWNLOAD = "/{distributionId}/{name:.+}";

    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Inject
    private BuildRequestService buildRequestService;

    @Inject
    private DownloadTokenService downloadTokenService;

    @Inject
    private DistributionChannelService distributionChannelService;

    /**
     * GET
     */
    @RequestMapping(value = COMPRESSED_DISTRIBUTION_ID,
        method = RequestMethod.GET, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @Timed
    public ResponseEntity getCompressedArtifactsFromDistributtionChannel(@PathVariable("distributionId") Long distributionId, @PathVariable("token") String token) throws IOException {
        return downloadTokenService.extractBuildRequestFromToken(token)
            .map(buildRequestDTO ->
                buildRequestService.getCompressedArtifacts(buildRequestDTO.getId(), distributionId)
                    .map(BuildRequestResource::getFileSystemResourceResponseEntity)
                    .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT)))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET
     */
    @RequestMapping(value = COMPRESSED,
        method = RequestMethod.GET, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @Timed
    public ResponseEntity getCompressedArtifactsFromAllDistributionChannels(@PathVariable("token") String token) throws IOException {
        return getCompressedArtifactsFromDistributtionChannel(null, token);
    }


    /**
     * GET
     */
    @RequestMapping(value = ARTIFACT_DOWNLOAD,
        method = RequestMethod.GET, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @Timed
    public ResponseEntity getArtifactByName(@PathVariable("token") String token, @PathVariable("distributionId") Long distributionId, @PathVariable("name") String name) throws IOException {
        return downloadTokenService.extractBuildRequestFromToken(token)
            .map(buildRequestDTO -> {
                return distributionChannelService.getBuildArtifactByName(buildRequestDTO, distributionId, name)
                    .map(ArtifactDownloadResource::getFileSystemResourceResponseEntityForArtifact)
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); //could not find the artifact
            })
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public static ResponseEntity getFileSystemResourceResponseEntityForArtifact(Artifact artifact) {
        if (artifact.isLocal()) {
            File file = new File(artifact.getUri());
            return BuildRequestResource.getFileSystemResourceResponseEntity(file);
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(artifact.getUri());
            return new ResponseEntity<Void>(headers, HttpStatus.SEE_OTHER);
        }
    }

}
