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
import static org.talend.dataprep.helper.api.ActionParamEnum.FROM_PATTERN_MODE;
import static org.talend.dataprep.helper.api.ActionParamEnum.NEW_PATTERN;
import static org.talend.dataprep.helper.api.ActionParamEnum.ROW_ID;
import static org.talend.dataprep.helper.api.ActionParamEnum.SCOPE;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import cucumber.api.DataTable;
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
        action.parameters.put(FROM_PATTERN_MODE, params.get(FROM_PATTERN_MODE.getName()));
        action.parameters.put(NEW_PATTERN, params.get(NEW_PATTERN.getName()));
        action.parameters.put(SCOPE, null == params.get(SCOPE.getName()) ? "column" : params.get(SCOPE.getName()));
        action.parameters.put(COLUMN_ID, params.get(COLUMN_ID.getName()));
        action.parameters.put(COLUMN_NAME, params.get(COLUMN_NAME.getName()));
        action.parameters.put(ROW_ID, params.get(ROW_ID.getName()));

        api.addAction(preparationId, action);
    }

}
