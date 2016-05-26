package org.appverse.builder.web.rest.util;

import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by panthro on 24/05/16.
 */
public class BundleInformationExtractor {

    private static final String CF_BUNDLE_VERSION = "CFBundleVersion";
    private static final String CF_BUNDLE_IDENTIFIER = "CFBundleIdentifier";

    private static final Logger LOG = LoggerFactory.getLogger(BundleInformationExtractor.class);
    public static final String BUNDLE_IDENTIFIER = "bundle-identifier";
    public static final String BUNDLE_VERSION = "bundle-version";


    @SuppressWarnings("unchecked")
    public static Optional<BundleInformation> getIpaInformationFromLocalfile(File ipaFile) throws ZipException {
        LOG.debug("Request to get ipa information from {}", ipaFile);
        ZipFile ipaAsZip = new ZipFile(ipaFile);

        Stream<FileHeader> filteredStream = ipaAsZip.getFileHeaders().stream()
            .filter(FileHeader.class::isInstance);

        return filteredStream
            .filter(fileHeader -> fileHeader.getFileName().matches(".*\\.plist"))
            .map(fileHeader -> {
                Optional<BundleInformation> result;
                try {
                    ZipInputStream zipInputStream = ipaAsZip.getInputStream(fileHeader);
                    result = getBundleInformationFromPlistStream(zipInputStream);
                } catch (Exception e) {
                    LOG.info("There was an error trying to get input stream from file {} inside ipa {} ", fileHeader, ipaFile);
                    result = Optional.empty();
                }
                return result;
            }).filter(Optional::isPresent)
            .map(Optional::get)
            .findAny();
    }

    public static Optional<BundleInformation> getBundleInformationFromPlistStream(InputStream plistStream) throws IOException, PropertyListFormatException, ParseException, ParserConfigurationException, SAXException {
        String plist = PropertyListParser.parse(plistStream).toXMLPropertyList();

        Optional<String> bundleId = findByKeyInPlist(plist, CF_BUNDLE_IDENTIFIER);
        bundleId = bundleId.isPresent() ? bundleId : findByKeyInPlist(plist, BUNDLE_IDENTIFIER);
        Optional<String> bundleVersion = findByKeyInPlist(plist, CF_BUNDLE_VERSION);
        bundleVersion = bundleVersion.isPresent() ? bundleVersion : findByKeyInPlist(plist, BUNDLE_VERSION);
        if (bundleId.isPresent() && bundleVersion.isPresent()) {
            BundleInformation bundleInformation = new BundleInformation(bundleId.get(), bundleVersion.get());
            LOG.debug("Found Bundle information {}", bundleInformation);
            return Optional.of(bundleInformation);
        }
        return Optional.empty();
    }

    private static Optional<String> findByKeyInPlist(String plist, String key) {
        Matcher matcher = Pattern.compile(".+<key>" + key + "</key>[\\r\\n\\t\\s]*<string>(.*)</string>.*", Pattern.MULTILINE).matcher(plist);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1));
        }
        return Optional.empty();
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
