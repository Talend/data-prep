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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.talend.dataprep.encrypt.AESEncryption;

/**
 * Abstract FeedbackSender
 */
public abstract class AbstractFeedbackSender implements FeedbackSender {

    private static final String DEFAULT_RECIPIENT = "test_default@test.org";

    private static final String DEFAULT_SENDER = "dataprep_dev_default@talend.com";

    private static final String DEFAULT_SMTP_USER_NAME = "user_default@talend.com";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFeedbackSender.class);

    protected String[] recipients;

    protected String userName;

    protected String sender;

    @Autowired
    public void setRecipients(@Value("${mail.smtp.to}") String recipients) {
        try {
            this.recipients = AESEncryption.decrypt(recipients).split(",");
        } catch (Exception exc) {
            LOGGER.debug("Unable to parse given recipients used to send feedback mails, falling back to the default value {}",
                    recipients, exc);
            this.recipients = new String[] { DEFAULT_RECIPIENT };
        }
    }

    @Autowired
    public void setUserName(@Value("${mail.smtp.username}") String userName) {
        try {
            this.userName = AESEncryption.decrypt(userName);
        } catch (Exception exc) {
            LOGGER.debug("Unable to parse given smtp user name used to send feedback mails, falling back to the default value {}",
                    userName, exc);
            this.userName = DEFAULT_SMTP_USER_NAME;
        }
    }

    @Autowired
    public void setSender(@Value("${mail.smtp.from}") String sender) {
        try {
            this.sender = AESEncryption.decrypt(sender);
        } catch (Exception exc) {
            LOGGER.debug("Unable to parse given sender used to send feedback mails, falling back to the default value {}",
                    sender, exc);
            this.userName = DEFAULT_SENDER;
        }
    }

    public String[] getRecipients() {
        return recipients;
    }

    public String getUserName() {
        return userName;
    }

    public String getSender() {
        return sender;
    }
}
