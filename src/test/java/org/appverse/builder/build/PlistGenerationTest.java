package org.appverse.builder.build;

import org.appverse.builder.Application;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.web.rest.ArtifactDownloadResource;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.util.ArtifactDownloadUrlCreator;
import org.appverse.builder.web.rest.util.BundleInformationExtractor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by panthro on 24/05/16.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class PlistGenerationTest {


    private static final String BUNDLE_ID = "org.appverse.builder.hello-world";
    private static final String BUNDLE_VERSION = "1.0";


    @Inject
    private ArtifactDownloadUrlCreator artifactDownloadUrlCreator;
    @Inject
    private ArtifactDownloadResource artifactDownloadResource;


    @Before
    public void setup() {

        ReflectionTestUtils.setField(artifactDownloadResource, "artifactDownloadUrlCreator", artifactDownloadUrlCreator);
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testGetBundleInformationFromIpa() throws Exception {

        File ipa = getSampleIpaAsFile();

        Optional<BundleInformationExtractor.BundleInformation> bundleInformation = BundleInformationExtractor.getIpaInformationFromLocalfile(ipa);

        assertThat(bundleInformation).isNotNull();
        assertThat(bundleInformation).isPresent();
        assertThat(bundleInformation.get().getBundleId()).isEqualTo(BUNDLE_ID);
        assertThat(bundleInformation.get().getBundleVersion()).isEqualTo(BUNDLE_VERSION);

    }

    @Test
    public void testGeneratedPlist() throws Exception {

        BuildRequestDTO buildRequestDTO = new BuildRequestDTO();
        buildRequestDTO.setId(1L);
        buildRequestDTO.setCreatedDate(ZonedDateTime.now());
        File ipaAsFile = getSampleIpaAsFile();
        Artifact artifact = new Artifact(ipaAsFile.toURI(), ipaAsFile.getName(), ipaAsFile.length());
        Optional<InputStream> plistStream = artifactDownloadResource.getPlistManifestAsStream(buildRequestDTO, artifact);
        assertThat(plistStream).isPresent();

        Optional<BundleInformationExtractor.BundleInformation> bundleInformation = BundleInformationExtractor.getBundleInformationFromPlistStream(plistStream.get());

        assertThat(bundleInformation).isPresent();

        assertThat(bundleInformation.get().getBundleId()).isEqualTo(BUNDLE_ID);
        assertThat(bundleInformation.get().getBundleVersion()).isEqualTo(BUNDLE_VERSION);


    }

    private File getSampleIpaAsFile() {
        URL resource = getClass().getResource("/ota/helloworld.ipa");
        assertThat(resource).isNotNull();
        File ipa = new File(resource.getFile());
        assertThat(ipa.exists());
        assertThat(ipa.canRead());
        return ipa;
    }
}
