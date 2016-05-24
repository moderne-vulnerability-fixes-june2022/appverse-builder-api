package org.appverse.builder.web.rest.util;

import com.dd.plist.PropertyListParser;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by panthro on 24/05/16.
 */
public class BundleInformationExtractor {

    private static final String CF_BUNDLE_VERSION = "CFBundleVersion";
    private static final String CF_BUNDLE_IDENTIFIER = "CFBundleIdentifier";

    private static final Logger LOG = LoggerFactory.getLogger(BundleInformationExtractor.class);


    @SuppressWarnings("unchecked")
    public static Optional<BundleInformation> getIpaInformationFromLocalfile(File ipaFile) throws ZipException {
        LOG.debug("Request to get ipa information from {}", ipaFile);
        ZipFile ipaAsZip = new ZipFile(ipaFile);

        Stream<FileHeader> filteredStream = ipaAsZip.getFileHeaders().stream()
            .filter(FileHeader.class::isInstance);

        return filteredStream
            .filter(fileHeader -> fileHeader.getFileName().matches(".*\\.plist"))
            .map(fileHeader -> {
                try {
                    ZipInputStream zipInputStream = ipaAsZip.getInputStream(fileHeader);
                    Object o = PropertyListParser.parse(zipInputStream).toJavaObject();
                    if (o instanceof HashMap) {
                        HashMap<String, String> map = (HashMap<String, String>) o;
                        if (map.containsKey(CF_BUNDLE_IDENTIFIER) && map.containsKey(CF_BUNDLE_VERSION)) {
                            BundleInformation bundleInformation = new BundleInformation(map.get(CF_BUNDLE_IDENTIFIER), map.get(CF_BUNDLE_VERSION));
                            LOG.debug("Found Bundle information {}", bundleInformation);
                            return bundleInformation;
                        }
                    }
                } catch (Exception e) {
                    LOG.info("There was an error trying to get input stream from file {} inside ipa {} ", fileHeader, ipaFile);
                }
                return null;
            }).filter(ipaInformation -> ipaInformation != null)
            .findAny();
    }


    public static class BundleInformation {
        private String bundleId, bundleVersion;


        public BundleInformation(String bundleId, String bundleVersion) {
            this.bundleId = bundleId;
            this.bundleVersion = bundleVersion;
        }

        public String getBundleId() {
            return bundleId;
        }

        public void setBundleId(String bundleId) {
            this.bundleId = bundleId;
        }

        public String getBundleVersion() {
            return bundleVersion;
        }

        public void setBundleVersion(String bundleVersion) {
            this.bundleVersion = bundleVersion;
        }

        @Override
        public String toString() {
            return "BundleInformation{" +
                "bundleId='" + bundleId + '\'' +
                ", bundleVersion='" + bundleVersion + '\'' +
                '}';
        }
    }
}
