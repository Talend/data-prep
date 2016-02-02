//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service.mail;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * Utility class to send mail.
 */
@ConditionalOnProperty("mail.smtp.host")
@Primary
@Component
public class MailFeedbackSender implements FeedbackSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailFeedbackSender.class);

    @Value("#{'${mail.smtp.to}'.split(',')}")
    private String[] recipients;

    @Value("${mail.smtp.subject.prefix}")
    private String subjectPrefix;

    @Value("${mail.smtp.body.prefix}")
    private String bodyPrefix;

    @Value("${mail.smtp.body.suffix}")
    private String bodySuffix;

    @Value("${mail.smtp.username}")
    private String userName;

    @Value("${mail.smtp.password}")
    private String password;

    @Value("${mail.smtp.from}")
    private String fromAddress;

    @Value("${mail.smtp.host}")
    private String smtpHost;

    @Value("${mail.smtp.port}")
    private String smtpPort;

    private MailFeedbackSender() {
    }

    @Override
    public void send(String subject, String body, String sender) {
        try {
            final String recipientList = StringUtils.join((new HashSet<>(Arrays.asList(recipients))).toArray(), ',');
            subject = subjectPrefix + subject;
            body = bodyPrefix + "<br/>" + body + "<br/>" + bodySuffix;

            InternetAddress from = new InternetAddress(fromAddress);
            InternetAddress replyTo = new InternetAddress(sender);

            Properties p = new Properties();
            p.put("mail.smtp.host", smtpHost);
            p.put("mail.smtp.port", smtpPort);
            p.put("mail.smtp.starttls.enable","true");
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
