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
import static com.jayway.restassured.http.ContentType.JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.response.Response;

/**
 * Parameters integration tests.
 */
public class ParametersTest extends TransformationServiceBaseTest {

    @Test
    public void testDynamicParams_should_return_textclustering_dynamic_params() throws Exception {
        // given
        final String datasetContent = IOUtils.toString(this.getClass().getResourceAsStream("../parameters/dataset.json"),
                UTF_8);
        final String expectedParameters = IOUtils.toString(this.getClass().getResourceAsStream("../parameters/expected_cluster_params_soundex.json"),
                UTF_8);

        // when
        final Response post = given() //
                .contentType(JSON) //
                .body(datasetContent) //
                .when() //
                .post("/transform/suggest/textclustering/params?columnId=uglystate");
        final String response = post.asString();

        // then
        assertEquals(expectedParameters, response, false);
    }

    @Test
    public void testDynamicParams_should_throw_error() throws Exception {
        // when
        final Response post = given() //
                .contentType(JSON) //
                .body("json") //
                .when() //
                .post("/transform/suggest/{action}/params?columnId=uglystate", "unkownaction");

        Assert.assertEquals(404, post.getStatusCode());

    }

}
