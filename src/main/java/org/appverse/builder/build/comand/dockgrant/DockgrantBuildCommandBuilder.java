package org.appverse.builder.build.comand.dockgrant;

import org.apache.commons.lang.RandomStringUtils;
import org.appverse.builder.build.comand.BuildCommandBuilder;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.service.BuildAgentService;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static org.appverse.builder.domain.enumeration.ImageType.VAGRANT;

/**
 * Created by panthro on 07/03/16.
 * <p>
 * This command builder uses dockgrant: @see https://github.com/ferranvila/dockgrant
 * to execute vagrant boxes as "docker run" commands
 * <p>
 * dockgrant run -q --rm --path /tmp/example --volume /tmp/example/data:/data --workdir /data -e VAR=world --image hashicorp/precise64 --script "sh script.sh"
 */
@Component
public class DockgrantBuildCommandBuilder implements BuildCommandBuilder<DockgrantCommand> {

    private static final Logger log = LoggerFactory.getLogger(DockgrantBuildCommandBuilder.class);
    public static final String DOCKGRANT_AGENT_KEY = "dockgrant";

    @Inject
    private BuildAgentService buildAgentService;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Override
    public DockgrantCommand buildCommand(BuildAgentDTO buildAgent, BuildRequestDTO buildRequest) {
        DockgrantCommand dockgrantCommand = new DockgrantCommand();
        dockgrantCommand.setQuiet(!log.isDebugEnabled());
        dockgrantCommand.setRemoveContainer(true);
        dockgrantCommand.setPath(buildAgentService.getBuildAgentRemoteRequestInputDir(buildAgent, buildRequest));
        dockgrantCommand.setBinary(appverseBuilderProperties.getBuild().getCommand().getDockgrant().getBinary());
        dockgrantCommand.setTask(appverseBuilderProperties.getBuild().getCommand().getDockgrant().getTask());
        dockgrantCommand.setWorkDir(appverseBuilderProperties.getBuild().getCommand().getDockgrant().getWorkDir());
        dockgrantCommand.setRequestInputDir(buildAgentService.getBuildAgentRemoteRequestInputDir(buildAgent, buildRequest));
        dockgrantCommand.getEnvironmentVariables().putAll(buildRequest.getVariables());
        dockgrantCommand.setScriptFileName(RandomStringUtils.randomAlphabetic(10) + ".sh");//TODO get from the platform the actual script name and or extension at least to support .sh and .bat
        dockgrantCommand.setQuiet(!log.isDebugEnabled());


        String imageName = Optional.ofNullable(buildRequest.getVariables().get(IMAGE_NAME))
            .orElse(buildRequest.getMappedEnginePlatform().getImageName());

        try {
            URL url = new URL(imageName);
            dockgrantCommand.setImageUrl(url.toString());
            dockgrantCommand.setImageName(String.valueOf(buildRequest.getId()));
        } catch (MalformedURLException e) {
            log.debug("Image name is a public image {}", imageName);
            dockgrantCommand.setImageName(imageName);
        }

        dockgrantCommand.setBeforeBuildScript(Optional.ofNullable(buildRequest.getVariables().get(BuildCommandBuilder.BEFORE_BUILD)).orElse(null));

        dockgrantCommand.setBuildScript(Optional.ofNullable(buildRequest.getVariables().get(BuildCommandBuilder.SCRIPT))
            .orElse(null));
        return dockgrantCommand;

    }

    @Override
    public boolean agentSupports(BuildAgentDTO agentDTO) {
        return agentDTO.getProperties().containsKey(DOCKGRANT_AGENT_KEY);
    }

    @Override
    public boolean canBuildForRequest(BuildRequestDTO buildRequest) {
        return VAGRANT.equals(buildRequest.getMappedEnginePlatform().getImageType());
    }
}
