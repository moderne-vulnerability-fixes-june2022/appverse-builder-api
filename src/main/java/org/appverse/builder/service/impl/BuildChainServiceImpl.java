package org.appverse.builder.service.impl;

import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.domain.BuildChain;
import org.appverse.builder.domain.BuildRequest;
import org.appverse.builder.domain.enumeration.BuildStatus;
import org.appverse.builder.dto.BuildInfoDTO;
import org.appverse.builder.repository.BuildChainRepository;
import org.appverse.builder.security.SecurityUtils;
import org.appverse.builder.service.BuildChainService;
import org.appverse.builder.service.PayloadService;
import org.appverse.builder.service.UserService;
import org.appverse.builder.web.rest.dto.BuildChainDTO;
import org.appverse.builder.web.rest.mapper.BuildChainMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service Implementation for managing BuildChain.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Service
@Transactional
public class BuildChainServiceImpl implements BuildChainService {

    private final Logger log = LoggerFactory.getLogger(BuildChainServiceImpl.class);

    @Inject
    private BuildChainRepository buildChainRepository;

    @Inject
    private BuildChainMapper buildChainMapper;

    @Inject
    private UserService userService;

    @Inject
    private PayloadService payloadService;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    /**
     * Save a buildChain.
     *
     * @return the persisted entity
     */
    public BuildChainDTO save(BuildChainDTO buildChainDTO) {
        log.debug("Request to save BuildChain : {}", buildChainDTO);
        BuildChain buildChain = buildChainMapper.buildChainDTOToBuildChain(buildChainDTO);
        buildChain = buildChainRepository.save(buildChain);
        BuildChainDTO result = buildChainMapper.buildChainToBuildChainDTO(buildChain);
        return result;
    }

    /**
     * get all the buildChains.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<BuildChain> findAll(Pageable pageable) {
        log.debug("Request to get all BuildChains");
        Page<BuildChain> result = buildChainRepository.findAll(pageable);
        return result;
    }

    /**
     * get one buildChain by id.
     *
     * @return the entity
     */
    @Transactional(readOnly = true)
    public BuildChainDTO findOne(Long id) {
        log.debug("Request to get BuildChain : {}", id);
        BuildChain buildChain = buildChainRepository.findOne(id);
        BuildChainDTO buildChainDTO = buildChainMapper.buildChainToBuildChainDTO(buildChain);
        return buildChainDTO;
    }

    /**
     * delete the  buildChain by id.
     */
    public void delete(Long id) {
        log.debug("Request to delete BuildChain : {}", id);
        buildChainRepository.delete(id);
    }

    @Override
    public BuildChainDTO createFromPayload(MultipartFile payload) {
        return createFromPayload(payload, new HashMap<String, String>(), Optional.empty());
    }

    @Override
    public BuildChainDTO createFromPayload(MultipartFile payload, Map<String, String> options, Optional<String> flavor) {
        final BuildChain buildChain = new BuildChain();
        buildChain.setOptions(options != null ? options : new HashMap<String, String>());
        buildChain.setRequester(userService.getUserWithAuthorities());
        buildChainRepository.save(buildChain);
        try {
            final File buildChainCompressedInput = getBuildChainCompressedInput(buildChainMapper.buildChainToBuildChainDTO(buildChain));
            payload.transferTo(buildChainCompressedInput);
            File inputDir = payloadService.extractPayload(buildChainCompressedInput, getBuildChainInputDirectory(buildChainMapper.buildChainToBuildChainDTO(buildChain)));
            Optional<BuildInfoDTO> optional = payloadService.parseBuildInfoFile(inputDir);
            final List<BuildRequest> requests = new ArrayList<>();
            optional.ifPresent(buildInfoDTO -> {
                if (payloadService.isValid(buildInfoDTO)) {
                    buildInfoDTO.getEngine().getPlatforms().forEach(platformInfoDTO ->
                        platformInfoDTO.getFlavors().stream()
                            //If a single flavor is passed, make sure only the passed flavor name is used
                            .filter(flavorInfoDTO -> !flavor.isPresent() || flavorInfoDTO.getName().equals(flavor.get()))
                            .forEach(flavorInfoDTO -> {
                            BuildRequest request = new BuildRequest();
                            request.setChain(buildChain);
                            request.setEngine(buildInfoDTO.getEngine().getName());
                            request.setPlatform(platformInfoDTO.getName());
                            request.setFlavor(flavorInfoDTO.getName());
                            requests.add(request);
                            }));
                } else {
                    requests.add(createBadRequest("You have an invalid configuration file [" + buildInfoDTO + "]"));
                }
            });
            if (requests.isEmpty()) {
                requests.add(createBadRequest("Could not determine the build information from the configuration file."));
            }
            buildChain.getRequests().addAll(requests);
        } catch (IOException e) {
            buildChain.getRequests().add(createBadRequest("Could not extract payload or read config file: " + e.getMessage()));
            return buildChainMapper.buildChainToBuildChainDTO(buildChain);
        }
        return buildChainMapper.buildChainToBuildChainDTO(buildChainRepository.save(buildChain));
    }

    @Override
    public File getBuildChainCompressedInput(Long chainId) {
        return getBuildChainCompressedInput(findOne(chainId));
    }

    @Override
    public File getBuildChainCompressedInput(BuildChainDTO buildChain) {
        return new File(getBuildChainRootDirectory(buildChain), appverseBuilderProperties.getBuild().getCompressedPayloadName());
    }

    @Override
    public File getBuildChainRootDirectory(BuildChainDTO buildChain) {
        File file = new File(appverseBuilderProperties.getBuild().getBuildRoot(), buildChain.getRequesterId() + "-" + buildChain.getRequesterLogin() + File.separator + buildChain.getId());
        file.mkdirs();
        return file;
    }

    @Override
    public File getBuildChainInputDirectory(BuildChainDTO buildChain) {
        File inputDirectory = new File(getBuildChainRootDirectory(buildChain), appverseBuilderProperties.getBuild().getInputDirName());
        inputDirectory.mkdirs();
        return inputDirectory;
    }

    @Override
    public Page<BuildChain> findByCurrentUser(Pageable pageable) {
        return buildChainRepository.findByRequesterLogin(SecurityUtils.getCurrentUserLogin(), pageable);

    }

    private BuildRequest createBadRequest(String message) {
        BuildRequest request = new BuildRequest();
        request.setEngine(appverseBuilderProperties.getBuild().getUnknownEngine());
        request.setFlavor(appverseBuilderProperties.getBuild().getUnknownFlavor());
        request.setPlatform(appverseBuilderProperties.getBuild().getUnknownPlatform());
        request.start();
        request.finish(BuildStatus.CANCELLED, message);
        return request;
    }

}
