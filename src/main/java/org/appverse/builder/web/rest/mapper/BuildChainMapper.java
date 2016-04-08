package org.appverse.builder.web.rest.mapper;

import org.appverse.builder.domain.BuildChain;
import org.appverse.builder.domain.User;
import org.appverse.builder.web.rest.dto.BuildChainDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity BuildChain and its DTO BuildChainDTO.
 */
@Mapper(componentModel = "spring", uses = {BuildRequestMapper.class})
public interface BuildChainMapper {

    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(source = "requester.login", target = "requesterLogin")
    BuildChainDTO buildChainToBuildChainDTO(BuildChain buildChain);

    @Mapping(source = "requesterId", target = "requester")
    BuildChain buildChainDTOToBuildChain(BuildChainDTO buildChainDTO);

    default User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }
}
