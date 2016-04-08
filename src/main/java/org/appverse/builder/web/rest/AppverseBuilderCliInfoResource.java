package org.appverse.builder.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by panthro on 17/02/16.
 */
@RestController
@RequestMapping("/api/cli")
public class AppverseBuilderCliInfoResource {


    public static final String REGISTRY = "registry";
    public static final String PACKAGE_NAME = "package-name";
    public static final String COMMAND_NAME = "command-name";
    public static final String LOG_NAME = "log-name";
    public static final String BUILD_CONFIG_FILE_NAME = "build-config-file-name";
    public static final String BUILD_IGNORE_FILE_NAME = "build-ignore-file-name";
    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @RequestMapping(value = "/details",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Map<String, Object>> getCLIDetails() {
        Map<String, Object> cliInfo = new HashMap<>();
        cliInfo.put(REGISTRY, appverseBuilderProperties.getCli().getRegistry());
        cliInfo.put(PACKAGE_NAME, appverseBuilderProperties.getCli().getPackageName());
        cliInfo.put(COMMAND_NAME, appverseBuilderProperties.getCli().getCommandName());
        cliInfo.put(LOG_NAME, appverseBuilderProperties.getBuild().getLogFileName());
        cliInfo.put(BUILD_CONFIG_FILE_NAME, appverseBuilderProperties.getBuild().getBuildInfoFileName());
        cliInfo.put(BUILD_IGNORE_FILE_NAME, appverseBuilderProperties.getBuild().getBuildIgnoreFileName());
        return ResponseEntity.ok(cliInfo);
    }
}
