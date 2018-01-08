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

import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@link FeedbackSender} useful to debug
 */
@Component
public class NoOpFeedbackSender implements FeedbackSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpFeedbackSender.class);

    @Override
    public void send(String subject, String body, String sender) {
        if (LOGGER.isDebugEnabled()) {
            try {
                InternetAddress from = new InternetAddress(sender);
                String builder = "***** Sending mail" + //
                        "from: " + from.toString() + //
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
