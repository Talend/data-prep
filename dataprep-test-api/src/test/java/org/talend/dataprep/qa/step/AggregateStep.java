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
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(AggregateStep.class);

    @When("^I apply an aggregation preparation \"(.*)\" with parameters :$")
    public void applyAnAggregation(String preparationName, DataTable dataTable) throws Exception {
        Map<String, String> params = new HashMap<>(dataTable.asMap(String.class, String.class));
        params.put("preparationId", context.getPreparationId(suffixName(preparationName)));

        Aggregate aggregate = createAggregate(params);

        Response response = api.applyAggragate(aggregate);
        response.then().statusCode(OK.value());

        context.storeObject("aggregate",
                objectMapper.readValue(response.body().print(), new TypeReference<List<AggregateResult>>() {
                }));
    }

    @Then("^The aggregate result with the operator \"(.*)\" is :$")
    public void testAggregate(String operator, DataTable dataTable) throws Exception {
        Map<String, String> params = dataTable.asMap(String.class, String.class);

        List<AggregateResult> aggregateResults = (List<AggregateResult>) (context.getObject("aggregate"));
        assertEquals(aggregateResults, toAggregateResult(params, operator));
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
            }
            result.add(element);
        }
        return result;
    }

    private Aggregate createAggregate(Map<String, String> params) {
        Aggregate aggregate = new Aggregate();
        AggregateOperation aggregateOperation = new AggregateOperation(params.get("operator"), params.get("columnId"));
        aggregate.addOperation(aggregateOperation);

        aggregate.preparationId = params.get("preparationId");
        aggregate.addGroupBy(params.get("groupBy"));
        aggregate.stepId = "head";
        return aggregate;
    }
}
