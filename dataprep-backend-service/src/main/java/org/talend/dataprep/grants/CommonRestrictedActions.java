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

package org.talend.dataprep.grants;

/**
 * Enum keeping track of Restricted actions.
 */
public enum CommonRestrictedActions implements RestrictedAction {

    CERTIFICATION("CERTIFICATION");

    /**
     * The name of the action
     */
    private final String action;

    /**
     * Constructor.
     * 
     * @param action the specified action
     */
    CommonRestrictedActions(String action) {
        this.action = action;
    }

    @Override
    public String action() {
        return action;
    }
}
