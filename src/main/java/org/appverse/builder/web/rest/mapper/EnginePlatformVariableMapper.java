package org.appverse.builder.web.rest.mapper;

import org.appverse.builder.domain.EnginePlatform;
import org.appverse.builder.domain.EnginePlatformVariable;
import org.appverse.builder.web.rest.dto.EnginePlatformVariableDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity EnginePlatformVariable and its DTO EnginePlatformVariableDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EnginePlatformVariableMapper {

    @Mapping(source = "enginePlatform.id", target = "enginePlatformId")
    @Mapping(source = "enginePlatform.name", target = "enginePlatformName")
    @Mapping(source = "enginePlatform.version", target = "enginePlatformVersion")
    EnginePlatformVariableDTO enginePlatformVariableToEnginePlatformVariableDTO(EnginePlatformVariable enginePlatformVariable);

    @Mapping(source = "enginePlatformId", target = "enginePlatform")
    EnginePlatformVariable enginePlatformVariableDTOToEnginePlatformVariable(EnginePlatformVariableDTO enginePlatformVariableDTO);

    default EnginePlatform enginePlatformFromId(Long id) {
        if (id == null) {
            return null;
        }
        EnginePlatform enginePlatform = new EnginePlatform();
        enginePlatform.setId(id);
        return enginePlatform;
    }
}
