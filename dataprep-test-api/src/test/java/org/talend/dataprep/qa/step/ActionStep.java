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

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ActionStep.class);

    @When("^I add a step with parameters :$")
    public void whenIAddAStepToAPreparation(DataTable dataTable) {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(params.get(PREPARATION_NAME));
        Action action = new Action();
        mapParamsToAction(params, action);
        api.addAction(prepId, action);
    }

    @When("^I add a step identified by \"(.*)\" with parameters :$")
    public void whenIAddAStepToAPreparation(String stepAlias, DataTable dataTable) throws IOException {
        // step creation
        whenIAddAStepToAPreparation(dataTable);
        // we recover the preparation details in order to get an action object with the step Id
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(params.get(PREPARATION_NAME));
        Action action = getLastActionfromPreparation(prepId);
        context.storeAction(stepAlias, action);
    }

    @Deprecated
    @Given("^A step with the following parameters exists on the preparation \"(.*)\" :$") //
    public void existStep(String preparationName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(preparationName);
        PreparationDetails prepDet = getPreparationDetails(prepId);
        List<Action> actions = prepDet.actions.stream() //
                .filter(action -> action.action.equals(params.get(ACTION_NAME))) //
                .filter(action -> action.parameters.get(COLUMN_ID).equals(params.get(COLUMN_ID.getName()))) //
                .filter(action -> action.parameters.get(COLUMN_NAME).equals(params.get(COLUMN_NAME.getName()))) //
                .collect(Collectors.toList());
        Assert.assertEquals(1, actions.size());
    }

    @Given("^I check that a step like \"(.*)\" exists in the preparation \"(.*)\"$")
    public void existStep(String stepAlias, String preparationName) throws IOException {
        String prepId = context.getPreparationId(preparationName);
        Action storedAction = context.getAction(stepAlias);
        List<Action> actions = getActionsFromStoredAction(prepId, storedAction);
        Assert.assertTrue(actions.contains(storedAction));
    }

    @Then("^I update the first step like \"(.*)\" on the preparation \"(.*)\" with the following parameters :$")
    public void updateStep(String stepName, String prepName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(prepName);
        Action storedAction = context.getAction(stepName);
        Assert.assertTrue(storedAction != null);
        List<Action> actions = getActionsFromStoredAction(prepId, storedAction);
        Assert.assertTrue(actions.size() > 0);
        // update stored action parameters
        mapParamsToAction(params, storedAction);
        storedAction.id = actions.get(0).id;
        Response response = api.updateAction(prepId, storedAction.id, storedAction);
        response.then().statusCode(200);
    }

    @Given("^I move the first step like \"(.*)\" after the first step like \"(.*)\" on the preparation \"(.*)\"$")
    public void successToMoveStep(String stepName, String parentStepName, String prepName) throws IOException {
        moveStep(stepName, parentStepName, prepName).then().statusCode(200);
    }

    @Given("^I fail to move the first step like \"(.*)\" after the first step like \"(.*)\" on the preparation \"(.*)\"$")
    public void failToMoveStep(String stepName, String parentStepName, String prepName) throws IOException {
        moveStep(stepName, parentStepName, prepName).then().statusCode(409);
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

    /**
     * Get the last {@link Action} from a preparation.
     *
     * @param preparationId the preparation id.
     * @return the last preparation {@link Action}.
     * @throws IOException
     */
    private Action getLastActionfromPreparation(String preparationId) throws IOException {
        PreparationDetails prepDet = getPreparationDetails(preparationId);
        prepDet.updateActionIds();
        return prepDet.actions.get(prepDet.actions.size() - 1);
    }

    /**
     * Recover a list of {@link Action} corresponding to a stored {@link Action} type in a given preparation.
     *
     * @param preparationId the preparation id.
     * @param storedAction the stored {@link Action} type.
     * @return a {@link List} of {@link Action} that looks like the given storedAction.
     * @throws IOException
     */
    private List<Action> getActionsFromStoredAction(String preparationId, Action storedAction) throws IOException {
        PreparationDetails prepDet = getPreparationDetails(preparationId);
        prepDet.updateActionIds();
        List<Action> actions = prepDet.actions.stream() //
                .filter(action -> action.action.equals(storedAction.action)) //
                .filter(action -> action.parameters.equals(storedAction.parameters)) //
                .collect(Collectors.toList());
        return actions;
    }

    /**
     * Map parameters from a Cucumber step to an {@link Action}.
     *
     * @param params the parameters to map.
     * @param action the {@link Action} that will receive the parameters.
     * @return the given {@link Action} updated.
     */
    private Action mapParamsToAction(Map<String, String> params, Action action) {
        action.action = params.get(ACTION_NAME) == null ? action.action : params.get(ACTION_NAME);
        params.forEach((k, v) -> {
            ActionParamEnum ape = ActionParamEnum.getActionParamEnum(k);
            if (ape != null) {
                action.parameters.put(ape, v);
            }
        });
        if (action.parameters.get(SCOPE) == null) {
            action.parameters.put(SCOPE, "column");
        }
        return action;
    }

    /**
     * Try to move a step after another step called parentStep.
     *
     * @param stepName the step to move.
     * @param parentStepName the parent step.
     * @param prepName the preparation name.
     * @return the response.
     * @throws IOException
     */
    private Response moveStep(String stepName, String parentStepName, String prepName) throws IOException {
        String prepId = context.getPreparationId(prepName);
        Action action = getActionsFromStoredAction(prepId, context.getAction(stepName)).get(0);
        Action parentAction = getActionsFromStoredAction(prepId, context.getAction(parentStepName)).get(0);
        return api.moveAction(prepId, action.id, parentAction.id);
    }
}