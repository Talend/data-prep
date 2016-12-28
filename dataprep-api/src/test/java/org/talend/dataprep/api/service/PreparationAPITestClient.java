package org.talend.dataprep.api.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;
import org.talend.dataprep.api.dataset.RowMetadata;
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

        public RowMetadata rowMetadata;

        public List<String> steps;

        public List<StepDiff> diff;

        public List<ActionDescriptor> actions;

        public List<ActionMetadataDescriptor> metadata;

        public boolean allowFullRun;

        public boolean allowDistributedRun;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("preparation", preparation)
                    .add("id", id)
                    .add("dataSetId", dataSetId)
                    .add("author", author)
                    .add("name", name)
                    .add("creationDate", creationDate)
                    .add("lastModificationDate", lastModificationDate)
                    .add("owner", owner)
                    .add("rowMetadata", rowMetadata)
                    .add("steps", steps)
                    .add("diff", diff)
                    .add("actions", actions)
                    .add("metadata", metadata)
                    .add("allowFullRun", allowFullRun)
                    .add("allowDistributedRun", allowDistributedRun)
                    .toString();
        }

        public static class ActionMetadataDescriptor {

            public String name;

            public String category;

            public boolean dynamic;

            public String description;

            public String label;

            public String docUrl;

            public List<String> actionScope;

            public List<ParameterDescriptor> parameters;

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("name", name)
                        .add("category", category)
                        .add("dynamic", dynamic)
                        .add("description", description)
                        .add("label", label)
                        .add("docUrl", docUrl)
                        .add("actionScope", actionScope)
                        .add("parameters", parameters)
                        .toString();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ActionMetadataDescriptor that = (ActionMetadataDescriptor) o;
                return dynamic == that.dynamic &&
                        Objects.equals(name, that.name) &&
                        Objects.equals(category, that.category) &&
                        Objects.equals(description, that.description) &&
                        Objects.equals(label, that.label) &&
                        Objects.equals(docUrl, that.docUrl) &&
                        Objects.equals(actionScope, that.actionScope) &&
                        Objects.equals(parameters, that.parameters);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, category, dynamic, description, label, docUrl, actionScope, parameters);
            }

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

                @Override
                public String toString() {
                    return MoreObjects.toStringHelper(this)
                            .add("name", name)
                            .add("label", label)
                            .add("defaultTruc", defaultTruc)
                            .add("type", type)
                            .add("description", description)
                            .add("defaultValue", defaultValue)
                            .add("implicit", implicit)
                            .add("canBeBlank", canBeBlank)
                            .add("placeHolder", placeHolder)
                            .add("configuration", configuration)
                            .toString();
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    ParameterDescriptor that = (ParameterDescriptor) o;
                    return implicit == that.implicit &&
                            canBeBlank == that.canBeBlank &&
                            Objects.equals(name, that.name) &&
                            Objects.equals(label, that.label) &&
                            Objects.equals(defaultTruc, that.defaultTruc) &&
                            Objects.equals(type, that.type) &&
                            Objects.equals(description, that.description) &&
                            Objects.equals(defaultValue, that.defaultValue) &&
                            Objects.equals(placeHolder, that.placeHolder) &&
                            Objects.equals(configuration, that.configuration);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(name, label, defaultTruc, type, description, defaultValue, implicit, canBeBlank,
                            placeHolder, configuration);
                }
            }
        }

        public static class ActionDescriptor {

            public String action;

            // Use of JSonNode because some actions (ie Lookup) have some more complex parameters
            public Map<String, JsonNode> parameters;

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this).add("action", action).add("parameters", parameters).toString();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ActionDescriptor that = (ActionDescriptor) o;
                return Objects.equals(action, that.action) && Objects.equals(parameters, that.parameters);
            }

            @Override
            public int hashCode() {
                return Objects.hash(action, parameters);
            }
        }
    }
}
