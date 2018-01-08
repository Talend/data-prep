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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests on column actions.
 */
public class ActionTest extends TransformationServiceBaseTest {

    @Autowired
    ActionRegistry actionRegistry;

    @Autowired
    ObjectMapper mapper;

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
        assertFalse(response.isEmpty());
    }

    @Test
    public void columnActions() throws Exception {
        // given
        final String columnMetadata =
                IOUtils.toString(ActionTest.class.getResourceAsStream("../suggestions/string_column.json"), UTF_8);

        // when
        final String response = given() //
                .contentType(JSON) //
                .body(columnMetadata) //
                .when() //
                .post("/actions/column") //
                .asString();

        // then
        assertFalse(response.isEmpty());
    }

    @Test
    public void should_get_all_lines_actions() throws Exception {
        // when
        final String response = given() //
                .when() //
                .get("/actions/line") //
                .asString();

        // then
        assertThat(response, sameJSONAsFile(ActionTest.class.getResourceAsStream("all_line_scope_actions.json")));
    }

    @Test
    public void should_get_all_dataset_actions() throws Exception {
        // when
        final String response = given() //
                .when() //
                .get("/actions/dataset") //
                .asString();

        // then
        assertThat(response, sameJSONAsFile(ActionTest.class.getResourceAsStream("all_dataset_scope_actions.json")));
    }

}
