//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.category;

/**
 * Action scope is a concept that allow us to describe on which scope(s) each action can be applied.
 *
 * As example, and first implementation, scopes INVALID and EMPTY allow frontend to "plug" some actions on DQ bar
 * dynamically.
 *
 * More applications may be added later.
 *
 */
public enum ActionScope {

    EQUALS, //
    INVALID, //
    EMPTY, //
    COLUMN_METADATA;

    public String getDisplayName() {
        return name().toLowerCase();
    }

}
