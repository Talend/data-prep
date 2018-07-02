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

package org.talend.dataprep.qa.step;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.dataprep.helper.api.Aggregate;
import org.talend.dataprep.helper.api.AggregateOperation;
import org.talend.dataprep.helper.api.AggregateResult;
import org.talend.dataprep.qa.config.DataPrepStep;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.response.Response;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Step dealing with aggregation
 */
public class AggregateStep extends DataPrepStep {

    @When("^I apply an aggregation on the preparation \"(.*)\" with parameters :$")
    public void applyAnAggregationOnPreparation(String preparationName, DataTable dataTable) throws Exception {
        Map<String, String> params = new HashMap<>(dataTable.asMap(String.class, String.class));
        params.put("preparationId", context.getPreparationId(suffixName(preparationName)));

        Aggregate aggregate = createAggregate(params);

        Response response = api.applyAggragate(aggregate);
        response.then().statusCode(OK.value());

        context.storeObject("aggregate",
                objectMapper.readValue(response.body().print(), new TypeReference<List<AggregateResult>>() {
                }));
    }

    @When("^I fail to apply an aggregation preparation \"(.*)\" with parameters :$")
    public void applyAnAggregationOnPreparationFailed(String preparationName, DataTable dataTable) throws Exception {
        aggregationFailled(preparationName, null, dataTable, BAD_REQUEST.value());
    }

    @When("^I fail to apply an aggregation on non existing preparation \"(.*)\" with parameters :$")
    public void applyAnAggregationOnNonExistingPreparationFailed(String preparationName, DataTable dataTable) throws Exception {
        aggregationFailled(preparationName, null, dataTable, NOT_ACCEPTABLE.value());
    }

    @When("^I fail to apply an aggregation on preparation \"(.*)\" and dataSet \"(.*)\" with parameters :$")
    public void applyAnAggregationOnNonExistingPreparationFailed(String preparationName, String dataSetName, DataTable dataTable) throws Exception {
        aggregationFailled(preparationName, dataSetName, dataTable, BAD_REQUEST.value());
    }

    private void aggregationFailled(String preparationName, String dataSetName, DataTable dataTable, int value) throws Exception {
        Map<String, String> params = new HashMap<>(dataTable.asMap(String.class, String.class));
        if (preparationName != null ) {
            params.put("preparationId", context.getPreparationId(suffixName(preparationName)));
        }
        if (dataSetName != null) {
            params.put("dataSetId", context.getDatasetId(suffixName(dataSetName)));
        }

        Aggregate aggregate = createAggregate(params);

        Response response = api.applyAggragate(aggregate);
        response.then().statusCode(value);
    }

    @Then("^The aggregate result with the operator \"(.*)\" is :$")
    public void testAggregate(String operator, DataTable dataTable) throws Exception {
        Map<String, String> params = dataTable.asMap(String.class, String.class);

        List<AggregateResult> aggregateResults = (List<AggregateResult>) (context.getObject("aggregate"));
        assertEquals(toAggregateResult(params, operator), aggregateResults);
    }

    @When("^I apply an aggregation on the dataSet \"(.*)\" with parameters :$")
    public void applyAnAggregationOnDataSet(String dataSetName, DataTable dataTable) throws Exception {
        Map<String, String> params = new HashMap<>(dataTable.asMap(String.class, String.class));
        params.put("dataSetId", context.getDatasetId(suffixName(dataSetName)));

        Aggregate aggregate = createAggregate(params);

        Response response = api.applyAggragate(aggregate);
        response.then().statusCode(OK.value());

        context.storeObject("aggregate",
                objectMapper.readValue(response.body().print(), new TypeReference<List<AggregateResult>>() {
                }));
    }

    private List<AggregateResult> toAggregateResult(Map<String, String> params, String operator) {
        List<AggregateResult> result = new ArrayList<>();

        for (String data : params.keySet()) {
            AggregateResult element = new AggregateResult();
            element.data = data;
            switch (operator) {
            case "AVERAGE":
                element.average = params.get(data);
                break;
            case "MAX":
                element.max = params.get(data);
                break;
            case "MIN":
                element.min = params.get(data);
                break;
            case "SUM":
                element.sum = params.get(data);
                break;
            default:
                break;
            }
            result.add(element);
        }
        return result;
    }

    private Aggregate createAggregate(Map<String, String> params) {
        Aggregate aggregate = new Aggregate();
        AggregateOperation aggregateOperation = new AggregateOperation(params.get("operator"), params.get("columnId"));
        aggregate.addOperation(aggregateOperation);

        if (params.get("preparationId") != null) {
            aggregate.preparationId = params.get("preparationId");
            aggregate.stepId = getPreparationDetails(aggregate.preparationId).getHead();
        }
        if (params.get("dataSetId") != null) {
            aggregate.datasetId = params.get("dataSetId");
        }
        aggregate.addGroupBy(params.get("groupBy"));
        if (params.get("filter") != null) {
            aggregate.filter = params.get("filter");
        }

        return aggregate;
    }
}
