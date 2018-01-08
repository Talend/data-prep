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

import static org.talend.dataprep.helper.api.ActionParamEnum.COLUMN_ID;
import static org.talend.dataprep.helper.api.ActionParamEnum.COLUMN_NAME;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.PreparationDetails;

import com.jayway.restassured.response.Response;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Step dealing with action
 */
public class ActionStep extends DataPrepStep {

    public static final String ACTION_NAME = "actionName";

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ActionStep.class);

    @When("^I add a step with parameters :$")
    public void whenIAddAStepToAPreparation(DataTable dataTable) {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(suffixName(params.get(PREPARATION_NAME)));
        Action action = new Action();
        util.mapParamsToAction(params, action);
        api.addAction(prepId, action);
    }

    @When("^I add a step identified by \"(.*)\" with parameters :$")
    public void whenIAddAStepToAPreparation(String stepAlias, DataTable dataTable) throws IOException {
        // step creation
        whenIAddAStepToAPreparation(dataTable);
        // we recover the preparation details in order to get an action object with the step Id
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(suffixName(params.get(PREPARATION_NAME)));
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
        String prepId = context.getPreparationId(suffixName(preparationName));
        Action storedAction = context.getAction(stepAlias);
        List<Action> actions = getActionsFromStoredAction(prepId, storedAction);
        Assert.assertTrue(actions.contains(storedAction));
    }

    @Then("^I update the first step like \"(.*)\" on the preparation \"(.*)\" with the following parameters :$")
    public void updateStep(String stepName, String prepName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(suffixName(prepName));
        Action storedAction = context.getAction(stepName);
        Assert.assertTrue(storedAction != null);
        List<Action> actions = getActionsFromStoredAction(prepId, storedAction);
        Assert.assertTrue(actions.size() > 0);
        // update stored action parameters
        util.mapParamsToAction(params, storedAction);
        storedAction.id = actions.get(0).id;
        Response response = api.updateAction(prepId, storedAction.id, storedAction);
        response.then().statusCode(200);
    }

    @And("^I move the first step like \"(.*)\" after the first step like \"(.*)\" on the preparation \"(.*)\"$")
    public void successToMoveStep(String stepName, String parentStepName, String prepName) throws IOException {
        moveStep(stepName, parentStepName, suffixName(prepName)).then().statusCode(200);
    }

    @Then("^I fail to move the first step like \"(.*)\" after the first step like \"(.*)\" on the preparation \"(.*)\"$")
    public void failToMoveStep(String stepName, String parentStepName, String prepName) throws IOException {
        moveStep(stepName, parentStepName, suffixName(prepName)).then().statusCode(409);
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
                .filter(action -> action.action.equals(storedAction.action) //
                        && action.parameters.equals(storedAction.parameters)) //
                .collect(Collectors.toList());
        return actions;
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
