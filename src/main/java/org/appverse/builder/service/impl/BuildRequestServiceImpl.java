package org.appverse.builder.service.impl;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.distribution.Artifact;
import org.appverse.builder.domain.BuildRequest;
import org.appverse.builder.domain.enumeration.BuildStatus;
import org.appverse.builder.dto.BuildInfoDTO;
import org.appverse.builder.notification.Notification;
import org.appverse.builder.repository.BuildRequestRepository;
import org.appverse.builder.security.SecurityUtils;
import org.appverse.builder.service.*;
import org.appverse.builder.web.rest.dto.*;
import org.appverse.builder.web.rest.mapper.BuildRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.appverse.builder.notification.Notification.Event.BUILD_REQUEST_CANCELLED;

/**
 * Service Implementation for managing BuildRequest.
 */
@Service
@Transactional
public class BuildRequestServiceImpl implements BuildRequestService {

    private final Logger log = LoggerFactory.getLogger(BuildRequestServiceImpl.class);

    @Inject
    private BuildRequestRepository buildRequestRepository;

    @Inject
    private BuildRequestMapper buildRequestMapper;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private DistributionChannelService distributionChannelService;

    @Inject
    private BuildAgentService buildAgentService;

    @Inject
    private EngineService engineService;

    @Inject
    private EnginePlatformService enginePlatformService;

    @Inject
    private PayloadService payloadService;

    @Inject
    private BuildChainService buildChainService;

    @Inject
    private EngineVariableService engineVariableService;

    @Inject
    private EnginePlatformVariableService enginePlatformVariableService;

    @Inject
    private NotificationChannelService notificationChannelService;


