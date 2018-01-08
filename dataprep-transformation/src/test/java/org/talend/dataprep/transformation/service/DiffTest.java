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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.talend.dataprep.api.export.ExportParameters.SourceType.HEAD;
import static org.talend.dataprep.test.SameJSONFile.sameJSONAsFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.talend.dataprep.transformation.preview.api.PreviewParameters;

import com.jayway.restassured.http.ContentType;

/**
 * Diff integration tests.
 */
public class DiffTest extends TransformationServiceBaseTest {

    @Test
    public void should_return_preview() throws Exception {
        // given
        PreviewParameters input = new PreviewParameters( //
                getSingleTransformation(), //
                getMultipleTransformation(), //
                createDataset("../preview/input.csv", "input4preview", "text/csv"), //
                null, //
                "[2,4,6]", //
                HEAD
        );

        // when
        final String response = given() //
                .contentType(ContentType.JSON) //
                .body(mapper.writer().writeValueAsString(input)) //
                .when().expect().statusCode(200).log().ifError() //
                .post("/transform/preview") //
                .asString();

        // then
        final String expected = IOUtils.toString(this.getClass().getResourceAsStream("../preview/expected_output.json"),
                UTF_8);
        assertEquals(expected, response, false);
    }

    /**
     * After TDP-1184 fix, there is a problem on preview (regression?).
     * Use case is:
     * - delete a column (lastname here)
     * - add new columns (with split on city here)
     * - preview an action on the first new column (uppercase on 0000 here)
     * <p>
     * -> lastname is still on the preview data for lines 4 & 6. it is absent (which is what we expect) only for the first line!
     */
    @Test
    public void test_TDP_1184() throws Exception {
        // given
        PreviewParameters input = new PreviewParameters( //
                getTransformation_TDP_1184_step_1(), //
                getTransformation_TDP_1184_step_2(), //
                createDataset("../preview/input.csv", "tdp-1184", "text/csv"), //
                null, //
                "[1,4,6]", //
                HEAD
        );

        // when
        final String response = given() //
                .contentType(ContentType.JSON) //
                .body(mapper.writer().writeValueAsString(input)) //
                .when().expect().statusCode(200).log().ifError() //
                .post("/transform/preview") //
                .asString();

        // then
        final InputStream expected = this.getClass().getResourceAsStream("../preview/expected_output_TDP_1184.json");
        assertThat(response, sameJSONAsFile(expected));
    }

    @Test
    public void should_return_created_columns() throws Exception {
        // given
        final PreviewParameters previewParams = new PreviewParameters( //
                getSingleTransformation(), //
                getMultipleTransformationWithNewColumn(), //
                createDataset("../preview/input.csv", "input4preview", "text/csv"), //
                null, //
                null, //
                HEAD
        );
        final List<PreviewParameters> input = Collections.singletonList(previewParams);

        // when
        final String response = given() //
                .contentType(ContentType.JSON) //
                .body(mapper.writer().writeValueAsString(input)) //
                .when().expect().statusCode(200).log().ifError() //
                .post("/transform/diff/metadata")
                .asString();

        // then
        assertEquals("[{\"createdColumns\":[\"0009\"]}]", response, false);
    }

    @Test
    public void should_return_created_columns_for_multiple_diffs() throws Exception {
        // given
        final String datasetId = createDataset("../preview/input.csv", "input4preview", "text/csv");
        final PreviewParameters previewParams = new PreviewParameters( //
                getSingleTransformation(), //
                getMultipleTransformationWithNewColumn(), //
                datasetId, //
                null, //
                null, //
                HEAD
        );
        final PreviewParameters previewParamsBis = new PreviewParameters( //
                getMultipleTransformationWithNewColumn(), //
                getMultipleTransformationWithNewColumnBis(), //
                datasetId, //
                null, //
                null, //
                HEAD
        );
        final List<PreviewParameters> input = new ArrayList<>(2);
        input.add(previewParams);
        input.add(previewParamsBis);

        // when
        final String response = given() //
                .contentType(ContentType.JSON) //
                .body(mapper.writer().writeValueAsString(input)) //
                .when().expect().statusCode(200).log().ifError() //
                .post("/transform/diff/metadata")
                .asString();

        // then
        assertEquals("[{\"createdColumns\":[\"0009\"]}, {\"createdColumns\":[\"0010\"]}]", response, false);
    }


    @Test
    public void should_return_empty_array_when_step_does_not_create_columns() throws Exception {
        // given
        final PreviewParameters previewParams = new PreviewParameters( //
                getSingleTransformation(), //
                getMultipleTransformationWithoutNewColumn(), //
                createDataset("../preview/input.csv", "input4preview", "text/csv"), //
                null, //
                null, //
                HEAD
        );
        final List<PreviewParameters> input = Collections.singletonList(previewParams);

        // when
        final String response = given() //
                .contentType(ContentType.JSON) //
                .body(mapper.writer().writeValueAsString(input)) //
                .when().expect().statusCode(200).log().ifError() //
                .post("/transform/diff/metadata")
                .asString();

        // then
        assertEquals("[{\"createdColumns\":[]}]", response, false);
    }

    private String getSingleTransformation() throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("../preview/uppercase.json"), UTF_8);
    }

    private String getMultipleTransformationWithNewColumn() throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("../preview/uppercase_copy.json"), UTF_8);
    }

    private String getMultipleTransformationWithNewColumnBis() throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("../preview/uppercase_copy_bis.json"),
                UTF_8);
    }

    private String getTransformation_TDP_1184_step_1() throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("../preview/deletecolumn_split.json"), UTF_8);
    }

    private String getTransformation_TDP_1184_step_2() throws IOException {
        // return "{\"actions\": [ { \"action\": \"delete_column\", \"parameters\":{ \"column_id\": \"lastname\",
        // \"scope\": \"column\" } }, { \"action\": \"split\", \"parameters\":{ \"column_id\": \"city\", \"scope\":
        // \"column\", \"separator\":\" \", \"limit\":\"2\" } }, { \"action\": \"uppercase\",\"parameters\":{
        // \"column_id\": \"0000\", \"scope\": \"column\" } } ]}";
        return IOUtils.toString(this.getClass().getResourceAsStream("../preview/deletecolumn_split_uppercase.json"),
                UTF_8);
    }

    private String getMultipleTransformationWithoutNewColumn() throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("../preview/uppercase_lowercase.json"), UTF_8);
    }

    private String getMultipleTransformation() throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("../preview/various_actions.json"), UTF_8);
    }

}
