package org.appverse.builder.web.rest.util;

import org.apache.commons.lang.RandomStringUtils;
import org.appverse.builder.Application;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.dto.DistributionChannelDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.util.UriUtils;

import javax.inject.Inject;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by panthro on 03/03/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class ArtifactDownloadUrlCreatorTest {

    @Inject
    private ArtifactDownloadUrlCreator artifactDownloadUrlCreator;

    private Long distributionChannelId = 5000L;

    private String artifactName = RandomStringUtils.randomAlphanumeric(10) + "." + RandomStringUtils.randomAlphabetic(3);

    private Pattern artifactDownloadPattern = Pattern.compile("^.*/api/artifacts/.+/" + distributionChannelId + "/" + artifactName);

    private Pattern artifactCompressedPattern = Pattern.compile("^.*/api/artifacts/.+/compressed");

    private Pattern artifactCompressedDistributtionPattern = Pattern.compile("^.*/api/artifacts/.+/compressed/" + distributionChannelId);


    @Test
    public void createArtifactDownloadPathHasNoInvalidChars() throws Exception {

        artifactName = "A File With Spáce And Ínvalid % chars & also b@d chars?";

        BuildRequestDTO buildRequestDTO = new BuildRequestDTO();
        buildRequestDTO.setId(1L);
        buildRequestDTO.setCreatedDate(ZonedDateTime.now());
        DistributionChannelDTO distributionChannelDTO = new DistributionChannelDTO();
        distributionChannelDTO.setId(distributionChannelId);

        Artifact artifact = new Artifact(URI.create("file:///tmp/not-a-file"), artifactName, distributionChannelDTO, 0L);
        String artifactDownloadPath = artifactDownloadUrlCreator.createArtifactDownloadPath(buildRequestDTO, artifact);
        String encodedArtifactName = UriUtils.encodePath(artifact.getName(), "UTF-8");
        System.out.println("encodedArtifactName = " + encodedArtifactName);
        assertThat(artifactDownloadPath).doesNotContain(" ");
        assertThat(artifactDownloadPath).contains(encodedArtifactName);

    }


    @Test
    public void createArtifactDownloadPath() throws Exception {
        BuildRequestDTO buildRequestDTO = new BuildRequestDTO();
        buildRequestDTO.setId(1L);
        buildRequestDTO.setCreatedDate(ZonedDateTime.now());
        DistributionChannelDTO distributionChannelDTO = new DistributionChannelDTO();
        distributionChannelDTO.setId(distributionChannelId);
        Artifact artifact = new Artifact(URI.create("file:///tmp/not-a-file"), artifactName, distributionChannelDTO, 0L);

        String artifactDownloadPath = artifactDownloadUrlCreator.createArtifactDownloadPath(buildRequestDTO, artifact);
        assertThat(artifactDownloadPath).matches(artifactDownloadPattern);

    }

    @Test
    public void createCompressedArtifactsDownloadPath() throws Exception {
        BuildRequestDTO buildRequestDTO = new BuildRequestDTO();
        buildRequestDTO.setId(1L);
        buildRequestDTO.setCreatedDate(ZonedDateTime.now());
        String artifactDownloadPath = artifactDownloadUrlCreator.createCompressedArtifactsDownloadPath(buildRequestDTO);
        assertThat(artifactDownloadPath).matches(artifactCompressedPattern);
    }

    @Test
    public void createCompressedArtifactsDownloadPath1() throws Exception {

        BuildRequestDTO buildRequestDTO = new BuildRequestDTO();
        buildRequestDTO.setId(1L);
        buildRequestDTO.setCreatedDate(ZonedDateTime.now());
        String artifactDownloadPath = artifactDownloadUrlCreator.createCompressedArtifactsDownloadPath(buildRequestDTO, distributionChannelId);
        assertThat(artifactDownloadPath).matches(artifactCompressedDistributtionPattern);
    }
}
