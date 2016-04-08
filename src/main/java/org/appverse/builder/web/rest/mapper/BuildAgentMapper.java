package org.appverse.builder.web.rest.mapper;

import org.appverse.builder.domain.BuildAgent;
import org.appverse.builder.web.rest.dto.BuildAgentDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity BuildAgent and its DTO BuildAgentDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface BuildAgentMapper {

    BuildAgentDTO buildAgentToBuildAgentDTO(BuildAgent buildAgent);

    BuildAgent buildAgentDTOToBuildAgent(BuildAgentDTO buildAgentDTO);
}
