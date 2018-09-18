// ============================================================================
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

package org.talend.dataprep.api.preparation;

import java.util.ArrayList;
import java.util.List;

public class AppendStep {

    private StepDiff diff = new StepDiff();

    private List<Action> actions = new ArrayList<>(1);

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public void setDiff(StepDiff diff) {
        this.diff = diff;
    }

    public StepDiff getDiff() {
        return diff;
    }

}
