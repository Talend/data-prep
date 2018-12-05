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

import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.config.DataPrepStep;
import org.talend.dataprep.qa.dto.PreparationContent;
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

    public static final String ACTION_LOOKUP = "lookup";

    private static final String LOOKUP_DS_ID = "lookup_ds_id";

    private static final String LOOKUP_DS_NAME = "lookup_ds_name";

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ActionStep.class);

    @When("^I add a \"(.*)\" step on the preparation \"(.*)\" with parameters :$")
    public void whenIAddAStepToAPreparation(String actionName, String preparationName, Map<String, String> params) {
        String prepId = context.getPreparationId(suffixName(preparationName));
        Action action = new Action();
        action.action = actionName;
        action.parameters.putAll(util.mapParamsToActionParameters(params));
        if (ACTION_LOOKUP.equals(actionName)) {
            String DatasetNamesuffixed = suffixName(params.get(LOOKUP_DS_NAME));
            String datasetId = context.getDatasetId(DatasetNamesuffixed);
            action.parameters.put(LOOKUP_DS_ID, datasetId);
            context.storeAction("", action, preparationName);
        }

        api.addAction(prepId, action).then().statusCode(200).log().ifValidationFails();
    }

    @When("^I add a \"(.*)\" step identified by \"(.*)\" on the preparation \"(.*)\" with parameters :$")
    public void whenIAddAStepWithAliasToAPreparation(String actionName, String stepAlias, String preparationName,
            Map<String, String> params) throws IOException {
        // step creation
        whenIAddAStepToAPreparation(actionName, preparationName, params);
        // we recover the preparation details in order to get an action object with the step Id
        String prepId = context.getPreparationId(suffixName(preparationName));
        Action action = getLastActionfromPreparation(prepId);
        context.storeAction(stepAlias, action, preparationName);
    }

    @Given("^I check that a step like \"(.*)\" exists in the preparation \"(.*)\"$")
    public void existStep(String stepAlias, String prepFullName) throws IOException {
        String prepSuffixedName = suffixName(util.extractNameFromFullName(prepFullName));
        String prepPath = util.extractPathFromFullName(prepFullName);
        String prepId = context.getPreparationId(prepSuffixedName, prepPath);
        Action storedAction = context.getAction(stepAlias);
        List<Action> actions = getActionsFromStoredAction(prepId, storedAction);
        assertTrue("No action \"" + storedAction.action + "\" has been stored in the IT context.",
                actions.contains(storedAction));
    }

    @Then("^I update the first step like \"(.*)\" on the preparation \"(.*)\" with the following parameters :$")
    public void updateStep(String stepName, String prepName, DataTable dataTable) throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(suffixName(prepName));
        Action storedAction = context.getAction(stepName);
        assertTrue("No Action on the step named \"" + stepName + "\" has been retrieve in the context.",
                storedAction != null);
        List<Action> actions = getActionsFromStoredAction(prepId, storedAction);
        assertTrue("Action list on the preparation named \"" + prepName + "\" is empty.", actions.size() > 0);
        // update stored action parameters
        storedAction.parameters.putAll(util.mapParamsToActionParameters(params));
        storedAction.id = actions.get(0).id;
        Response response = api.updateAction(prepId, storedAction.id, storedAction);
        response.then().statusCode(200);
    }

    @Given("^I update the first action with name \"(.*)\" on the preparation \"(.*)\" with the following parameters :$")
    public void updateFirstActionFoundWithName(String actionName, String prepName, DataTable dataTable)
            throws IOException {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String prepId = context.getPreparationId(suffixName(prepName));
        Action foundAction = getFirstActionWithName(prepId, actionName);
        assertTrue("No action with name \"" + actionName + "\" on the preparation named \"" + prepName + "\".",
                foundAction != null);
        // Update action
        Action action = new Action();
        action.action = actionName;
        action.id = foundAction.id;
        action.parameters = new HashMap<>(foundAction.parameters);
        action.parameters.putAll(util.mapParamsToActionParameters(params));

        Response response = api.updateAction(prepId, action.id, action);
        response.then().statusCode(200);
    }

    private Action getFirstActionWithName(String preparationId, String actionName) throws IOException {
        PreparationDetails prepDet = getPreparationDetails(preparationId);
        prepDet.updateActionIds();
        return prepDet.actions
                .stream() //
                .filter(action -> action.action.equals(actionName)) //
                .findFirst()
                .get();
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
        return prepDet.actions
                .stream() //
                .filter(action -> action.action.equals(storedAction.action) //
                        && action.parameters.equals(storedAction.parameters)) //
                .collect(Collectors.toList());
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

    @Given("^I remove the first action with name \"(.*)\" on the preparation \"(.*)\"$")
    public void removeFirstActionFoundWithName(String actionName, String prepName) throws IOException {
        String prepId = context.getPreparationId(suffixName(prepName));
        Action foundAction = getFirstActionWithName(prepId, actionName);
        assertTrue(foundAction != null);
        // Remove action
        Response response = api.deleteAction(prepId, foundAction.id);
        response.then().statusCode(200);
    }

    @When("^I disable the last \"(.*)\" steps of the preparation \"(.*)\"$")
    public void iDisableTheLastStepsOfThePreparation(int nbStepsToDisable, String prepName) throws Throwable {
        // Get steps in order to retrieve the id of the nth last one
        String prepId = context.getPreparationId(suffixName(prepName));
        List<String> steps = getPreparationDetails(prepId).steps;
        assertTrue("Not enough steps in the preparation to disable", steps.size() > nbStepsToDisable);
        String version = steps.get(steps.size() - nbStepsToDisable - 1);
        Response response = api.getPreparationContent(prepId, version, "HEAD", null);
        response.then().statusCode(OK.value());
        PreparationContent preparationContent = response.as(PreparationContent.class);
        context.storeObject("preparationContent", preparationContent);
    }
}
