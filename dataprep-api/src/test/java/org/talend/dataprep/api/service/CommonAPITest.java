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

package org.talend.dataprep.api.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Common Unit test for the dataset service API.
 */
public class CommonAPITest extends ApiServiceTestBase {

    @Test
    public void testCORSHeaders() throws Exception {
        given().header("Origin", "fake.host.to.trigger.cors").when().post("/datasets").then()
                .header("Access-Control-Allow-Origin", "fake.host.to.trigger.cors");
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

    /**
     * Test that API do not return hystrix exceptions.
     */
    @Test
    public void shouldNotReturnHystrixExceptions() {
        // given
        final ObjectMapper mapper = new ObjectMapper();

        // when
        final String badRequest = when().get("/command/test/fail_bad_request_exception").asString();
        final String commandException = when().get("//command/test/fail_command_exception").asString();
        final String rejectedSemaphoreExecution = when().get("/command/test/fail_rejected_semaphore_execution").asString();
        final String rejectedExecution = when().get("/command/test/fail_rejected_execution").asString();
        final String rejectedSemaphoreFallback = when().get("/command/test/fail_rejected_semaphore_fallback").asString();
        final String shortCircuit = when().get("/command/test/fail_short_circuit").asString();
        final String timeout = when().get("/command/test/fail_timeout").asString();

        // then
        List<JsonNode> rootNodes = new ArrayList<>();

        try {
            rootNodes.add(mapper.readTree(badRequest));
            final JsonNode commandExceptionNodes = mapper.readTree(commandException);
            final JsonNode rejectedSemaphoreExecutionNodes = mapper.readTree(rejectedSemaphoreExecution);
            final JsonNode rejectedExecutionNodes = mapper.readTree(rejectedExecution);
            final JsonNode rejectedSemaphoreFallbackNodes = mapper.readTree(rejectedSemaphoreFallback);
            final JsonNode shortCircuitNodes = mapper.readTree(shortCircuit);
            final JsonNode timeoutNodes = mapper.readTree(timeout);

        } catch (IOException e) {
            fail("The error must be de-serialized");
        }

        for (final JsonNode rootNode : rootNodes) {
            assertTrue(rootNode.has("code"));
            assertTrue(rootNode.has("message"));
            assertTrue(rootNode.has("messageTitle"));
            assertTrue(rootNode.has("context"));
        }
    }

}
