package org.talend.dataprep.api.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.StepDiff;

import static com.jayway.restassured.RestAssured.given;

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

    public static PreparationDetailsResponse getPreparationDetails(String testPrepId) {
        Response preparationDetails = given().contentType(ContentType.JSON)
                .expect()
                .statusCode(200)
                .get("/api/preparations/{id}/details", testPrepId);
        return preparationDetails.as(PreparationDetailsResponse.class);
    }

    public static void changePreparationStepsOrder(String testPrepId, String rootStep, String secondStep) {
        given().contentType(ContentType.JSON)
                .expect()
                .statusCode(200)
                .post("/api/preparations/{preparationId}/steps/{stepId}/order?parentStepId={parentStepId}", testPrepId,
                        secondStep, rootStep);
    }

    /**
     * Class that represents the response of a call to /api/preparations/{id}/details produced by {@link PreparationAPI#getPreparation}.
     */
    public static class PreparationDetailsResponse {

        public Preparation preparation;

        public String id;

        public String dataSetId;

        public String author;

        public String name;

        public String creationDate;

        public String lastModificationDate;

        public String owner;

        public List<String> steps;

        public List<StepDiff> diff;

        public List<Action> actions;

        public List<ActionMetadataDescriptor> metadata;

        public boolean allowFullRun;

        public static class ActionMetadataDescriptor {

            public String name;

            public String category;

            public boolean dynamic;

            public String description;

            public String label;

            public String docUrl;

            public List<String> actionScope;

            public List<ParameterDescriptor> parameters;

            public static class ParameterDescriptor {

                public String name;

                public String label;

                @JsonProperty("default")
                public String defaultTruc;

                public String type;

                public String description;

                public String defaultValue;

                public boolean implicit;

                public boolean canBeBlank;

                public String placeHolder;

                public Map<String, Object> configuration;

            }
        }
    }
}
