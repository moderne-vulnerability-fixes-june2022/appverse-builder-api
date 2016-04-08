package org.appverse.builder.web.rest.mapper;

import org.appverse.builder.domain.BuildChain;
import org.appverse.builder.domain.BuildRequest;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity BuildRequest and its DTO BuildRequestDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface BuildRequestMapper {

    @Mapping(source = "chain.id", target = "chainId")
    @Mapping(source = "chain.requester.login", target = "requesterLogin")
    BuildRequestDTO buildRequestToBuildRequestDTO(BuildRequest buildRequest);

    @Mapping(source = "chainId", target = "chain")
    BuildRequest buildRequestDTOToBuildRequest(BuildRequestDTO buildRequestDTO);

    default BuildChain buildChainFromId(Long id) {
        if (id == null) {
            return null;
        }
        BuildChain buildChain = new BuildChain();
        buildChain.setId(id);
        return buildChain;
    }
}
