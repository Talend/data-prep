// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import static org.talend.dataprep.helper.api.ActionParamEnum.COLUMN_ID;
import static org.talend.dataprep.helper.api.ActionParamEnum.COLUMN_NAME;
import static org.talend.dataprep.helper.api.ActionParamEnum.SCOPE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.helper.api.ActionParamEnum;
import org.talend.dataprep.qa.dto.PreparationDetails;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import com.jayway.restassured.response.Response;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Step dealing with action
 */
public class ActionStep extends DataPrepStep {

    public static final String ACTION_NAME = "actionName";

    public static final String PREPARATION_NAME = "preparationName";

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ActionStep.class);

    @When("^I add a step with parameters :$")
    public void whenIAddAStepToAPreparation(DataTable dataTable) {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String preparationId = context.getPreparationId(params.get(PREPARATION_NAME));
        Action action = new Action();
        action.action = params.get(ACTION_NAME);
        params.forEach((k, v) -> {
            ActionParamEnum ape = ActionParamEnum.getActionParamEnum(k);
            if (ape != null) {
                action.parameters.put(ape, v);
            }
        });
        if (action.parameters.get(SCOPE) == null) {
            action.parameters.put(SCOPE, "column");
        }
        api.addAction(preparationId, action);
    }

    @Given("^A step with the following parameters exists on the preparation \"(.*)\" :$") //
    public void existStep(String preparationName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String preparationId = context.getPreparationId(preparationName);
        PreparationDetails preparationDetails = getPreparationDetails(preparationId);
        List<PreparationDetails.Action> actions = preparationDetails.actions.stream() //
                .filter(action -> action.action.equals(params.get(ACTION_NAME))) //
                .filter(action -> action.parameters.get(COLUMN_ID).equals(params.get(COLUMN_ID.getName()))) //
                .filter(action -> action.parameters.get(COLUMN_NAME).equals(params.get(COLUMN_NAME.getName()))) //
                .collect(Collectors.toList());
        Assert.assertEquals(1, actions.size());
    }

    @Then("^I update the step \"(.*)\" on the preparation \"(.*)\" with the following parameters :$")
    public void updateStep(String stepName, String prepName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String preparationId = context.getPreparationId(prepName);
        PreparationDetails prepDet = getPreparationDetails(preparationId);
        // link the actions and the step id
        IntStream.range(0, prepDet.steps.size() - 1).forEach(i -> prepDet.actions.get(i).id = prepDet.steps.get(i + 1));

        List<PreparationDetails.Action> actions = prepDet.actions.stream() //
                .filter(action -> action.action.equals(stepName)) //
                .collect(Collectors.toList());
        // TODO if needed one day, we should change this method to be able to select the step byt its name and position in the
        // preparation.
        Assert.assertEquals(1, actions.size());

        Action action = new Action();
        action.action = stepName;
        action.parameters = prepDet.actions.get(0).parameters;
        params.forEach((k, v) -> {
            ActionParamEnum ape = ActionParamEnum.getActionParamEnum(k);
            if (ape != null) {
                action.parameters.put(ape, v);
            }
        });
        if (action.parameters.get(SCOPE) == null) {
            action.parameters.put(SCOPE, "column");
        }
        Response response = api.updateAction(preparationId, prepDet.actions.get(0).id, action);
        response.then().statusCode(200);
    }

    /**
     * Retrieve the details of a preparation from its id.
     * 
     * @param preparationId the preparation id.
     * @return the preparation details.
     * @throws IOException
     */
    private PreparationDetails getPreparationDetails(String preparationId) throws IOException {
        PreparationDetails preparationDetails = null;
        Response response = api.getPreparationDetails(preparationId);
        response.then().statusCode(200);
        final String content = IOUtils.toString(response.getBody().asInputStream(), StandardCharsets.UTF_8);
        preparationDetails = objectMapper.readValue(content, PreparationDetails.class);
        return preparationDetails;
    }
}
