package org.appverse.builder.build.comand.docker;

import org.apache.commons.lang.RandomStringUtils;
import org.appverse.builder.build.comand.BuildCommandBuilder;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.service.BuildAgentService;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

import static org.appverse.builder.domain.enumeration.ImageType.DOCKER;

/**
 * Created by panthro on 19/01/16.
 */
@Component
public class DockerBuildCommandBuilder implements BuildCommandBuilder<DockerCommand> {

    public static final String DOCKER_AGENT_KEY = "docker";

    @Inject
    private BuildAgentService buildAgentService;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Override
    public DockerCommand buildCommand(BuildAgentDTO buildAgent, BuildRequestDTO buildRequest) {

        DockerCommand dockerCommand = new DockerCommand();
        dockerCommand.setBinary(appverseBuilderProperties.getBuild().getCommand().getDocker().getBinary());
        dockerCommand.setTask(appverseBuilderProperties.getBuild().getCommand().getDocker().getTask());
        dockerCommand.setWorkDir(appverseBuilderProperties.getBuild().getCommand().getDocker().getWorkDir());
        dockerCommand.setRequestInputDir(buildAgentService.getBuildAgentRemoteRequestInputDir(buildAgent, buildRequest));
        dockerCommand.getEnvironmentVariables().putAll(buildRequest.getVariables());

        String imageName = Optional.ofNullable(buildRequest.getVariables().get(IMAGE_NAME))
            .orElse(buildRequest.getMappedEnginePlatform().getImageName());
        dockerCommand.setImageName(imageName);
        dockerCommand.setScriptFileName(RandomStringUtils.randomAlphabetic(10) + ".sh");//TODO get from the platform the actual script name and or extension at least to support .sh and .bat

        dockerCommand.setBeforeBuildScript(Optional.ofNullable(buildRequest.getVariables().get(BuildCommandBuilder.BEFORE_BUILD)).orElse(null));

        dockerCommand.setBuildScript(Optional.ofNullable(buildRequest.getVariables().get(BuildCommandBuilder.SCRIPT))
            .orElse(null));
        return dockerCommand;
    }

    @Override
    public boolean agentSupports(BuildAgentDTO agentDTO) {
        return agentDTO.getProperties().containsKey(DOCKER_AGENT_KEY);
    }

    @Override
    public boolean canBuildForRequest(BuildRequestDTO buildRequest) {
        return DOCKER.equals(buildRequest.getMappedEnginePlatform().getImageType());
    }
}
