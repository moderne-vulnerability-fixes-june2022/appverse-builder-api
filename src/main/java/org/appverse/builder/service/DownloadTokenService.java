package org.appverse.builder.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.text.StrTokenizer;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by panthro on 03/03/16.
 */
@Service
public class DownloadTokenService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String REQUEST_ID = "requestId";
    private static final String EXPIRE_TIMESTAMP = "expireTimestamp";
    private static final String SIGNATURE = "signature";
    private static final char TOKEN_SEPARATOR = ':';

    private static String TOKEN_PATTERN = "${" + REQUEST_ID + "}" + TOKEN_SEPARATOR + "${" + EXPIRE_TIMESTAMP + "}" + TOKEN_SEPARATOR + "${" + SIGNATURE + "}";

    private static String CHARSET = "UTF-8";

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private BuildRequestService buildRequestService;


    public String createToken(BuildRequestDTO buildRequestDTO, long expireTimestamp) {
        String freshToken = new StrSubstitutor(buildTokenValueMap(buildRequestDTO, expireTimestamp)).replace(TOKEN_PATTERN);
        try {
            return Base64.encodeBase64URLSafeString(freshToken.getBytes(CHARSET));
        } catch (UnsupportedEncodingException e) {
            //this will most likely never happen, but we need a fall back
            return Base64.encodeBase64URLSafeString(freshToken.getBytes(Charset.defaultCharset()));
        }
    }

    private Map<String, Object> buildTokenValueMap(BuildRequestDTO buildRequestDTO, long expireTimestamp) {
        String secret = appverseBuilderProperties.getAuth().getDownloadTokenSecret();

        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put(REQUEST_ID, buildRequestDTO.getId());
        valuesMap.put(EXPIRE_TIMESTAMP, expireTimestamp);

        StringBuilder cleanSignature = new StringBuilder(secret);
        valuesMap.values().forEach(o -> cleanSignature.append(o.toString()));
        valuesMap.put(SIGNATURE, DigestUtils.sha256Hex(cleanSignature.toString()));

        return valuesMap;
    }

    public Optional<BuildRequestDTO> extractBuildRequestFromToken(String token) {
        try {
            String cleanToken = new String(Base64.decodeBase64(token), CHARSET);
            List<String> tokenList = new StrTokenizer(cleanToken, TOKEN_SEPARATOR).getTokenList();
            if (tokenList.isEmpty()) {
                log.debug("Trying to access resource with empty token {}", token);
                return Optional.empty();
            }
            if (tokenList.size() < 3) {
                log.debug("Trying to access resource with invalid size token {}", token);
                return Optional.empty();
            }
            Long requestId = Long.valueOf(tokenList.get(0));
            BuildRequestDTO buildRequest = buildRequestService.findOne(requestId);
            if (buildRequest == null) {
                log.debug("Trying to access resource with invalid size token {}", token);
                return Optional.empty();
            }

            Long expireTimestamp = Long.valueOf(tokenList.get(1));
            if (Optional.ofNullable(appverseBuilderProperties.getAuth().getDownloadExpireAfterSeconds()).orElse(0) > 0 && expireTimestamp < System.currentTimeMillis()) {
                return Optional.empty();
            }

            if (!createToken(buildRequest, expireTimestamp).equals(token)) {
                return Optional.empty();
            }
            return Optional.of(buildRequest);

        } catch (Exception e) {
            log.warn("Error extracting build request from token {}", token, e);
            return Optional.empty();
        }

    }

}
