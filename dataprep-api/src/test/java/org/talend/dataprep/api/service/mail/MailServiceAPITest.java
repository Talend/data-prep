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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.talend.dataprep.api.service.ApiServiceTestBase;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class MailServiceAPITest extends ApiServiceTestBase {

    @Test public void shouldNotSendEmptyMail() throws Exception {

        MailDetails mailDetails = new MailDetails();

        // send with bad recipients
        Response response = RestAssured.given() //
                .body(mapper.writer().writeValueAsBytes(mailDetails))//
                .contentType(ContentType.JSON) //
                .when() //
                .put("/api/mail");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(400);
    }




}
