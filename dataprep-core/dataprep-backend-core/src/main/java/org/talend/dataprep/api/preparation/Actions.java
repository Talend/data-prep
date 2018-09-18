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

package org.talend.dataprep.api.preparation;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Javabean used to wrap a list of Action in json.
 */
public class Actions {

    /** The list of actions. */
    @JsonProperty("actions")
    private List<Action> actions = new LinkedList<>();

    /**
     * @return the list of actions.
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * @param actions the list of actions to set.
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
}
