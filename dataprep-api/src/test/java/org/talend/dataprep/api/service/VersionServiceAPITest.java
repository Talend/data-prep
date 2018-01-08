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

package org.talend.dataprep.api.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.info.BuildDetails;

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

        BuildDetails buildDetails = objectMapper.readValue(response.asString(), BuildDetails.class);

        Assert.assertEquals(4, buildDetails.getServices().length);
        Assert.assertEquals("GLOBAL_VERSION", buildDetails.getDisplayVersion());
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

        BuildDetails buildDetails = objectMapper.readValue(response.asString(), BuildDetails.class);

        BuildDetails buildDetails2 = objectMapper.readValue(response.asString(), BuildDetails.class);

        Assert.assertArrayEquals(buildDetails.getServices(), buildDetails2.getServices());
        Assert.assertEquals(buildDetails.getDisplayVersion(), buildDetails2.getDisplayVersion());
    }
}
