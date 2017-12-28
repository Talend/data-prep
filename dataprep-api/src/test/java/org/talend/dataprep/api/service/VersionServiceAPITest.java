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
import org.talend.dataprep.info.GlobalVersion;

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

        GlobalVersion globalVersion = objectMapper.readValue(response.asString(), GlobalVersion.class);

        Assert.assertEquals(4, globalVersion.getServices().length);
        Assert.assertEquals("GLOBAL_VERSION", globalVersion.getApplication());
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

        GlobalVersion globalVersion = objectMapper.readValue(response.asString(), GlobalVersion.class);

        GlobalVersion globalVersion2 = objectMapper.readValue(response.asString(), GlobalVersion.class);

        Assert.assertArrayEquals(globalVersion.getServices(), globalVersion2.getServices());
        Assert.assertEquals(globalVersion.getApplication(), globalVersion2.getApplication());
    }
}
