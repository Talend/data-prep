// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service;

import java.util.List;

import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.StepDiff;

/**
 * Java bean used to parse PreparationMessage from the PreparationAPI.
 *
 * PreparationMessage cannot be used since the steps are changed into a List&lt;String&gt; by the API.
 */
public class PreparationMessageForTest {

    private List<String> steps;

    private List<StepDiff> diff;

    private List<Action> actions;

    /**
     * @return the Steps
     */
    public List<String> getSteps() {
        return steps;
    }

    /**
     * @param steps the steps to set.
     */
    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    /**
     * @return the Diff
     */
    public List<StepDiff> getDiff() {
        return diff;
    }

    /**
     * @param diff the diff to set.
     */
    public void setDiff(List<StepDiff> diff) {
        this.diff = diff;
    }

    /**
     * @return the Actions
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * @param actions the actions to set.
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
}
