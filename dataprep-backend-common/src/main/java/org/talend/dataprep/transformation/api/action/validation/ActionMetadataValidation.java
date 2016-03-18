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

package org.talend.dataprep.transformation.api.action.validation;

import static org.talend.dataprep.exception.error.CommonErrorCodes.*;
import static org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters.SCOPE;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

@Component
public class ActionMetadataValidation {

    /**
     * Get the scope category from parameters
     *
     * @param parameters the transformation parameters
     * @return the scope
     * @throws TDPException if the scope parameter is missing
     */
    private ScopeCategory getScope(final Map<String, String> parameters) {
        final ScopeCategory scope = ScopeCategory.from(parameters.get(SCOPE.getKey()));
        if (scope == null) {
            throw new TDPException(MISSING_ACTION_SCOPE);
        }
        return scope;
    }

    /**
     * Scope consistency checks
     * 1. scope has mandatory parameters
     * 2. scope is available for the current transformation
     *
     * @param action     the action metadata
     * @param parameters the transformation parameters
     * @throws TDPException if the scope is missing, not supported or
     */
    public void checkScopeConsistency(final ActionMetadata action, final Map<String, String> parameters) {
        final ScopeCategory scope = getScope(parameters);
        if (!action.acceptScope(scope)) {
            throw new TDPException(UNSUPPORTED_ACTION_SCOPE);
        }
        if (!scope.checkMandatoryParameters(parameters)) {
            throw new TDPException(MISSING_ACTION_SCOPE_PARAMETER);
        }
    }

}
