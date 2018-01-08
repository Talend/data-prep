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

package org.talend.dataprep.transformation.actions.common;

import org.talend.dataprep.api.action.ActionDefinition;

/**
 * Model an action to perform on a dataset.
 *
 * This interface adds information and also hides some information when serializing (information specific to Data Prep
 * actions not (yet) exposed in action API).
 */
public interface InternalActionDefinition extends ActionDefinition {



}
