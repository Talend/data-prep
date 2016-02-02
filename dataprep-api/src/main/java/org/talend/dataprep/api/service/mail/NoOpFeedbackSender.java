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
import java.util.HashSet;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@link FeedbackSender} useful to debug
 */
@ConditionalOnProperty(value = { "mail.smtp.host", "mail.smtp.to", "mail.smtp.username",
        "mail.smtp.from" }, matchIfMissing = true)
@Component
public class NoOpFeedbackSender implements FeedbackSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpFeedbackSender.class);

    @Value("#{'${mail.smtp.to}'.split(',')}")
    private String[] recipients;

    @Value("${mail.smtp.username}")
    private String userName;

    @Value("${mail.smtp.from}")
    private String fromAddress;

    @Override
    public void send(String subject, String body, String sender) {
        if (LOGGER.isDebugEnabled()) {
            try {
                String recipientList = StringUtils.join((new HashSet<>(Arrays.asList(recipients))).toArray(), ',');
                InternetAddress from = new InternetAddress(sender);
                String builder = "***** Sending mail" + //
                        "from: " + from.toString() + //
                        "username: " + userName + //
                        "Recipients=" + recipientList + //
                        "Subject=" + subject + //
                        "Body=" + body;
                LOGGER.debug(builder);
            } catch (Exception e) {
                LOGGER.error("Problem when sending mail", e);
            }
        } else {
            LOGGER.debug("Sending a feedback.");
        }
    }
}
