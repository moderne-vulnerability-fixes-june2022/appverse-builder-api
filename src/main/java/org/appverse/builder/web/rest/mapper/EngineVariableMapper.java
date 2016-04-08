package org.appverse.builder.web.rest.mapper;

import org.appverse.builder.domain.Engine;
import org.appverse.builder.domain.EngineVariable;
import org.appverse.builder.web.rest.dto.EngineVariableDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity EngineVariable and its DTO EngineVariableDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EngineVariableMapper {

    @Mapping(source = "engine.id", target = "engineId")
    @Mapping(source = "engine.name", target = "engineName")
    @Mapping(source = "engine.version", target = "engineVersion")
    EngineVariableDTO engineVariableToEngineVariableDTO(EngineVariable engineVariable);

    @Mapping(source = "engineId", target = "engine")
    EngineVariable engineVariableDTOToEngineVariable(EngineVariableDTO engineVariableDTO);

    default Engine engineFromId(Long id) {
        if (id == null) {
            return null;
        }
        Engine engine = new Engine();
        engine.setId(id);
        return engine;
    }
}
