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

package org.talend.dataprep.transformation.actions.common;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.transformation.actions.category.ActionScope;

/**
 * Model an action to perform on a dataset.
 *
 * This interface adds information and also hides some information when serializing (information specific to Data Prep
 * actions not (yet) exposed in action API).
 */
public interface InternalActionDefinition extends ActionDefinition {

    /**
     * Defines the list of scopes this action belong to.
     * <p>
     * Scope scope is a concept that allow us to describe on which scope(s) each action can be applied.
     *
     * @return list of scopes of this action
     * @see ActionScope
     */
    List<String> getActionScope();

    /**
     * @return <code>true</code> if the action is dynamic (i.e the parameters depends on the context (dataset / preparation /
     * previous_actions).
     */
    // Only here for JSON serialization purposes.
    boolean isDynamic();

    default String getDocUrl() {
        return StringUtils.EMPTY;
    }

}
