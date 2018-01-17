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

package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;

/**
 * Base tests for transformation service.
 */
public class BaseTest extends TransformationServiceBaseTest {

    @Test
    public void CORSHeaders() throws Exception {
        given().header("Origin", "fake.host.to.trigger.cors")
                .when()
                .post("/transform/JSON").then().header("Access-Control-Allow-Origin", "fake.host.to.trigger.cors");
        given().header("Origin", "fake.host.to.trigger.cors")
                .when()
                .post("/suggest/column").then().header("Access-Control-Allow-Origin", "fake.host.to.trigger.cors");
    }

    @Test
    public void shouldListExportTypes() throws Exception {
        String json = when().get("/export/formats").asString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode types = mapper.readTree(json);

        assertTrue(types.isArray());

        List<String> actual = new ArrayList<>(types.size());
        for (int i = 0; i < types.size(); i++) {
            actual.add(types.get(i).get("id").asText());
        }

        List<String> expected = Arrays.asList("XLSX", "CSV");
        Assertions.assertThat(actual).isNotNull().isNotEmpty().containsAll( expected );

    }

    /**
     * Check that the error listing service returns a list parsable of error codes. The content is not checked
     *
     * @throws Exception if an error occurs.
     */
    @Test
    public void shouldListErrors() throws Exception {
        String errors = when().get("/transform/errors").asString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualErrors = mapper.readTree(errors);

        assertTrue(actualErrors.isArray());
        assertTrue(actualErrors.size() > 0);
        for (final JsonNode errorCode : actualErrors) {
            assertTrue(errorCode.has("code"));
            assertTrue(errorCode.has("http-status-code"));
        }
    }

    @Test
    public void checkHeadersFromCache() throws Exception {

        LOGGER.debug("toto");

        // given
        final String datasetId = createDataset("input_dataset.csv", "testHeadersFromCache", "text/csv");
        final String preparationId = createEmptyPreparationFromDataset(datasetId, "myPrep");

        // first time is computed, next times are from the cache
        for (int i = 0; i < 3; i++) {
            // when
            final Response response = given() //
                    .when() //
                    .get("/apply/preparation/{preparationId}/dataset/{datasetId}/{format}?name={name}", preparationId, datasetId,
                            "CSV", "myPrep");

            // then
            Assert.assertTrue(response.getContentType().startsWith("text/csv"));
            assertEquals(response.getHeader("Content-Disposition"), "attachment; filename*=UTF-8''myPrep.csv");
        }
    }
}
