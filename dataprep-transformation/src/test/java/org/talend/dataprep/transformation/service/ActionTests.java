package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Integration tests on column actions.
 */
public class ActionTests extends TransformationServiceBaseTests {

    @Test
    public void noColumnActions() throws Exception {
        // when
        final String response = given() //
                .contentType(JSON) //
                .body("") //
                .when() //
                .post("/actions/column") //
                .asString();

        // then
        assertThat(response, sameJSONAsFile(ActionTests.class.getResourceAsStream("all_actions.json")));
    }

    @Test
    public void columnActions() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(ActionTests.class.getResourceAsStream("../suggestions/string_column.json"));

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/actions/column") //
                .asString();

        // then
        assertThat(response, sameJSONAsFile(ActionTests.class.getResourceAsStream("all_actions_string.json")));
    }

}
