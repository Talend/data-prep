// ============================================================================
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

import org.talend.dataprep.api.preparation.AppendStep;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;

/**
 * Java client for testings.
 */
public class PreparationAPITestClient {

    static {
        RestAssured.defaultParser = Parser.JSON;
    }

    public static void appendStepsToPrep(String prepId, AppendStep stepsToAppend) {
        given().contentType(ContentType.JSON)
                .body(stepsToAppend)
                .expect()
                .statusCode(200)
                .post("/api/preparations/{id}/actions", prepId);
    }

    public static void changePreparationStepsOrder(String testPrepId, String rootStep, String secondStep) {
        given().contentType(ContentType.JSON)
                .expect()
                .statusCode(200)
                .post("/api/preparations/{preparationId}/steps/{stepId}/order?parentStepId={parentStepId}", testPrepId,
                        secondStep, rootStep);
    }

}
