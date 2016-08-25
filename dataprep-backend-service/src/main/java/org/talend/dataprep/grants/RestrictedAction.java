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

import java.io.Serializable;

/**
 * Represents a restricted action. To perform such kind of action user needs to be granted some privileges.
 */
@FunctionalInterface
public interface RestrictedAction extends Serializable {

    String action();

}
