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

package org.talend.dataprep.actions.resources;

import java.util.List;

import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.actions.common.RunnableAction;

/**
 * A factory interface for creating {@link FunctionResource resources}.
 */
public interface FunctionResourceProvider {

    /**
     * Create a {@link FunctionResource resource} based on provided <code>actions</code>.
     * 
     * @param actions The list of {@link Action actions} to generate {@link FunctionResource resources} from.
     * @return A {@link FunctionResource} ready to be included in a {@link org.talend.dataprep.actions.SerializableFunction}.
     */
    FunctionResource get(List<RunnableAction> actions);

}
