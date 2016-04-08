package org.appverse.builder.web.rest.mapper;

import org.appverse.builder.domain.DistributionChannel;
import org.appverse.builder.web.rest.dto.DistributionChannelDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity DistributionChannel and its DTO DistributionChannelDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface DistributionChannelMapper {

    DistributionChannelDTO distributionChannelToDistributionChannelDTO(DistributionChannel distributionChannel);

    DistributionChannel distributionChannelDTOToDistributionChannel(DistributionChannelDTO distributionChannelDTO);
}
