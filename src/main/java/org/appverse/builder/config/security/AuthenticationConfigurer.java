package org.appverse.builder.config.security;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

/**
 * Created by panthro on 01/02/16.
 */
public interface AuthenticationConfigurer {


    /**
     * Configure the authentication manager builder
     *
     * @param authenticationManagerBuilder
     */
    void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception;
}
