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

package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.talend.dataprep.transformation.Application;

import com.jayway.restassured.path.json.JsonPath;

/**
 * Integration tests on suggestions.
 */
public class SuggestionTest extends TransformationServiceBaseTest {

    @Test
    public void dataSetSuggest() throws Exception {
        // given
        final String dataSetMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/dataset_metadata.json"), UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(dataSetMetadata) //
                .when() //
                .post("/suggest/dataset") //
                .asString();

        // then
        assertEquals("[]", response, false);
    }

    @Test
    public void emptyColumnSuggest() throws Exception {
        // when
        final String response = given() //
                .contentType(JSON) //
                .body("") //
                .when() //
                .post("/suggest/column") //
                .asString();

        // then
        assertEquals("[]", response, false);
    }

    @Test
    public void stringColumnSuggest() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/string_column.json"),
                UTF_8);
        final String expectedSuggestions = IOUtils.toString(Application.class.getResourceAsStream("suggestions/string_column_suggestions.json"), UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column?limit=6") //
                .asString();

        // then
        assertEquals(expectedSuggestions, response, false);
    }

    @Test
    public void suggestLimit() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/date_column.json"),
                UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column?limit=2") //
                .asString();

        // then
        final JsonPath json = JsonPath.from(response);
        assertThat(json.getList("").size(), is(2));
    }

    @Test
    public void suggestLimitDefault() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/date_column.json"),
                UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column") //
                .asString();

        // then
        final JsonPath json = JsonPath.from(response);
        assertThat(json.getList("").size(), is(5)); // Default for "limit" is 5.
    }

    @Test
    public void floatColumnSuggest() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/float_column.json"),
                UTF_8);
        final String expectedSuggestions = IOUtils.toString(Application.class.getResourceAsStream("suggestions/float_column_suggestions.json"), UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column") //
                .asString();

        // then
        assertEquals(expectedSuggestions, response, false);
    }

    @Test
    public void integerColumnSuggest() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/integer_column.json"),
                UTF_8);
        final String expectedSuggestions = IOUtils.toString(Application.class.getResourceAsStream("suggestions/integer_column_suggestions.json"), UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column?limit=9") //
                .asString();

        // then
        assertEquals(expectedSuggestions, response, false);
    }

    @Test
    public void booleanColumnSuggest() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/boolean_column.json"),
                UTF_8);
        final String expectedSuggestions = IOUtils.toString(Application.class.getResourceAsStream("suggestions/boolean_column_suggestions.json"), UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column") //
                .asString();

        // then
        assertEquals(expectedSuggestions, response, false);
    }

    @Test
    public void dateColumnSuggest() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/date_column.json"),
                UTF_8);
        final String expectedSuggestions = IOUtils.toString(Application.class.getResourceAsStream("suggestions/date_column_suggestions.json"), UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column?limit=10") //
                .asString();

        // then

        assertEquals(expectedSuggestions, response, false);
    }

    @Test
    public void dateColumnSuggestWithStringType() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/date_column_string_type.json"), UTF_8);
        String expectedSuggestions = IOUtils.toString(Application.class.getResourceAsStream("suggestions/date_column_string_type_suggestions.json"),
                UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column?limit=10") //
                .asString();

        // then
        assertEquals(expectedSuggestions, response, false);
    }

    /**
     * @throws Exception
     * @see <a href="https://jira.talendforge.org/browse/TDP-2091">TDP-2091_improve_new_actions_suggestions</a>
     */
    @Test
    public void ensureThatDataMaskingAndPhoneFormatAreSuggestedOnPhoneColumns() throws Exception {
        // given
        // @formatter:off
        final String columnMetadata = "{\n" +
                "  \"id\": \"id\",\n" +
                "  \"quality\": {\n" +
                "    \"empty\": 5,\n" +
                "    \"invalid\": 10,\n" +
                "    \"valid\": 72\n" +
                "  },\n" +
                "  \"type\": \"integer\",\n" +
                "  \"domain\": \"FR_PHONE\"\n" +
                "}";

        given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column?limit=100") // 100 just to make sure that we get all the suggestions
                .then()
                .statusCode(200)
                .body("name", hasItems("mask_data_by_domain", "format_phone_number"));
        // @formatter:on

    }

}
