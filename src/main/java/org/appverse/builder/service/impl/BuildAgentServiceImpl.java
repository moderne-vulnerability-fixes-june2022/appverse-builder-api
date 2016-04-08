package org.appverse.builder.service.impl;

import org.apache.commons.io.FilenameUtils;
import org.appverse.builder.build.BuildExecutorWorker;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.domain.BuildAgent;
import org.appverse.builder.repository.BuildAgentRepository;
import org.appverse.builder.service.BuildAgentQueueService;
import org.appverse.builder.service.BuildAgentService;
import org.appverse.builder.service.BuildCommandBuilderService;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.mapper.BuildAgentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * Service Implementation for managing BuildAgent.
 */
@Service
@Transactional
public class BuildAgentServiceImpl implements BuildAgentService {

    private final Logger log = LoggerFactory.getLogger(BuildAgentServiceImpl.class);

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private BuildAgentRepository buildAgentRepository;

    @Inject
    private BuildAgentMapper buildAgentMapper;

    @Inject
    private BuildAgentQueueService buildAgentQueueService;

    @Inject
    private BuildCommandBuilderService buildCommandBuilderService;

    /**
     * Save a buildAgent.
     *
     * @return the persisted entity
     */
    public BuildAgentDTO save(BuildAgentDTO buildAgentDTO) {
        log.debug("Request to save BuildAgent : {}", buildAgentDTO);
        BuildAgent buildAgent = buildAgentMapper.buildAgentDTOToBuildAgent(buildAgentDTO);
        buildAgent = buildAgentRepository.save(buildAgent);
        BuildAgentDTO result = buildAgentMapper.buildAgentToBuildAgentDTO(buildAgent);
        return result;
    }

    /**
     * get all the buildAgents.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<BuildAgent> findAll(Pageable pageable) {
        log.debug("Request to get all BuildAgents");
        Page<BuildAgent> result = buildAgentRepository.findAll(pageable);
        return result;
    }

    /**
     * get one buildAgent by id.
     *
     * @return the entity
     */
    @Transactional(readOnly = true)
    public BuildAgentDTO findOne(Long id) {
        log.debug("Request to get BuildAgent : {}", id);
        BuildAgent buildAgent = buildAgentRepository.findOne(id);
        BuildAgentDTO buildAgentDTO = buildAgentMapper.buildAgentToBuildAgentDTO(buildAgent);
        return buildAgentDTO;
    }

    /**
     * delete the  buildAgent by id.
     */
    public void delete(Long id) {
        log.debug("Request to delete BuildAgent : {}", id);
        buildAgentRepository.delete(id);
    }

    @Async
    @Override
    public void queueForExecution(BuildAgentDTO buildAgentDTO, BuildRequestDTO buildRequestDTO) {
        buildAgentQueueService.queue(buildAgentDTO, buildRequestDTO);
    }

    @Override
    public Optional<BuildAgentDTO> getAvailableBuildAgent(BuildRequestDTO buildRequestDTO) {
        return buildAgentRepository.findAll().stream()
            .filter(BuildAgent::getEnabled)
            .map(agent2 -> buildAgentMapper.buildAgentToBuildAgentDTO(agent2))
            .filter(buildAgentDTO -> buildCommandBuilderService.agentCanBuildRequest(buildAgentDTO, buildRequestDTO))
            .filter(agent -> matchesRequirements(agent, buildRequestDTO.getMappedEnginePlatform().getAgentRequirements()))
            .sorted((o1, o2) -> Long.compare(getCurrentLoad(o1), getCurrentLoad(o2)))
            .findFirst();
    }

    @Override
    public InputStream getRunningLogStream(BuildRequestDTO buildRequestDTO) {
        return buildAgentQueueService
            .findWorkerExecutingRequest(buildRequestDTO)
            .map(BuildExecutorWorker::getLogInputStream)
            .orElse(BuildExecutorWorker.raceConditionStream());
    }

    private long getCurrentLoad(BuildAgentDTO agent) {
        return buildAgentQueueService.getCurrentLoad(agent);
    }


    /**
     * Checks if the agent properties matches the required agentRequirements from the request
     *
     * @param agent
     * @param agentRequirements
     * @return
     */
    public boolean matchesRequirements(BuildAgentDTO agent, Map<String, String> agentRequirements) {
        return agentRequirements
            .keySet()
            .stream()
            /**
             * the following line maches
             *  - The agent has all the properties required (key)
             *  - If the required key has a null value, the agent also has a null value (key property match only)
             *  - If the required key has a value, the agent value is the same as the requirement
             */
            .allMatch(key -> agent.getProperties().containsKey(key) &&
                ((agent.getProperties().get(key) == null && agentRequirements.get(key) == null) || agent.getProperties().get(key).equals(agentRequirements.get(key))));
    }

    @Override
    public String getBuildAgentRemoteRequestDir(BuildAgentDTO buildAgentDTO, BuildRequestDTO request) {
        String remoteRootWorkDir = Optional.ofNullable(buildAgentDTO.getProperties().get(WORK_DIR))
            .map(root -> FilenameUtils.normalizeNoEndSeparator(root, true))
            .orElseGet(() -> FilenameUtils.normalizeNoEndSeparator(appverseBuilderProperties.getAgent().getDefaultRemoteRoot()));
        String separator = getAgentFileSeparator(buildAgentDTO);
        return remoteRootWorkDir + separator + request.getId();
    }

    private String getAgentFileSeparator(BuildAgentDTO buildAgentDTO) {
        return Optional.ofNullable(buildAgentDTO.getProperties().get(FILE_SEPARATOR))
            .orElse(appverseBuilderProperties.getAgent().getDefaultFileSeparator());
    }


    @Override
    public String getBuildAgentRemoteRequestInputDir(BuildAgentDTO buildAgentDTO, BuildRequestDTO request) {
        return getBuildAgentRemoteRequestDir(buildAgentDTO, request) + getAgentFileSeparator(buildAgentDTO) + appverseBuilderProperties.getBuild().getInputDirName();
    }
}
