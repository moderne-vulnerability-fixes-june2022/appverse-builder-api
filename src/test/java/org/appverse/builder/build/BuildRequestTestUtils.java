package org.appverse.builder.build;

import org.appverse.builder.notification.email.EmailNotificationSender;
import org.appverse.builder.service.BuildChainService;
import org.appverse.builder.service.BuildRequestService;
import org.appverse.builder.web.rest.dto.BuildChainDTO;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.appverse.builder.domain.enumeration.BuildStatus.QUEUED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by panthro on 23/02/16.
 */
@Component
public class BuildRequestTestUtils {

    @Inject
    private BuildRequestService buildRequestService;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    private BuildChainService buildChainService;

    public void waitFinish(BuildRequestDTO buildRequest) throws InterruptedException {
        while (!buildRequestService.findOne(buildRequest.getId()).isFinished()) {
            log.info("Waiting build request to finish");
            Thread.sleep(500);
        }
    }

    public void waitStart(BuildRequestDTO buildRequest) throws InterruptedException {
        while (QUEUED.equals(buildRequestService.findOne(buildRequest.getId()).getStatus())) {
            log.info("Waiting for request to start, currently queued");
            Thread.sleep(200);
        }
    }

    public BuildChainDTO createBuildChainFromPayload(File nativeDemoDir) throws IOException, ZipException {
        File tempFile = File.createTempFile("payload-test", ".zip");
        tempFile.delete();
        ZipFile zipFile = new ZipFile(tempFile);
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
        parameters.setIncludeRootFolder(false);
        zipFile.createZipFileFromFolder(nativeDemoDir, parameters, false, 0L);
        MockMultipartFile file = new MockMultipartFile("payload", new FileInputStream(zipFile.getFile()));

        BuildChainDTO buildChain = buildChainService.createFromPayload(file);
        tempFile.delete();
        return buildChain;
    }

    public BuildRequestDTO createAndSchedule(String path) throws IOException, ZipException {
        final BuildChainDTO buildChainDTO = createBuildChainFromPayload(new ClassPathResource(path).getFile());
        assertThat(buildChainDTO.getRequests()).isNotEmpty();
        assertThat(buildChainDTO.getRequests().stream().map(BuildRequestDTO::getStatus).distinct().findAny().get()).isEqualTo(QUEUED);

        final BuildRequestDTO buildRequestDTO = buildChainDTO.getRequests().stream().findAny().get();
        buildRequestDTO.getVariables().put(EmailNotificationSender.EMAIL_MODEL_KEY, "user@user.com");
        buildRequestService.schedule(buildRequestDTO);
        return buildRequestDTO;
    }
}
