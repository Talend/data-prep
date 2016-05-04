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

import org.apache.commons.validator.routines.EmailValidator;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.service.ApiServiceTestBase;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class MailServiceAPITest extends ApiServiceTestBase {

    @Autowired
    private AbstractFeedbackSender mailFeedbackSender;

    @Test public void shouldReturnInternalSeverError500() throws Exception {

        MailDetails mailDetails = new MailDetails();

        // send with bad recipients
        Response response = RestAssured.given() //
                .body(mapper.writer().writeValueAsBytes(mailDetails))//
                .contentType(ContentType.JSON) //
                .when() //
                .put("/api/mail");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(400);

    }

    /**
     *
     * @throws Exception
     */
    @Test public void shouldHaveValidMailProperties() throws Exception {

        String[] recipients = mailFeedbackSender.getRecipients();
        String fromAddress = mailFeedbackSender.getSender();
        String userName = mailFeedbackSender.getUserName();

        EmailValidator.getInstance().isValid(fromAddress);
        EmailValidator.getInstance().isValid(userName);
        for (String recipient: recipients){
            Assert.assertTrue(EmailValidator.getInstance().isValid(recipient));
        }
    }


}
