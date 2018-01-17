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

package org.talend.dataprep.transformation.actions.category;

/**
 * Action scope is a concept that allow us to describe on which scope(s) each action can be applied.
 *
 * As example, and first implementation, scopes {@link #INVALID} and {@link #EMPTY} allow frontend to "plug" some actions
 * on DQ bar dynamically.
 */
public enum ActionScope {

    /**
     * Action works on invalid / valid values.
     */
    INVALID,
    /**
     * Action works on empty values.
     */
    EMPTY,
    /**
     * Actions works on the column metadata (not column's values).
     */
    COLUMN_METADATA,
    /**
     * Actions are hidden from actions list
     */
    HIDDEN_IN_ACTION_LIST,

    /**
     * Actions are showed only when filters are applied
     */
    COLUMN_FILTERED;

    /**
     * @return A "user friendly" name for the action scope.
     */
    public String getDisplayName() {
        return name().toLowerCase();
    }

}
