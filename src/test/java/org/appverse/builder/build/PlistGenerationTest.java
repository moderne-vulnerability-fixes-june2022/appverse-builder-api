package org.appverse.builder.build;

import org.appverse.builder.web.rest.util.BundleInformationExtractor;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by panthro on 24/05/16.
 */
public class PlistGenerationTest {


    private static final String BUNDLE_ID = "org.appverse.builder.hello-world";
    private static final String BUNDLE_VERSION = "1.0";

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testGetBundleInformationFromIpa() throws Exception {

        URL resource = getClass().getResource("/ota/helloworld.ipa");
        assertThat(resource).isNotNull();
        File ipa = new File(resource.getFile());
        assertThat(ipa.exists());
        assertThat(ipa.canRead());

        Optional<BundleInformationExtractor.BundleInformation> bundleInformation = BundleInformationExtractor.getIpaInformationFromLocalfile(ipa);

        assertThat(bundleInformation).isNotNull();
        assertThat(bundleInformation).isPresent();
        assertThat(bundleInformation.get().getBundleId()).isEqualTo(BUNDLE_ID);
        assertThat(bundleInformation.get().getBundleVersion()).isEqualTo(BUNDLE_VERSION);

    }
}
