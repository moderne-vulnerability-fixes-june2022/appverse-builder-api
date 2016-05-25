package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by panthro on 19/05/16.
 */
@RestController
@RequestMapping("/api")
public class AuthTokenResource {

    public static final String TOKEN_NAME = "token_name";
    private final Logger log = LoggerFactory.getLogger(AuthTokenResource.class);

    @Inject
    private TokenStore tokenStore;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;


    @RequestMapping(value = "/tokens",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.USER)
    public ResponseEntity<Collection<OAuth2AccessToken>> getTokens() throws URISyntaxException {
        log.warn("REST request to get tokens");
        Collection<OAuth2AccessToken> oAuth2AccessTokens = tokenStore.findTokensByClientIdAndUserName(appverseBuilderProperties.getAuth().getNonExpireTokenClientId(), SecurityUtils.getCurrentUserLogin());
        return ResponseEntity.ok(oAuth2AccessTokens);
    }


    @RequestMapping(value = "/tokens",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.USER)
    public ResponseEntity<Void> revokeToken(@RequestParam("token") String token) throws URISyntaxException {
        log.warn("REST request to revoke token {}", token);
        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(token);
        Optional.ofNullable(oAuth2Authentication)
            .filter(auth -> auth.getPrincipal() instanceof User && ((User) auth.getPrincipal()).getUsername().equals(SecurityUtils.getCurrentUserLogin()))
            .ifPresent(auth2 -> Optional.ofNullable(tokenStore.readAccessToken(token)).ifPresent(tokenStore::removeAccessToken));

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
