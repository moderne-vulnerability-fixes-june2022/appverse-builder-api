package org.appverse.builder.config;

import org.appverse.builder.security.AjaxLogoutSuccessHandler;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.security.Http401UnauthorizedEntryPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.appverse.builder.web.rest.AuthTokenResource.TOKEN_NAME;

@Configuration
public class OAuth2ServerConfiguration {

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Inject
        private Http401UnauthorizedEntryPoint authenticationEntryPoint;

        @Inject
        private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(ajaxLogoutSuccessHandler)
                .and()
                .csrf()
                .disable()
                .headers()
                .frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/health").permitAll()
                .antMatchers("/api/cli/details").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/artifacts/**").permitAll()
                .antMatchers("/api/logs/**").hasAnyAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/api/**").authenticated()
                .antMatchers("/metrics/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/health/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/trace/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/dump/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/shutdown/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/beans/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/configprops/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/info/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/autoconfig/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/env/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/trace/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/liquibase/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/api-docs/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/protected/**").authenticated();

        }
    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Inject
        private DataSource dataSource;

        @Inject
        private AppverseBuilderProperties appverseBuilderProperties;
        @Inject
        @Qualifier("authenticationManagerBean")
        private AuthenticationManager authenticationManager;

        @Bean
        public TokenStore tokenStore() {

            JdbcTokenStore jdbcTokenStore = new JdbcTokenStore(dataSource) {
                @Override
                public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
                    OAuth2AccessToken existingAccessToken = super.getAccessToken(authentication);
                    //for Named Tokens we should never return an existing token that has a different name than requested
                    String tokenName = authentication.getOAuth2Request().getRequestParameters().get(TOKEN_NAME);
                    if (tokenName != null && existingAccessToken != null){
                        if (!tokenName.equals(existingAccessToken.getAdditionalInformation().get(TOKEN_NAME))) {
                            return null;
                        }
                    }
                    return existingAccessToken;
                }
            };

            jdbcTokenStore.setAuthenticationKeyGenerator(new DefaultAuthenticationKeyGenerator() {
                @Override
                public String extractKey(OAuth2Authentication authentication) {
                    String key = super.extractKey(authentication);
                    return Optional.ofNullable(authentication.getOAuth2Request().getRequestParameters().get(TOKEN_NAME))
                        .map(s -> key + "-" + s)
                        .orElse(key);
                }
            });
            return jdbcTokenStore;
        }

        @Bean
        public TokenEnhancer tokenEnhancer() {
            return (accessToken, authentication) -> {
                Optional.ofNullable(authentication.getOAuth2Request())
                    .ifPresent(oAuth2Request -> Optional.ofNullable(oAuth2Request.getRequestParameters())
                        .ifPresent(params -> Optional.ofNullable(params.get(TOKEN_NAME))
                            .ifPresent(name -> {
                                if (accessToken instanceof DefaultOAuth2AccessToken) {
                                    Map<String, Object> additionalInformation = new HashMap<>(accessToken.getAdditionalInformation());
                                    additionalInformation.put(TOKEN_NAME, name);
                                    ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInformation);
                                }
                            })));
                return accessToken;
            };
        }


        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints)
            throws Exception {

            endpoints
                .tokenStore(tokenStore())
                .tokenEnhancer(tokenEnhancer())
                .authenticationManager(authenticationManager);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer.allowFormAuthenticationForClients();
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients
                .jdbc(dataSource)
            ;
        }
    }
}
