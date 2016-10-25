package org.appverse.builder.web.rest.util;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.service.DownloadTokenService;
import org.appverse.builder.web.rest.ArtifactDownloadResource;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by panthro on 03/03/16.
 */
@Component
public class ArtifactDownloadUrlCreator {

    private static final String API_DOWNLOAD_ARTIFACT = (ArtifactDownloadResource.API_BASE + ArtifactDownloadResource.ARTIFACT_DOWNLOAD.replace(":.+", ""));
    private static final String API_DOWNLOAD_COMPRESSED = (ArtifactDownloadResource.API_BASE + ArtifactDownloadResource.COMPRESSED);
    private static final String API_DOWNLOAD_COMPRESSED_DISTRIBUTION_ID = ArtifactDownloadResource.API_BASE + ArtifactDownloadResource.COMPRESSED_DISTRIBUTION_ID;
    private static final String API_MANIFEST_PLIST = ArtifactDownloadResource.API_BASE + ArtifactDownloadResource.IPA_MANIFEST.replace(":.+\\.ipa", "");

    @Inject
    AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    DownloadTokenService downloadTokenService;

    public String createArtifactDownloadPath(BuildRequestDTO buildRequestDTO, Artifact artifact) {
        String artifactName = artifact.getName();
        try {
            artifactName = URLEncoder.encode(artifact.getName(), "UTF-8");
        } catch (Exception e) {
            LoggerFactory.getLogger(ArtifactDownloadUrlCreator.class).warn("Error encoding artifact name {}", artifact);
        }
        return appverseBuilderProperties.getBaseUrl() + StrSubstitutor.replace(API_DOWNLOAD_ARTIFACT, buildReplaceMap(buildRequestDTO, artifact.getDistributionChannelId(), artifactName), "{", "}");
    }


    public String createCompressedArtifactsDownloadPath(BuildRequestDTO buildRequestDTO) {
        return appverseBuilderProperties.getBaseUrl() + StrSubstitutor.replace(API_DOWNLOAD_COMPRESSED, buildReplaceMap(buildRequestDTO, null, null), "{", "}");
    }

    public String createCompressedArtifactsDownloadPath(BuildRequestDTO buildRequestDTO, Long distributionChannelId) {
        return appverseBuilderProperties.getBaseUrl() + StrSubstitutor.replace(API_DOWNLOAD_COMPRESSED_DISTRIBUTION_ID, buildReplaceMap(buildRequestDTO, distributionChannelId, null), "{", "}");
    }

    public String createManifestPlistDownloadPath(String token, Artifact artifact) {
        return appverseBuilderProperties.getBaseUrl() + StrSubstitutor.replace(API_MANIFEST_PLIST, buildReplaceMap(token, artifact.getDistributionChannelId(), artifact.getName()), "{", "}");
    }


    private Map<String, Object> buildReplaceMap(BuildRequestDTO buildRequestDTO, Long distributionChannelId, String artifactName) {
        return buildReplaceMap(downloadTokenService.createToken(buildRequestDTO, getExpiryTimestamp()), distributionChannelId, artifactName);
    }

    private Map<String, Object> buildReplaceMap(String token, Long distributionChannelId, String artifactName) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("token", token);
        valuesMap.put("distributionId", distributionChannelId);
        valuesMap.put("name", artifactName);
        return valuesMap;
    }

    public long getExpiryTimestamp() {
        return DateTime.now().plusSeconds(appverseBuilderProperties.getAuth().getDownloadExpireAfterSeconds()).getMillis();
    }


}
