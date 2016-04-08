package org.appverse.builder.web.rest.mapper;

import org.appverse.builder.domain.Engine;
import org.appverse.builder.web.rest.dto.EngineDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity Engine and its DTO EngineDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EngineMapper {

    EngineDTO engineToEngineDTO(Engine engine);

    @Mapping(target = "engineVariables", ignore = true)
    Engine engineDTOToEngine(EngineDTO engineDTO);
}
