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
            for(final ImplicitParameters param : mandatoryParameters) {
                if(! map.containsKey(param.getKey())) {
                    return false;
                }
            }
            return true;
        };
    }

    public boolean checkMandatoryParameters(final Map<String, String> parameters) {
        return mandatoryParametersChecker.test(parameters);
    }

    public static ScopeCategory from(final String scopeAsString) {
        if(scopeAsString == null) {
            return null;
        }
        return ScopeCategory.valueOf(scopeAsString.toUpperCase());
    }
}
