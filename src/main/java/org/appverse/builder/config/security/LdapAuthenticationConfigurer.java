package org.appverse.builder.config.security;

import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.domain.User;
import org.appverse.builder.security.AuthoritiesConstants;
import org.appverse.builder.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.ppolicy.PasswordPolicyAwareContextSource;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by panthro on 01/02/16.
 */

@Configuration
@ConditionalOnProperty(name = "apb.auth.ldap.enabled", havingValue = "true")
public class LdapAuthenticationConfigurer implements AuthenticationConfigurer {


    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private UserDetailsService userDetailsService;

    @Inject
    private UserService userService;


    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder> configurer = auth.ldapAuthentication()
            .userDetailsContextMapper(userDetailsContextMapper())
            .ldapAuthoritiesPopulator(ldapAuthoritiesPopulator());
        if (ldapProperties().getUserDnPattern() != null) {
            configurer.userDnPatterns(ldapProperties().getUserDnPattern());
        }
        if (ldapProperties().getUserSearchBase() != null) {
            configurer.userSearchBase(ldapProperties().getUserSearchBase());
        }
        if (ldapProperties().getUserSearchFilter() != null) {
            configurer.userSearchFilter(ldapProperties().getUserSearchFilter());
        }

        configurer.contextSource(contextSource());
    }

    @Bean
    public UserDetailsContextMapper userDetailsContextMapper() {
        return new LdapUserDetailsMapper() {

            @Override
            public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
            }

            @Override
            public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
                UserDetails userDetails = super.mapUserFromContext(ctx, username, authorities);
                try {
                    return userDetailsService.loadUserByUsername(username);
                } catch (UsernameNotFoundException e) {
                    Object emailValue = ctx.getObjectAttribute(ldapProperties().getEmailAttribute());
                    if (!(emailValue instanceof String)) {
                        // Assume it's binary
                        emailValue = new String((byte[]) emailValue);
                    }
                    Object firstName = ctx.getObjectAttribute(ldapProperties().getFirstNameAttribute());
                    if (!(firstName instanceof String) && firstName != null) {
                        // Assume it's binary
                        firstName = new String((byte[]) firstName);
                    }
                    Object lastName = ctx.getObjectAttribute(ldapProperties().getLastNameAttribute());
                    if (!(lastName instanceof String) && lastName != null) {
                        // Assume it's binary
                        lastName = new String((byte[]) lastName);
                    }
                    if (firstName == null || lastName == null) {
                        Object fullName = ctx.getObjectAttribute(ldapProperties().getFullNameAttribute());
                        if (!(fullName instanceof String) && fullName != null) {
                            // Assume it's binary
                            fullName = new String((byte[]) fullName);
                        }
                        if (fullName != null) {
                            String[] names = ((String) fullName).split(" ");
                            if (names.length > 0) {
                                lastName = names[names.length - 1];
                            }
                            if (names.length > 1) {
                                firstName = String.join(" ", (CharSequence[]) Arrays.copyOf(names, names.length - 1));
                            }
                        }
                    }
                    final User user = userService.createUserInformation(userDetails.getUsername(), Optional.ofNullable(userDetails.getPassword()).orElse(UUID.randomUUID().toString()), String.valueOf(firstName), String.valueOf(lastName), String.valueOf(emailValue), "en");
                    userService.activateRegistration(user.getActivationKey());
                }
                return userDetailsService.loadUserByUsername(username);
            }
        };

    }

    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator() {
        DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource(), ldapProperties().getGroupSearchBase());
        authoritiesPopulator.setDefaultRole(AuthoritiesConstants.USER);
        if (ldapProperties().getGroupRoleAttribute() != null) {
            authoritiesPopulator.setGroupRoleAttribute(ldapProperties().getGroupRoleAttribute());
        }
        if (ldapProperties().getGroupSearchFilter() != null) {
            authoritiesPopulator.setGroupSearchFilter(ldapProperties().getGroupSearchFilter());
        }
        return authoritiesPopulator;
    }


    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(contextSource());
    }

    private AppverseBuilderProperties.Auth.Ldap ldapProperties() {
        return appverseBuilderProperties.getAuth().getLdap();
    }

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new PasswordPolicyAwareContextSource(ldapProperties().getUrl());
        if (ldapProperties().getUserDn() != null) {
            contextSource.setUserDn(ldapProperties().getUserDn());
        }
        if (ldapProperties().getPassword() != null) {
            contextSource.setPassword(ldapProperties().getPassword());
        }
        contextSource.setAnonymousReadOnly(ldapProperties().isAnonymousReadOnly());
        contextSource.setBase(ldapProperties().getBase());
        return contextSource;
    }
}
