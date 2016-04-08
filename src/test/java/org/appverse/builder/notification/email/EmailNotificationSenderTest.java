package org.appverse.builder.notification.email;

import org.appverse.builder.Application;
import org.appverse.builder.domain.enumeration.NotificationChannelType;
import org.appverse.builder.notification.Notification;
import org.appverse.builder.repository.NotificationChannelRepository;
import org.appverse.builder.web.rest.dto.BuildRequestDTO;
import org.appverse.builder.web.rest.dto.NotificationChannelDTO;
import org.appverse.builder.web.rest.dto.UserDTO;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by panthro on 03/02/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
@Transactional
public class EmailNotificationSenderTest {

    @Inject
    private NotificationChannelRepository notificationChannelRepository;

    @Inject
    private EmailNotificationSender emailNotificationSender;

    public final GreenMail greenMail = new GreenMail(new ServerSetup(3825, null, ServerSetup.PROTOCOL_SMTP));

    private NotificationChannelDTO notificationChannelDTO;

    @PostConstruct
    private void setup() {
        greenMail.start();
        notificationChannelDTO = new NotificationChannelDTO();
        notificationChannelDTO.setEnabled(true);
        notificationChannelDTO.setType(NotificationChannelType.EMAIL);
        notificationChannelDTO.setName("default");
        notificationChannelDTO.setDescription("default notification channel");
        Map<String, String> properties = new HashMap<>();
        properties.put(EmailNotificationSender.SMTP_HOST, "localhost");
        properties.put(EmailNotificationSender.SMTP_PORT, String.valueOf(greenMail.getSmtp().getPort()));
        notificationChannelDTO.setProperties(properties);

    }

    @PreDestroy
    private void destroy() {
        greenMail.stop();
    }


    @Test
    public void testAllNotificationTypes() throws Exception {

        Stream.of(Notification.Event.values()).forEach(event -> {
            Notification notification = new Notification();
            notification.setEvent(event);
            notification.setTitle(event.name());
            BuildRequestDTO buildRequestDTO = new BuildRequestDTO();
            buildRequestDTO.setId(0L);
            buildRequestDTO.setMessage("test-message");
            notification.getModelMap().put("request", buildRequestDTO);
            notification.getModelMap().put("baseUrl", "http://localhost");
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail("me@me.com");
            userDTO.setLangKey("en");
            userDTO.setLogin("user");
            notification.setUser(userDTO);

            emailNotificationSender.sendNotification(notification, notificationChannelDTO);
        });

        assertThat(greenMail.waitForIncomingEmail(Notification.Event.values().length));


        assertThat(Stream.of(greenMail.getReceivedMessages()).map(mimeMessage1 -> {
            try {
                return mimeMessage1.getSubject();
            } catch (MessagingException e) {
                e.printStackTrace();
                return "empty";
            }
        }).collect(Collectors.toList())).containsAll(Stream.of(Notification.Event.values()).map(Notification.Event::name).collect(Collectors.toList()));
    }


}
