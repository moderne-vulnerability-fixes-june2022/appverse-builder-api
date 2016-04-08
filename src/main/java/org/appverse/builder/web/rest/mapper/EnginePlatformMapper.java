package org.appverse.builder.web.rest.mapper;

import org.appverse.builder.domain.Engine;
import org.appverse.builder.domain.EnginePlatform;
import org.appverse.builder.web.rest.dto.EnginePlatformDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity EnginePlatform and its DTO EnginePlatformDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EnginePlatformMapper {

    @Mapping(source = "engine.id", target = "engineId")
    @Mapping(source = "engine.name", target = "engineName")
    @Mapping(source = "engine.version", target = "engineVersion")
    EnginePlatformDTO enginePlatformToEnginePlatformDTO(EnginePlatform enginePlatform);

    @Mapping(target = "enginePlatformVariables", ignore = true)
    @Mapping(source = "engineId", target = "engine")
    EnginePlatform enginePlatformDTOToEnginePlatform(EnginePlatformDTO enginePlatformDTO);

    default Engine engineFromId(Long id) {
        if (id == null) {
            return null;
        }
        Engine engine = new Engine();
        engine.setId(id);
        return engine;
    }
}
