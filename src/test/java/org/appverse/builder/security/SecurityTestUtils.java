package org.appverse.builder.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by panthro on 31/12/15.
 */
public class SecurityTestUtils {


    public static final String USER_LOGIN = "user";
    public static final String USER_PASSWORD = "user";
    public static final String ADMIN_LOGIN = "admin";
    public static final String ADMIN_PASSWORD = "admin";

    public static void loginAs(String user, String password) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(user, password, Collections.emptyList()), user));
        SecurityContextHolder.setContext(securityContext);
        boolean isAuthenticated = SecurityUtils.isAuthenticated();
        assertThat(isAuthenticated).isTrue();
    }

    public static void loginAsRegularUser() {
        loginAs(USER_LOGIN, USER_PASSWORD);
    }

    public static void loginAsAdmin() {
        loginAs(ADMIN_LOGIN, ADMIN_PASSWORD);
    }
}
