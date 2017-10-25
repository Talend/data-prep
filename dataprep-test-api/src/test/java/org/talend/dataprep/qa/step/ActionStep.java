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

import java.util.Map;

import cucumber.api.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.helper.api.ActionParamEnum;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;

import static org.talend.dataprep.helper.api.ActionParamEnum.SCOPE;

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
        if(action.parameters.get(SCOPE) == null ){
            action.parameters.put(SCOPE, "column");
        }
        api.addAction(preparationId, action);
    }

    @Then("^I update the step \"(.*)\" on the preparation \"(.*)\" with the following parameters :$")
    public void updateStep(String stepName, String prepName, DataTable dataTable){
        // TODO !!!! 
    }
}
