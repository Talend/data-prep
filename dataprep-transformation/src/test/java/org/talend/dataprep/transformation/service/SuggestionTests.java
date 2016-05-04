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

package org.talend.dataprep.transformation.service;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
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
public class SuggestionTests extends TransformationServiceBaseTests {

    @Test
    public void dataSetSuggest() throws Exception {
        // given
        final String dataSetMetadata = IOUtils
                .toString(Application.class.getResourceAsStream("suggestions/dataset_metadata.json"));

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
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/string_column.json"));
        final String expectedSuggestions = IOUtils
                .toString(Application.class.getResourceAsStream("suggestions/string_column_suggestions.json"));

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
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/date_column.json"));

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
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/date_column.json"));

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
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/float_column.json"));
        final String expectedSuggestions = IOUtils
                .toString(Application.class.getResourceAsStream("suggestions/float_column_suggestions.json"));

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
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/integer_column.json"));
        final String expectedSuggestions = IOUtils
                .toString(Application.class.getResourceAsStream("suggestions/integer_column_suggestions.json"));

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/suggest/column?limit=7") //
                .asString();

        // then
        assertEquals(expectedSuggestions, response, false);
    }

    @Test
    public void booleanColumnSuggest() throws Exception {
        // given
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/boolean_column.json"));
        final String expectedSuggestions = IOUtils
                .toString(Application.class.getResourceAsStream("suggestions/boolean_column_suggestions.json"));

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
        final String columnMetadata = IOUtils.toString(Application.class.getResourceAsStream("suggestions/date_column.json"));
        final String expectedSuggestions = IOUtils
                .toString(Application.class.getResourceAsStream("suggestions/date_column_suggestions.json"));

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
        final String columnMetadata = IOUtils
                .toString(Application.class.getResourceAsStream("suggestions/date_column_string_type.json"));
        String expectedSuggestions = IOUtils
                .toString(Application.class.getResourceAsStream("suggestions/date_column_string_type_suggestions.json"));

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

}
