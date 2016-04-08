package org.appverse.builder.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.dto.BuildInfoDTO;
import org.appverse.builder.service.PayloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by panthro on 29/12/15.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Service
public class PayloadServiceImpl implements PayloadService {


    private final Logger log = LoggerFactory.getLogger(PayloadServiceImpl.class);

    private Map<String, ObjectMapper> knownMappers = new HashMap<>();

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @PostConstruct
    private void initMappers() {
        log.debug("Initializing Object Mappers");
        knownMappers.put("yml", new ObjectMapper(new YAMLFactory()));
        knownMappers.put("json", new ObjectMapper());
    }

    @Override
    public File extractPayload(File payload, File inputDir) throws IOException {
        inputDir.mkdirs();
        try {
            ZipFile zipFile = new ZipFile(payload);
            zipFile.extractAll(inputDir.getAbsolutePath());
        } catch (Exception e) {
            log.warn("Error extracting payload {} into the input dir {}", payload, inputDir);
            throw new IOException(e);
        }
        return inputDir;
    }

    @Override
    public Optional<BuildInfoDTO> parseBuildInfoFile(File configFile) {
        if (!configFile.exists()) {
            return Optional.empty();
        }

        if (configFile.isDirectory()) {
            File[] candidates = configFile.listFiles((dir, name) -> FilenameUtils.getBaseName(name).equals(FilenameUtils.getBaseName(appverseBuilderProperties.getBuild().getBuildInfoFileName())) && knownMappers.containsKey(FilenameUtils.getExtension(name)));
            return Stream.of(candidates).map(this::parseBuildInfoFile)
                .filter(Optional::isPresent)
                .map(Optional::get).findAny();
        }

        Optional<ObjectMapper> mapper = getMapperForFile(configFile);
        if (!mapper.isPresent()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(mapper.get().readerFor(BuildInfoDTO.class).readValue(configFile));
        } catch (IOException e) {
            log.warn("could not read config file located at {}", configFile, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isValid(BuildInfoDTO buildInfoDTO) {
        return buildInfoDTO != null && hasEngine(buildInfoDTO) && hasPlatforms(buildInfoDTO) && hasFlavors(buildInfoDTO);
    }

    /**
     * Check if every platform has a flavor and all flavors have a name
     *
     * @param buildInfoDTO
     * @return
     */
    private boolean hasFlavors(BuildInfoDTO buildInfoDTO) {

        return buildInfoDTO.getEngine().getPlatforms().stream().noneMatch(platformInfoDTO ->
            CollectionUtils.isEmpty(platformInfoDTO.getFlavors()) || platformInfoDTO.getFlavors().stream().anyMatch(flavorInfoDTO -> flavorInfoDTO.getName() == null)
        );
    }

    /**
     * check if has at least 1 platform
     *
     * @param buildInfoDTO
     * @return
     */
    private boolean hasPlatforms(BuildInfoDTO buildInfoDTO) {
        return !CollectionUtils.isEmpty(buildInfoDTO.getEngine().getPlatforms())
            && buildInfoDTO.getEngine().getPlatforms().stream().allMatch(platformInfoDTO -> platformInfoDTO.getName() != null);
    }

    /**
     * check if engine is present
     *
     * @param buildInfoDTO
     * @return
     */
    private boolean hasEngine(BuildInfoDTO buildInfoDTO) {
        return buildInfoDTO.getEngine() != null && buildInfoDTO.getEngine().getName() != null;
    }

    private Optional<ObjectMapper> getMapperForFile(File configFile) {
        return Optional.ofNullable(knownMappers.get(FilenameUtils.getExtension(configFile.getAbsolutePath())));
    }
}
