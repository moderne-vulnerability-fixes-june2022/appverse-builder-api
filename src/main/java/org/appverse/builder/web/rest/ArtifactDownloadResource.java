package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.service.BuildRequestService;
import org.appverse.builder.service.DistributionChannelService;
import org.appverse.builder.service.DownloadTokenService;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.util.ArtifactDownloadUrlCreator;
import org.appverse.builder.web.rest.util.BundleInformationExtractor;
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
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public static final String IPA_MANIFEST = "/{distributionId}/{name:.+\\.ipa}/manifest.plist";
    public static final String PLIST_TEMPLATE = "/ota/manifest.plist.vm";
    public static final String BUNDLE_IDENTIFIER = "BUNDLE_IDENTIFIER";
    public static final String BUNDLE_VERSION = "BUNDLE_VERSION";
    public static final String IPA_URL = "IPA_URL";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private BuildRequestService buildRequestService;

    @Inject
    private DownloadTokenService downloadTokenService;

    @Inject
    private DistributionChannelService distributionChannelService;

    @Inject
    private ArtifactDownloadUrlCreator artifactDownloadUrlCreator;

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

    /**
     * GET
     */
    @RequestMapping(value = IPA_MANIFEST,
        method = RequestMethod.GET, produces = {MediaType.TEXT_XML_VALUE})
    @Timed
    public ResponseEntity<String> getIpaManifestPlist(@PathVariable("token") String token, @PathVariable("distributionId") Long distributionId, @PathVariable("name") String name) throws IOException {
        return downloadTokenService.extractBuildRequestFromToken(token)
            .map(buildRequestDTO -> {
                return distributionChannelService.getBuildArtifactByName(buildRequestDTO, distributionId, name)
                    .map(artifact -> getManifestPlistForArtifact(buildRequestDTO, artifact))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); //could not find the artifact
            })
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private ResponseEntity<String> getManifestPlistForArtifact(BuildRequestDTO buildRequestDTO, Artifact artifact) {
        return getPlistManifestAsStream(buildRequestDTO, artifact)
            .map(inputStream -> {
                try {
                    return new ResponseEntity<>(IOUtils.toString(inputStream), HttpStatus.OK);
                } catch (IOException e) {
                    log.warn("Error consuming plist input stream",e);
                    return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            })
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    public Optional<InputStream> getPlistManifestAsStream(BuildRequestDTO buildRequest, Artifact artifact) {
        final Map<String, String> context = new HashMap<>();

        try {
            Optional<BundleInformationExtractor.BundleInformation> bundleInformation = BundleInformationExtractor.getIpaInformationFromLocalfile(new File(artifact.getUri()));
            if (!bundleInformation.isPresent()) {
                log.warn("Could not find bundle information inside ipa {}", artifact);
                return Optional.empty();
            }
            context.put(BUNDLE_IDENTIFIER, bundleInformation.get().getBundleId());
            context.put(BUNDLE_VERSION, bundleInformation.get().getBundleVersion());
        } catch (ZipException e) {
            log.warn("Could not extract bundle information from ipa {}", artifact, e);
            return Optional.empty();
        }

        context.put(IPA_URL, artifact.isLocal() ? artifactDownloadUrlCreator.createArtifactDownloadPath(buildRequest, artifact) : artifact.getUri().toString());
//        context.put("DISPLAY_IMAGE_URL", "http://imagedomain.com/image.png");

        Reader reader = new InputStreamReader(getClass().getResourceAsStream(PLIST_TEMPLATE));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(outputStream)) {
            boolean success = Velocity.evaluate(new VelocityContext(context), writer, "PLIST-GENERATOR", reader);
            if (!success) {
                return Optional.empty();
            }

        } catch (IOException e) {
            log.warn("Could not write plist", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }

        return Optional.of(new BufferedInputStream(new ByteArrayInputStream(outputStream.toByteArray())));
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
