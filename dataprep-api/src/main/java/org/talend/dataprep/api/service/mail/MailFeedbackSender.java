// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service.mail;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.talend.dataprep.encrypt.AESEncryption;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * Utility class to send mail.
 */
@ConditionalOnProperty("mail.smtp.host")
@Primary
@Component
public class MailFeedbackSender extends AbstractFeedbackSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailFeedbackSender.class);

    private String subjectPrefix;

    private String bodyPrefix;

    private String bodySuffix;

    private String password;

    private String smtpHost;

    private String smtpPort;

    @Autowired
    public void setSubjectPrefix(@Value("${mail.smtp.subject.prefix}") String subjectPrefix) {
        try {
            this.subjectPrefix = AESEncryption.decrypt(subjectPrefix);
        } catch (Exception exc) {
            LOGGER.debug("Unable to parse given subject prefix used to send feedback mails {}", subjectPrefix, exc);
        }
    }

    @Autowired
    public void setBodyPrefix(@Value("${mail.smtp.body.prefix}") String bodyPrefix) {
        try {
            this.bodyPrefix = AESEncryption.decrypt(bodyPrefix);
        } catch (Exception exc) {
            LOGGER.debug("Unable to parse given body prefix used to send feedback mails {}", bodyPrefix, exc);
        }
    }

    @Autowired
    public void setBodySuffix(@Value("${mail.smtp.body.suffix}") String bodySuffix) {
        try {
            this.bodySuffix = AESEncryption.decrypt(bodySuffix);
        } catch (Exception exc) {
            LOGGER.debug("Unable to parse given body suffix used to send feedback mails {}", bodySuffix, exc);
        }
    }

    @Autowired
    public void setPassword(@Value("${mail.smtp.password}") String password) {
        try {
            this.password = AESEncryption.decrypt(password);
        } catch (Exception exc) {
            LOGGER.debug("Unable to parse given password used to send feedback mails {}", password, exc);
        }
    }

    @Autowired
    public void setSmtpHost(@Value("${mail.smtp.host}") String smtpHost) {
        try {
            this.smtpHost = AESEncryption.decrypt(smtpHost);
        } catch (Exception exc) {
            LOGGER.debug("Unable to parse given smtp host used to send feedback mails {}", smtpHost, exc);
        }
    }

    @Autowired
    public void setSmtpPort(@Value("${mail.smtp.port}") String smtpPort) {
        try {
            this.smtpPort = AESEncryption.decrypt(smtpPort);
        } catch (Exception exc) {
            LOGGER.debug("Unable to parse given smtp port used to send feedback mails {}", smtpPort, exc);
        }
    }

    private MailFeedbackSender() {
    }

    @Override
    public void send(String subject, String body, String sender) {
        try {
            final String recipientList = StringUtils.join((new HashSet<>(Arrays.asList(recipients))).toArray(), ',');
            subject = subjectPrefix + subject;
            body = bodyPrefix + "<br/>" + body + "<br/>" + bodySuffix;

            InternetAddress from = new InternetAddress(this.sender);
            InternetAddress replyTo = new InternetAddress(sender);

            Properties p = new Properties();
            p.put("mail.smtp.host", smtpHost);
            p.put("mail.smtp.port", smtpPort);
            p.put("mail.smtp.starttls.enable", "true");
            p.put("mail.smtp.auth", "true");

            MailAuthenticator authenticator = new MailAuthenticator(userName, password);
            Session sendMailSession = Session.getInstance(p, authenticator);

            MimeMessage msg = new MimeMessage(sendMailSession);
            msg.setFrom(from);
            msg.setReplyTo(new Address[] { replyTo });
            msg.addRecipients(Message.RecipientType.TO, recipientList);

            msg.setSubject(subject, "UTF-8");
            msg.setSentDate(new Date());
            Multipart mainPart = new MimeMultipart();
            BodyPart html = new MimeBodyPart();
            html.setContent(body, "text/html; charset=utf-8");
            mainPart.addBodyPart(html);
            msg.setContent(mainPart);
            Transport.send(msg);

            LOGGER.debug("Sending mail:'{}' to '{}'", subject, recipients);
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_SEND_MAIL, e);
        }
    }

    private static class MailAuthenticator extends Authenticator {

        private final String userName;

        private final String password;

        public MailAuthenticator(String username, String password) {
            this.userName = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password);
        }
    }

}
