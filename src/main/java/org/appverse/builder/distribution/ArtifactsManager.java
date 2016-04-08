package org.appverse.builder.distribution;

import org.appverse.builder.domain.enumeration.DistributionChannelType;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.dto.DistributionChannelDTO;

import java.util.List;
import java.util.Optional;

/**
 * Created by panthro on 11/01/16.
 */
public interface ArtifactsManager {


    /**
     * Get the distribution channel type the implementation supports
     *
     * @return
     */
    DistributionChannelType getSupportedDistributionChannelType();


    /**
     * Gets the distribution channel this artifacts manager uses
     *
     * @return
     */
    void setDistributionChannel(DistributionChannelDTO distributionChannel);


    /**
     * Gets the distribution channel this artifacts manager uses
     *
     * @return
     */
    DistributionChannelDTO getDistributionChannel();


    /**
     * Saves the given artifact into the distribution channel
     *
     * @param artifact
     * @param buildRequestDTO
     * @return
     */
    boolean distribute(Artifact artifact, BuildRequestDTO buildRequestDTO);

    /**
     * Saves the given artifacts into the distribution channel
     *
     * @param artifact
     * @param buildRequestDTO
     * @return
     */
    boolean distribute(List<Artifact> artifact, BuildRequestDTO buildRequestDTO);

    /**
     * Retrieve the artifacts for a given build request
     *
     * @param buildRequestDTO
     * @return
     */
    List<Artifact> retrieve(BuildRequestDTO buildRequestDTO);

    /**
     * Tries to retrieve a single artifact from the distribution channel with the given name
     *
     * @param buildRequestDTO
     * @param name
     * @return
     */
    Optional<Artifact> retrieve(BuildRequestDTO buildRequestDTO, String name);
}
