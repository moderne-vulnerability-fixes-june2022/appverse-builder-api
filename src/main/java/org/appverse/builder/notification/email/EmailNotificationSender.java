package org.appverse.builder.notification.email;

import org.appverse.builder.domain.enumeration.NotificationChannelType;
import org.appverse.builder.notification.Notification;
import org.appverse.builder.notification.NotificationSender;
import org.appverse.builder.service.MailService;
import org.appverse.builder.web.rest.dto.NotificationChannelDTO;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Created by panthro on 01/02/16.
 */
@Service
public class EmailNotificationSender implements NotificationSender {

    public static final String SMTP_HOST = "smtp.host";
    public static final String SMTP_PORT = "smtp.port";
    public static final String DEFAULT_SMTP_PORT = "25";
    public static final String SMTP_USER = "smtp.user";
    public static final String SMTP_PASSWORD = "smtp.password";
    public static final String SMTP_FROM = "smtp.from";
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String EMAIL_MODEL_KEY = "email";

    @Inject
    private MailService mailService;

    @Override
    public NotificationChannelType getSupportedNotificationChannelType() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public void sendNotification(Notification notification, NotificationChannelDTO notificationChannel) {
        Set<String> recipients = new HashSet<>();
        if (notification.getUser() == null || !isValidEmailAddress(notification.getUser().getEmail())) {
            log.info("User email {} is not a valid email address, notification {} won't be sent to user email.", notification.getUser(), notification);
        } else {
            recipients.add(notification.getUser().getEmail());
        }

        Optional.ofNullable(notification.getModelMap().get(EMAIL_MODEL_KEY)).ifPresent(emailObject -> {
            if (emailObject instanceof CharSequence) {
                recipients.add(emailObject.toString());
            }
        });
        if (recipients.isEmpty()) {
            log.info("No recipients found, not sending notification {}", notification);
            return;
        }
        log.info("Sending notification {} for channel {} ", notification, notificationChannel);
        String template = findTemplateName(notification);
        String subject = notification.getTitle();
        Locale locale = Locale.forLanguageTag(notification.getUser() == null ? "en" : notification.getUser().getLangKey());

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        String host = Optional.ofNullable(notificationChannel.getProperties().get(SMTP_HOST)).orElse(notificationChannel.getName());
        String port = Optional.ofNullable(notificationChannel.getProperties().get(SMTP_PORT)).orElse(DEFAULT_SMTP_PORT);
        String from = notificationChannel.getProperties().get(SMTP_FROM);

        javaMailSender.setHost(host);
        javaMailSender.setPort(Integer.valueOf(port));

        Optional.ofNullable(notificationChannel.getProperties().get(SMTP_USER)).ifPresent(javaMailSender::setUsername);

        Optional.ofNullable(notificationChannel.getProperties().get(SMTP_PASSWORD)).ifPresent(javaMailSender::setPassword);

        mailService.parseTemplateAndSendEmail(recipients, locale, template, subject, notification.getModelMap(), from, javaMailSender);

    }

    /**
     * @param notification
     * @return
     */
    private String findTemplateName(Notification notification) {
        //TODO find a better way or document this one
        return notification.getEvent().toString().toLowerCase().replaceAll("_", "/");
    }

    private boolean isValidEmailAddress(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
