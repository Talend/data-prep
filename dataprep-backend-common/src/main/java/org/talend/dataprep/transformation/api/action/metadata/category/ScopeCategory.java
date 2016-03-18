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

package org.talend.dataprep.transformation.api.action.metadata.category;

import static org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters.COLUMN_ID;
import static org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters.ROW_ID;

import java.util.Map;
import java.util.function.Predicate;

import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;

/**
 * List all scope category.
 */
public enum ScopeCategory {
                           CELL(ROW_ID, COLUMN_ID),
                           COLUMN(COLUMN_ID),
                           LINE(ROW_ID),
                           DATASET;

    private final Predicate<Map<String, String>> mandatoryParametersChecker;

    ScopeCategory(final ImplicitParameters... mandatoryParameters) {
        mandatoryParametersChecker = map -> {
            for (final ImplicitParameters param : mandatoryParameters) {
                if (!map.containsKey(param.getKey())) {
                    return false;
                }
            }
            return true;
        };
    }

    public static ScopeCategory from(final String scopeAsString) {
        if (scopeAsString == null) {
            return null;
        }
        return ScopeCategory.valueOf(scopeAsString.toUpperCase());
    }

    public boolean checkMandatoryParameters(final Map<String, String> parameters) {
        return mandatoryParametersChecker.test(parameters);
    }
}
