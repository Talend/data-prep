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
 * Checks whether or not current user is allowed to perform the a specified action.
 */
@FunctionalInterface
public interface AccessGrantChecker {

    /**
     * Returns either true if the user is allowed to perform the specified restricted action or false otherwise.
     *
     * @param action the specified restricted action
     * @return either true if the user is allowed to perform the specified restricted action or false otherwise
     */
    boolean allowed(RestrictedAction action);
}
