package org.appverse.builder.service;

import org.apache.commons.lang.CharEncoding;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for sending e-mails.
 * <p/>
 * <p>
 * We use the @Async annotation to send e-mails asynchronously.
 * </p>
 */
@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    @Inject
    private JavaMailSenderImpl javaMailSender;

    @Inject
    private MessageSource messageSource;

    @Inject
    private SpringTemplateEngine templateEngine;


    /**
     * Asynchronously parses an email template and send to the recipients
     *
     * @param recipients       a collection containing all the recipients
     * @param locale           the locale of the message
     * @param templateName     the template name
     * @param subject          the subject
     * @param contextVariables the variables that will be used to parse the template
     */
    @Async
    public void parseTemplateAndSendEmail(Collection<String> recipients, Locale locale, String templateName, String subject, Map<String, Object> contextVariables) {
        Context context = new Context(locale);
        context.setVariables(contextVariables);
        String content = templateEngine.process(templateName, context);
        recipients.forEach(email -> sendEmail(appverseBuilderProperties.getMail().getFrom(), email, subject, content, false, true, javaMailSender));
    }

    @Async
    public void parseTemplateAndSendEmail(Collection<String> recipients, Locale locale, String templateName, String subject, Map<String, Object> contextVariables, String from, JavaMailSender mailSender) {
        Context context = new Context(locale);
        context.setVariables(contextVariables);
        String content = templateEngine.process(templateName, context);
        recipients.forEach(email -> sendEmail(from, email, subject, content, false, true, mailSender));
    }

    @Async
    public void sendEmail(String from, String to, String subject, String content, boolean isMultipart, boolean isHtml, JavaMailSender mailSender) {
        log.debug("Send e-mail multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, CharEncoding.UTF_8);
            message.setTo(to);
            message.setFrom(from != null ? from : appverseBuilderProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            mailSender.send(mimeMessage);
            log.debug("Sent e-mail to User '{}'", to);
        } catch (Exception e) {
            log.warn("E-mail could not be sent to user '{}', exception is: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendActivationEmail(User user, String baseUrl) {
        log.debug("Sending activation e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Map<String, Object> context = new HashMap<>();
        context.put("user", user);
        context.put("baseUrl", baseUrl);
        String subject = messageSource.getMessage("email.activation.title", null, locale);
        parseTemplateAndSendEmail(Stream.of(user.getEmail()).collect(Collectors.toList()), locale, "activationEmail", subject, context);

    }

    @Async
    public void sendCreationEmail(User user, String baseUrl) {
        log.debug("Sending creation e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Map<String, Object> context = new HashMap<>();
        context.put("user", user);
        context.put("baseUrl", baseUrl);
        String subject = messageSource.getMessage("email.activation.title", null, locale);
        parseTemplateAndSendEmail(Stream.of(user.getEmail()).collect(Collectors.toList()), locale, "creationEmail", subject, context);
    }

    @Async
    public void sendPasswordResetMail(User user, String baseUrl) {
        log.debug("Sending password reset e-mail to '{}'", user.getEmail());
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Map<String, Object> context = new HashMap<>();
        context.put("user", user);
        context.put("baseUrl", baseUrl);
        String subject = messageSource.getMessage("email.reset.title", null, locale);
        parseTemplateAndSendEmail(Stream.of(user.getEmail()).collect(Collectors.toList()), locale, "passwordResetEmail", subject, context);
    }

}
