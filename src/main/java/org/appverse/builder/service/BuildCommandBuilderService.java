package org.appverse.builder.service;

import org.appverse.builder.build.comand.BuildCommand;
import org.appverse.builder.build.comand.BuildCommandBuilder;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by panthro on 19/01/16.
 */
@Service
public class BuildCommandBuilderService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private ApplicationContext context;


    public Optional<BuildCommand> buildCommandFor(BuildAgentDTO buildAgent, BuildRequestDTO buildRequest) {
        return getBuildCommandBuilders().stream()
            .filter(buildCommandBuilder -> buildCommandBuilder.agentSupports(buildAgent))
            .filter(buildCommandBuilder -> buildCommandBuilder.canBuildForRequest(buildRequest))
            .findAny().map(buildCommandBuilder -> buildCommandBuilder.buildCommand(buildAgent, buildRequest));

    }

    public boolean agentCanBuildRequest(BuildAgentDTO buildAgent, BuildRequestDTO buildRequest) {
        return getBuildCommandBuilders().stream()
            .filter(buildCommandBuilder -> buildCommandBuilder.agentSupports(buildAgent))
            .filter(buildCommandBuilder -> buildCommandBuilder.canBuildForRequest(buildRequest))
            .findAny()
            .isPresent();
    }

    public Collection<BuildCommandBuilder> getBuildCommandBuilders() {
        return context.getBeansOfType(BuildCommandBuilder.class).values();
    }
}
