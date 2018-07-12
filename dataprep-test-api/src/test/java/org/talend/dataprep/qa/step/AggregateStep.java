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

    public static final String PREPARATION_ID = "preparationId";

    public static final String DATA_SET_ID = "dataSetId";

    @When("^I apply an aggregation \"(.*)\" on the preparation \"(.*)\" with parameters :$")
    public void applyAnAggregationOnPreparation(String aggregationName, String preparationName, DataTable dataTable) throws Exception {
        Map<String, String> params = new HashMap<>(dataTable.asMap(String.class, String.class));
        params.put(PREPARATION_ID, context.getPreparationId(suffixName(preparationName)));

        Aggregate aggregate = createAggregate(params);

        Response response = api.applyAggragate(aggregate);
        response.then().statusCode(OK.value());

        context.storeObject(suffixName(aggregationName),
                objectMapper.readValue(response.body().print(), new TypeReference<List<AggregateResult>>() {
                }));
    }

    @When("^I fail to apply an aggregation preparation \"(.*)\" with parameters :$")
    public void applyAnAggregationOnPreparationFailed(String preparationName, DataTable dataTable) throws Exception {
        aggregationFailed(preparationName, null, dataTable, BAD_REQUEST.value());
    }

    @When("^I fail to apply an aggregation on non existing preparation \"(.*)\" with parameters :$")
    public void applyAnAggregationOnNonExistingPreparationFailed(String preparationName, DataTable dataTable)
            throws Exception {
        aggregationFailed(preparationName, null, dataTable, NOT_ACCEPTABLE.value());
    }

    @When("^I fail to apply an aggregation on preparation \"(.*)\" and dataSet \"(.*)\" with parameters :$")
    public void applyAnAggregationOnNonExistingPreparationFailed(String preparationName, String dataSetName,
            DataTable dataTable) throws Exception {
        aggregationFailed(preparationName, dataSetName, dataTable, BAD_REQUEST.value());
    }

    private void aggregationFailed(String preparationName, String dataSetName, DataTable dataTable, int value)
            throws Exception {
        Map<String, String> params = new HashMap<>(dataTable.asMap(String.class, String.class));
        if (preparationName != null) {
            params.put(PREPARATION_ID, context.getPreparationId(suffixName(preparationName)));
        }
        if (dataSetName != null) {
            params.put(DATA_SET_ID, context.getDatasetId(suffixName(dataSetName)));
        }

        Aggregate aggregate = createAggregate(params);

        Response response = api.applyAggragate(aggregate);
        response.then().statusCode(value);
    }

    @Then("^The aggregation \"(.*)\" results with the operator \"(.*)\" is :$")
    public void testAggregate(String aggregationName, String operator, DataTable dataTable) throws Exception {
        Map<String, String> params = dataTable.asMap(String.class, String.class);

        List<AggregateResult> aggregateResults = (List<AggregateResult>) (context.getObject(suffixName(aggregationName)));
        assertEquals(toAggregateResult(params, operator), aggregateResults);
    }

    @When("^I apply an aggregation \"(.*)\" on the dataSet \"(.*)\" with parameters :$")
    public void applyAnAggregationOnDataSet(String aggregationName, String dataSetName, DataTable dataTable) throws Exception {
        Map<String, String> params = new HashMap<>(dataTable.asMap(String.class, String.class));
        params.put(DATA_SET_ID, context.getDatasetId(suffixName(dataSetName)));

        Aggregate aggregate = createAggregate(params);

        Response response = api.applyAggragate(aggregate);
        response.then().statusCode(OK.value());

        context.storeObject(suffixName(aggregationName),
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

        if (params.get(PREPARATION_ID) != null) {
            aggregate.preparationId = params.get(PREPARATION_ID);
            aggregate.stepId = getPreparationDetails(aggregate.preparationId).getHead();
        }
        if (params.get(DATA_SET_ID) != null) {
            aggregate.datasetId = params.get(DATA_SET_ID);
        }
        aggregate.addGroupBy(params.get("groupBy"));
        if (params.get("filter") != null) {
            aggregate.filter = params.get("filter");
        }

        return aggregate;
    }
}
