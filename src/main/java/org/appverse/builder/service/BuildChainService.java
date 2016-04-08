package org.appverse.builder.service;

import org.appverse.builder.domain.BuildChain;
import org.appverse.builder.web.rest.dto.BuildChainDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.Optional;

/**
 * Service Interface for managing BuildChain.
 */
public interface BuildChainService {

    /**
     * Save a buildChain.
     *
     * @return the persisted entity
     */
    public BuildChainDTO save(BuildChainDTO buildChainDTO);

    /**
     * get all the buildChains.
     *
     * @return the list of entities
     */
    public Page<BuildChain> findAll(Pageable pageable);

    /**
     * get the "id" buildChain.
     *
     * @return the entity
     */
    public BuildChainDTO findOne(Long id);

    /**
     * delete the "id" buildChain.
     */
    public void delete(Long id);

    /**
     * Creates a buildchain from a compressed payload
     *
     * @param payload
     * @return
     */
    BuildChainDTO createFromPayload(MultipartFile payload);

    /**
     * Creates a buildChain from a compressed payload with options and a flavor
     *
     * @param payload
     * @param options
     * @param flavor
     * @return
     */
    BuildChainDTO createFromPayload(MultipartFile payload, Map<String, String> options, Optional<String> flavor);


    /**
     * The file where the compressed payload should be stored and retrieved
     *
     * @return
     */
    File getBuildChainCompressedInput(Long chainId);

    /**
     * The file where the compressed payload should be stored and retrieved
     *
     * @param buildChain
     * @return
     */
    File getBuildChainCompressedInput(BuildChainDTO buildChain);

    /**
     * @param buildChain
     * @return
     */
    File getBuildChainRootDirectory(BuildChainDTO buildChain);

    /**
     * @param buildChain
     * @return
     */
    File getBuildChainInputDirectory(BuildChainDTO buildChain);

    Page<BuildChain> findByCurrentUser(Pageable pageable);
}
