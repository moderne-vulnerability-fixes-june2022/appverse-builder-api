package org.appverse.builder.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.appverse.builder.Application;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.domain.BuildChain;
import org.appverse.builder.domain.enumeration.BuildStatus;
import org.appverse.builder.repository.BuildChainRepository;
import org.appverse.builder.security.SecurityTestUtils;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.util.ArtifactDownloadUrlCreator;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by panthro on 03/03/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class DownloadTokenServiceTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    private DownloadTokenService downloadTokenService;

    @Inject
    private ArtifactDownloadUrlCreator artifactDownloadUrlCreator;

    @Inject
    private BuildRequestService buildRequestService;

    @Inject
    private BuildChainRepository buildChainRepository;

    @Inject
    private UserService userService;

    @Inject
    AppverseBuilderProperties appverseBuilderProperties;

    @PostConstruct
    private void init() {
        SecurityTestUtils.loginAsRegularUser();
        BuildChain buildChain = new BuildChain();
        buildChain.setRequester(userService.getUserWithAuthorities());
        buildChainRepository.save(buildChain);
    }


    @Test
    public void testTokenIsAlwaysValidWhenExpiryIsZero() throws InterruptedException {

        BuildRequestDTO saved = createBuildRequest();
        assertThat(saved.getId()).isNotNull();


        appverseBuilderProperties.getAuth().setDownloadExpireAfterSeconds(0);

        //make sure time passes at least 2 secs

        Thread.sleep(2000);

        String token = downloadTokenService.createToken(saved, artifactDownloadUrlCreator.getExpiryTimestamp());
        log.debug("Generated token: {}", token);

        Optional<BuildRequestDTO> buildRequestDTOOptional = downloadTokenService.extractBuildRequestFromToken(token);
        assertThat(buildRequestDTOOptional).isPresent();

        assertThat(buildRequestDTOOptional).contains(saved);

        //test old tokens also not expire
        token = downloadTokenService.createToken(saved, DateTime.now().minusDays(2).getMillis());


        buildRequestDTOOptional = downloadTokenService.extractBuildRequestFromToken(token);
        assertThat(buildRequestDTOOptional).isPresent();

        assertThat(buildRequestDTOOptional).contains(saved);

        //test it expires if date is then set
        appverseBuilderProperties.getAuth().setDownloadExpireAfterSeconds(10000);

        buildRequestDTOOptional = downloadTokenService.extractBuildRequestFromToken(token);
        assertThat(buildRequestDTOOptional).isEmpty();

    }


    @Test
    public void testTokenIsValid() {

        BuildRequestDTO saved = createBuildRequest();

        assertThat(saved.getId()).isNotNull();

        String token = downloadTokenService.createToken(saved, DateTime.now().plusSeconds(appverseBuilderProperties.getAuth().getDownloadExpireAfterSeconds()).getMillis());
        log.debug("Generated token: {}", token);

        Optional<BuildRequestDTO> buildRequestDTOOptional = downloadTokenService.extractBuildRequestFromToken(token);
        assertThat(buildRequestDTOOptional).isPresent();

        assertThat(buildRequestDTOOptional).contains(saved);

    }

    private BuildRequestDTO createBuildRequest() {
        BuildRequestDTO buildRequestDTO = new BuildRequestDTO();
        buildRequestDTO.setEngine(RandomStringUtils.randomAlphanumeric(5));
        buildRequestDTO.setPlatform(RandomStringUtils.randomAlphanumeric(5));
        buildRequestDTO.setFlavor(RandomStringUtils.randomAlphanumeric(5));
        buildRequestDTO.setChainId(buildChainRepository.findAll().stream().findAny().get().getId());
        buildRequestDTO.setStatus(BuildStatus.SUCCESSFUL);
        buildRequestDTO.setStartTime(ZonedDateTime.now());

        return buildRequestService.save(buildRequestDTO);
    }

    @Test
    public void textTokenExpired() {

        BuildRequestDTO saved = createBuildRequest();

        assertThat(saved.getId()).isNotNull();

        String token = downloadTokenService.createToken(saved, DateTime.now().plusSeconds(-1).getMillis());
        log.debug("Generated token: {}", token);


        Optional<BuildRequestDTO> buildRequestDTOOptional = downloadTokenService.extractBuildRequestFromToken(token);
        assertThat(buildRequestDTOOptional).isEmpty();

    }

    @Test
    public void textSecretUpdate() {

        BuildRequestDTO saved = createBuildRequest();

        assertThat(saved.getId()).isNotNull();

        String token = downloadTokenService.createToken(saved, DateTime.now().plusSeconds(appverseBuilderProperties.getAuth().getDownloadExpireAfterSeconds()).getMillis());
        log.debug("Generated token: {}", token);

        appverseBuilderProperties.getAuth().setDownloadTokenSecret(RandomStringUtils.random(20));

        Optional<BuildRequestDTO> buildRequestDTOOptional = downloadTokenService.extractBuildRequestFromToken(token);
        assertThat(buildRequestDTOOptional).isEmpty();

    }

    @Test
    public void textSignatureManipulation() throws UnsupportedEncodingException {

        BuildRequestDTO saved = createBuildRequest();

        assertThat(saved.getId()).isNotNull();

        String token = downloadTokenService.createToken(saved, DateTime.now().plusSeconds(appverseBuilderProperties.getAuth().getDownloadExpireAfterSeconds()).getMillis());
        final String originalToken = token;
        log.debug("Generated token: {}", token);
        token = new String(Base64.decodeBase64(token), "UTF-8");
        log.debug("Decoded token: {}", token);
        char[] chars = token.toCharArray();
        chars[chars.length - 1] = RandomStringUtils.randomAlphanumeric(1).charAt(0);
        token = new String(chars);
        log.debug("Manipulated token: {}", token);
        token = Base64.encodeBase64URLSafeString(token.getBytes("UTF-8"));
        log.debug("Encoded token: {}", token);
        appverseBuilderProperties.getAuth().setDownloadTokenSecret(RandomStringUtils.random(20));

        assertThat(originalToken).isNotEqualTo(token);

        Optional<BuildRequestDTO> buildRequestDTOOptional = downloadTokenService.extractBuildRequestFromToken(token);
        assertThat(buildRequestDTOOptional).isEmpty();

    }

}
