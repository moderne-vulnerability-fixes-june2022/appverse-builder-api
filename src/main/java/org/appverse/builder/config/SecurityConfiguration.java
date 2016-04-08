package org.appverse.builder.config;

import org.appverse.builder.config.security.AuthenticationConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

import javax.inject.Inject;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    private Set<AuthenticationConfigurer> configurerSet;

    @Inject
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        configurerSet.forEach(apbSecurityConfigurer -> {
            try {
                apbSecurityConfigurer.configure(auth);
            } catch (Exception e) {
                log.warn("There was an exception configuring {}, this authentication method might not be available.", apbSecurityConfigurer.getClass().getSimpleName(), e);
            }
        });

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
            .antMatchers("/swagger-ui/index.html")
            .antMatchers("/swagger-ui/**")
            .antMatchers("/api/register")
            .antMatchers("/api/activate")
            .antMatchers("/api/account/reset_password/init")
            .antMatchers("/api/account/reset_password/finish")
            .antMatchers("/api/account/config/status")
            .antMatchers("/test/**")
            .antMatchers("/console/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic().realmName("Appverse")
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .requestMatchers().antMatchers("/oauth/authorize")
            .and()
            .authorizeRequests()
            .antMatchers("/oauth/authorize").authenticated();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
