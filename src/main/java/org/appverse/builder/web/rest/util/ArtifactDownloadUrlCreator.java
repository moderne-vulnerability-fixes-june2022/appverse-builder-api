package org.appverse.builder.web.rest.util;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.service.DownloadTokenService;
import org.appverse.builder.web.rest.ArtifactDownloadResource;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
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

    @Inject
    AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    DownloadTokenService downloadTokenService;

    public String createArtifactDownloadPath(BuildRequestDTO buildRequestDTO, Artifact artifact) {
        return appverseBuilderProperties.getBaseUrl() + StrSubstitutor.replace(API_DOWNLOAD_ARTIFACT, buildReplaceMap(buildRequestDTO, artifact.getDistributionChannelId(), artifact.getName()), "{", "}");
    }


    public String createCompressedArtifactsDownloadPath(BuildRequestDTO buildRequestDTO) {
        return appverseBuilderProperties.getBaseUrl() + StrSubstitutor.replace(API_DOWNLOAD_COMPRESSED, buildReplaceMap(buildRequestDTO, null, null), "{", "}");
    }

    public String createCompressedArtifactsDownloadPath(BuildRequestDTO buildRequestDTO, Long distributionChannelId) {
        return appverseBuilderProperties.getBaseUrl() + StrSubstitutor.replace(API_DOWNLOAD_COMPRESSED_DISTRIBUTION_ID, buildReplaceMap(buildRequestDTO, distributionChannelId, null), "{", "}");
    }


    private Map<String, Object> buildReplaceMap(BuildRequestDTO buildRequestDTO, Long distributionChannelId, String artifactName) {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("token", downloadTokenService.createToken(buildRequestDTO, getExpiryTimestamp()));
        valuesMap.put("distributionId", distributionChannelId);
        valuesMap.put("name", artifactName);
        return valuesMap;
    }

    public long getExpiryTimestamp() {
        return DateTime.now().plusSeconds(appverseBuilderProperties.getAuth().getDownloadExpireAfterSeconds()).getMillis();
    }


}
