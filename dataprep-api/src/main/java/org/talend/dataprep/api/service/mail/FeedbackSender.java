//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

/**
 * Sends a feedback to Talend.
 */
public interface FeedbackSender {

    /**
     * Sends a mail with given subject and body.
     * 
     * @param subject A subject to be included in sent email.
     * @param body A body to be included in sent email.
     * @param sender Email adress of sender.
     */
    void send(String subject, String body, String sender);

}
