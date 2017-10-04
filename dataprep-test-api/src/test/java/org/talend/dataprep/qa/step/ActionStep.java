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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.qa.step.config.DataPrepStep;

import cucumber.api.DataTable;
import cucumber.api.java.en.When;

/**
 * Step dealing with action
 */
public class ActionStep extends DataPrepStep {

    public static final String PREPARATION_NAME = "preparationName";

    public static final String ACTION_NAME = "actionName";

    public static final String COLUMN_NAME = "columnName";

    public static final String COLUMN_ID = "columnId";

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ActionStep.class);

    @When("^I add a step with parameters :$")
    public void whenIAddAStepToAPreparation(DataTable dataTable) {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        String preparationId = context.getPreparationId(params.get(PREPARATION_NAME));
        api.addStep(preparationId, params.get(ACTION_NAME), params.get(COLUMN_NAME), params.get(COLUMN_ID));
    }

}
