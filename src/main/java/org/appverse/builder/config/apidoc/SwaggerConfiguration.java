package org.appverse.builder.config.apidoc;

import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.StopWatch;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Date;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Springfox Swagger configuration.
 * <p>
 * Warning! When having a lot of REST endpoints, Springfox can become a performance issue. In that
 * case, you can use a specific Spring profile for this class, so that only front-end developers
 * have access to the Swagger view.
 */
@Configuration
@EnableSwagger2
//@Profile("!" + Constants.SPRING_PROFILE_PRODUCTION)
public class SwaggerConfiguration {

    public static final String DEFAULT_INCLUDE_PATTERN = "/api/.*";
    private final Logger log = LoggerFactory.getLogger(SwaggerConfiguration.class);

    /**
     * Swagger Springfox configuration.
     */
    @Bean
    @Profile("!" + Constants.SPRING_PROFILE_FAST)
    public Docket swaggerSpringfoxDocket(AppverseBuilderProperties appverseBuilderProperties) {
        log.debug("Starting Swagger");
        StopWatch watch = new StopWatch();
        watch.start();
        ApiInfo apiInfo = new ApiInfo(
            appverseBuilderProperties.getSwagger().getTitle(),
            appverseBuilderProperties.getSwagger().getDescription(),
            appverseBuilderProperties.getSwagger().getVersion(),
            appverseBuilderProperties.getSwagger().getTermsOfServiceUrl(),
            appverseBuilderProperties.getSwagger().getContact(),
            appverseBuilderProperties.getSwagger().getLicense(),
            appverseBuilderProperties.getSwagger().getLicenseUrl());

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo)
            .genericModelSubstitutes(ResponseEntity.class)
            .forCodeGeneration(true)
            .ignoredParameterTypes(Pageable.class, OAuth2Authentication.class)
            .genericModelSubstitutes(ResponseEntity.class)
            .directModelSubstitute(java.time.LocalDate.class, String.class)
            .directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
            .directModelSubstitute(java.time.LocalDateTime.class, Date.class)
            .select()
            .paths(regex(DEFAULT_INCLUDE_PATTERN))
            .build();
        watch.stop();
        log.debug("Started Swagger in {} ms", watch.getTotalTimeMillis());
        return docket;
    }
}