    /**
     * Save a buildRequest.
     *
     * @return the persisted entity
     */
    public BuildRequestDTO save(BuildRequestDTO buildRequestDTO) {
        log.debug("Request to save BuildRequest : {}", buildRequestDTO);
        BuildRequest buildRequest = buildRequestMapper.buildRequestDTOToBuildRequest(buildRequestDTO);
        buildRequest = buildRequestRepository.save(buildRequest);
        BuildRequestDTO result = buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest);
        return result;
    }

    /**
     * get all the buildRequests.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<BuildRequest> findAll(Pageable pageable) {
        log.debug("Request to get all BuildRequests");
        Page<BuildRequest> result = buildRequestRepository.findAll(pageable);
        return result;
    }

    /**
     * get one buildRequest by id.
     *
     * @return the entity
     */
    @Transactional(readOnly = true)
    public BuildRequestDTO findOne(Long id) {
        log.debug("Request to get BuildRequest : {}", id);
        BuildRequest buildRequest = buildRequestRepository.findOne(id);
        BuildRequestDTO buildRequestDTO = buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest);
        return buildRequestDTO;
    }

    /**
     * delete the  buildRequest by id.
     */
    public void delete(Long id) {
        log.debug("Request to delete BuildRequest : {}", id);
        buildRequestRepository.delete(id);
    }

    public void cancelRequest(BuildRequestDTO buildRequestDTO, String message) {
        buildRequestDTO.finish(BuildStatus.CANCELLED, message);
        log.info("Canceling build request {} reason: {}", buildRequestDTO, message);
        save(buildRequestDTO);
        Notification notification = notificationChannelService.buildBuildRequestNotification(buildRequestDTO, BUILD_REQUEST_CANCELLED);
        notificationChannelService.sendNotificationToAllChannels(notification);
    }

    @Override
    public void schedule(BuildRequestDTO buildRequestDTO) {

        final Optional<BuildInfoDTO> buildInfoDTO = getBuildInfoDTO(buildRequestDTO);
        if (!buildInfoDTO.isPresent()) {
            cancelRequest(buildRequestDTO, "Could not locate or parse build config file");
            return;
        }
        final Optional<BuildInfoDTO.PlatformInfoDTO> actualPlatformInfoDTO = getPlatformInfoDTO(buildRequestDTO, buildInfoDTO);

        //in case it could not find one, cancel the request and end the process.
        if (!actualPlatformInfoDTO.isPresent()) {
            cancelRequest(buildRequestDTO, "There is a misconfiguration, your config file does not have the platform you are trying to build");
            return;
        }

        final Optional<BuildInfoDTO.FlavorInfoDTO> actualFlavor = getFlavorInfoDTO(buildRequestDTO, actualPlatformInfoDTO.get());
        //in case it could not find one, cancel the request and end the process.
        if (!actualFlavor.isPresent()) {
            cancelRequest(buildRequestDTO, "There is a misconfiguration, your config file does not have the flavor you are trying to build");
            return;
        }
        final Optional<EngineDTO> engine = engineService.findByNameAndVersion(buildInfoDTO.get().getEngine().getName(), buildInfoDTO.get().getEngine().getVersion());
        if (!engine.isPresent()) {
            cancelRequest(buildRequestDTO, "Could not find specified engine");
            return;
        }

        //find a suitable EnginePlatform that will be used in the build
        final Optional<EnginePlatformDTO> enginePlatformDTO = enginePlatformService.findByEngineNameAndNameAndVersion(buildRequestDTO.getEngine(), buildRequestDTO.getPlatform(), actualPlatformInfoDTO.get().getVersion());
        //in case it could not find one, cancel the request and end the process.
        if (!enginePlatformDTO.isPresent() || !enginePlatformDTO.get().getEngineId().equals(engine.get().getId())) {
            cancelRequest(buildRequestDTO, "Could not find the Engine or Platform to build.");
            return;
        }
        buildRequestDTO.setMappedEnginePlatform(enginePlatformDTO.get());


        BuildChainDTO buildChainDTO = buildChainService.findOne(buildRequestDTO.getChainId());
        if (!allRequiredEngineVariablesArePresent(buildRequestDTO, buildChainDTO)) {
            cancelRequest(buildRequestDTO, "Missing required Engine variables");
            return;
        }

        if (!allRequiredPlatformVariablesArePresent(buildRequestDTO, buildChainDTO)) {
            cancelRequest(buildRequestDTO, "Missing required Platform variables");
            return;
        }

        final Map<String, String> variablesMap = getSystemVariablesFromPlatformAndEngine(enginePlatformDTO);
        mergeUserEnvironmentVariables(buildInfoDTO, actualPlatformInfoDTO, actualFlavor, variablesMap);

        mergeBuildChainOptions(variablesMap, buildChainDTO.getOptions());


        buildRequestDTO.setVariables(variablesMap);

        final Optional<BuildAgentDTO> buildAgent = buildAgentService.getAvailableBuildAgent(buildRequestDTO);

        if (!buildAgent.isPresent()) {
            cancelRequest(buildRequestDTO, "Could not find a suitable agent to execute the request");
            return;
        }
        buildAgentService.queueForExecution(buildAgent.get(), buildRequestDTO);
    }

    private boolean allRequiredPlatformVariablesArePresent(BuildRequestDTO buildRequestDTO, BuildChainDTO buildChainDTO) {
        return enginePlatformVariableService.findByEnginePlatformId(buildRequestDTO.getMappedEnginePlatform().getId()).stream().allMatch(enginePlatformVariableDTO -> !enginePlatformVariableDTO.getRequired() || buildChainDTO.getOptions().containsKey(enginePlatformVariableDTO.getName()));

    }

    private boolean allRequiredEngineVariablesArePresent(BuildRequestDTO buildRequestDTO, BuildChainDTO buildChainDTO) {
        return engineVariableService.findByEngineId(buildRequestDTO.getMappedEnginePlatform().getEngineId()).stream().allMatch(engineVariableDTO -> !engineVariableDTO.getRequired() || buildChainDTO.getOptions().containsKey(engineVariableDTO.getName()));
    }

    private void mergeBuildChainOptions(Map<String, String> variablesMap, Map<String, String> options) {
        variablesMap.putAll(options);
    }

    private void mergeUserEnvironmentVariables(Optional<BuildInfoDTO> buildInfoDTO, Optional<BuildInfoDTO.PlatformInfoDTO> actualPlatformInfoDTO, Optional<BuildInfoDTO.FlavorInfoDTO> actualFlavor, Map<String, String> variablesMap) {
        /**
         * User specified ones
         */
        //Engine Variables
        variablesMap.putAll(buildInfoDTO.get().getEngine().getEnv());

        //Platform Variables
        variablesMap.putAll(actualPlatformInfoDTO.get().getEnv());

        //Flavor variables
        variablesMap.putAll(actualFlavor.get().getEnv());
    }

    private Map<String, String> getSystemVariablesFromPlatformAndEngine(Optional<EnginePlatformDTO> enginePlatformDTO) {
        /**
         * System default ones
         */
        final Map<String, String> variablesMap = new HashMap<>();
        engineVariableService.findByEngineId(enginePlatformDTO.get().getEngineId()).forEach(engineVariableDTO -> {
            variablesMap.put(engineVariableDTO.getName(), engineVariableDTO.getDefaultValue());
        });

        enginePlatformVariableService.findByEnginePlatformId(enginePlatformDTO.get().getId()).forEach(enginePlatformVariableDTO -> {
            variablesMap.put(enginePlatformVariableDTO.getName(), enginePlatformVariableDTO.getDefaultValue());
        });
        return variablesMap;
    }

    private Optional<BuildInfoDTO.FlavorInfoDTO> getFlavorInfoDTO(BuildRequestDTO buildRequestDTO, BuildInfoDTO.PlatformInfoDTO actualPlatformInfoDTO) {
        return actualPlatformInfoDTO.getFlavors()
            .stream()
            .filter(flavorInfoDTO -> flavorInfoDTO.getName().equals(buildRequestDTO.getFlavor()))
            .findFirst();
    }

    private Optional<BuildInfoDTO.PlatformInfoDTO> getPlatformInfoDTO(BuildRequestDTO buildRequestDTO, Optional<BuildInfoDTO> buildInfoDTO) {
        //Find the platform from the config file it should match
        return buildInfoDTO.get().getEngine().getPlatforms()
            .stream() //iterate over the buildInfo platforms
            .filter(platformInfoDTO -> platformInfoDTO.getName().equals(buildRequestDTO.getPlatform())) //filter by platform name
            .findFirst();
    }

    private Optional<BuildInfoDTO> getBuildInfoDTO(BuildRequestDTO buildRequestDTO) {
        //Find the chain
        final BuildChainDTO buildChainDTO = buildChainService.findOne(buildRequestDTO.getChainId());
        //Find the configuration file and parse it
        return payloadService.parseBuildInfoFile(buildChainService.getBuildChainInputDirectory(buildChainDTO));
    }

    public Optional<InputStream> getLogs(Long id) {
        Optional<BuildRequest> request = buildRequestRepository.findById(id);
        return request.map(buildRequest -> {

            switch (buildRequest.getStatus()) {
                case QUEUED:
                    return Optional.<InputStream>of(new ByteArrayInputStream("Build has not yet started, hold on".getBytes()));
                case CANCELLED:
                    return Optional.<InputStream>of(new ByteArrayInputStream(("Build has been cancelled [" + buildRequest.getMessage() + "]").getBytes()));
                case RUNNING:
                    return Optional.of(buildAgentService.getRunningLogStream(findOne(id)));
                case FAILED:
                case SUCCESSFUL:
                default:
                    return distributionChannelService.getLogAsStream(buildRequestMapper.buildRequestToBuildRequestDTO(buildRequest));
            }
        }).orElse(Optional.empty());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    //TODO find a way to delete the zip artifact after the request has ended.
    //TODO enabled more compression types and modes
    public Optional<File> getCompressedArtifacts(Long id, Long distributionId) {
        return Optional.ofNullable(findOne(id)).map(buildRequest -> {
            switch (buildRequest.getStatus()) {
                case CANCELLED:
                case RUNNING:
                case QUEUED:
                    return null;
                case SUCCESSFUL:
                case FAILED:
                default:
                    try {
                        List<Artifact> requestArtifacts = distributionChannelService.getRequestArtifacts(buildRequest);
                        if (requestArtifacts.isEmpty()) {
                            log.info("could not find artifacts for build request {} in any distribution channel", buildRequest);
                            return null;
                        }

                        File tempZip = File.createTempFile(appverseBuilderProperties.getBuild().getArtifactCompressedName(), appverseBuilderProperties.getBuild().getArtifactCompressedExtension());
                        //ZipException occur if artifact exists, we just need the File holder.
                        tempZip.delete();
                        ZipFile zipFile = new ZipFile(tempZip);
                        ZipParameters parameters = new ZipParameters();
                        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
                        requestArtifacts
                            .stream()
                            .filter(artifact -> distributionId == null || artifact.getDistributionChannelId().equals(distributionId))
                            .forEach(artifact -> {
                            try {
                                if (artifact.isLocal()) {
                                    zipFile.addFile(new File(artifact.getUri()), parameters);
                                } else {
                                    log.warn("Remote artifacts are not yet supported");
                                }
                            } catch (ZipException e) {
                                log.info("could not add artifact {} to compressedFile", artifact);
                            }
                        });
                        return tempZip;
                    } catch (ZipException | IOException e) {
                        log.warn("Could not create compressed artifact for request {}", buildRequest, e);
                        return null;
                    }
            }
        });
    }

    @Override
    public Optional<File> getCompressedArtifacts(Long id) {
        return getCompressedArtifacts(id, null);
    }


    @Override
    public File getBuildRequestRootDir(BuildRequestDTO buildRequest) {
        return new File(buildChainService.getBuildChainRootDirectory(buildChainService.findOne(buildRequest.getChainId())), buildRequest.getId().toString());
    }

    @Override
    public Page<BuildRequest> findByCurrentUser(Pageable pageable) {
        return buildRequestRepository.findByChainRequesterLogin(SecurityUtils.getCurrentUserLogin(), pageable);
    }

    @Override
    public boolean hasAccess(BuildRequestDTO buildRequestDTO) {
        return !SecurityUtils.isCurrentUserAdmin() && !buildRequestDTO.getRequesterLogin().equals(SecurityUtils.getCurrentUserLogin());
    }
}
