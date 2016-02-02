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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Common Unit test for the dataset service API.
 */
public class CommonAPITest extends ApiServiceTestBase {

    @Test
    public void testCORSHeaders() throws Exception {
        given().header("Origin", "fake.host.to.trigger.cors")
                .when()
                .post("/datasets").then().header("Access-Control-Allow-Origin", "fake.host.to.trigger.cors");
    }


    /**
     * Test that errors are properly listed and displayed.
     */
    @Test
    public void shouldListErrors() throws IOException {
        // given
        final ObjectMapper mapper = new ObjectMapper();

        // when
        final String errors = when().get("/api/errors").asString();

        // then : content is not checked, only mandatory fields
        final JsonNode rootNode = mapper.readTree(errors);
        assertTrue(rootNode.isArray());
        assertTrue(rootNode.size() > 0);
        for (final JsonNode errorCode : rootNode) {
            assertTrue(errorCode.has("code"));
            assertTrue(errorCode.has("http-status-code"));
        }
    }
}
