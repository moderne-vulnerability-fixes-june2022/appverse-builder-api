package org.appverse.builder.service;

import org.appverse.builder.dto.BuildInfoDTO;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by panthro on 29/12/15.
 */
public interface PayloadService {


    /**
     * Extract the compressedPayload (a compressed file that has been uploaded) to the given input root
     *
     * @param compressedPayload   the compressedPayload
     * @param inputRoot the root folder to extract the compressedPayload content
     * @return
     * @throws IOException in an error occurs while extracting
     */
    File extractPayload(File compressedPayload, File inputRoot) throws IOException;


    /**
     * Parses the configuration file (usually a yml) file describing the application that is represented by the <code>BuildInfoDTO</code>
     *
     * @param configFile the <code>File</code> representing the input directory OR the configuration file.
     *                   if the file is a directory the system should try to guess where is the configuration file by checking common names
     * @return
     */
    Optional<BuildInfoDTO> parseBuildInfoFile(File configFile);

    /**
     * Validates the given <code>BuildInfoDTO</code>
     *
     * @param buildInfoDTO the buildInfo to be validated
     * @return <code>true</code> if validation succeed or <code>false</code> if not
     */
    boolean isValid(BuildInfoDTO buildInfoDTO);


}
