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

package org.talend.dataprep.api.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.info.Version;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public class VersionServiceAPITest extends ApiServiceTestBase {

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldReturnOKWhenVersionAsked() throws Exception {
        Response response = RestAssured.given() //
                .when() //
                .get("/api/version");

        Assert.assertEquals(200, response.getStatusCode());

        Version[] versions = objectMapper.readValue(response.asString(), new TypeReference<Version[]>() {
        });

        Assert.assertEquals(4, versions.length);
    }

    @Test
    public void shouldReceiveSameVersionsWhenAskedTwice() throws Exception {
        //
        Response response = RestAssured.given() //
                .when() //
                .get("/api/version");

        Response response2 = RestAssured.given() //
                .when() //
                .get("/api/version");

        Version[] versions = objectMapper.readValue(response.asString(), new TypeReference<Version[]>() {
        });

        Version[] versions2 = objectMapper.readValue(response2.asString(), new TypeReference<Version[]>() {
        });

        Assert.assertArrayEquals(versions, versions2);
    }
}