package org.talend.dataprep.transformation.actions.common;

import static org.talend.dataprep.transformation.actions.common.RunnableAction.Builder.builder;

import java.util.HashMap;
import java.util.Map;

import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.validation.ActionMetadataValidation;

public class ActionFactory {

    /** The validator. */
    private final ActionMetadataValidation validator = new ActionMetadataValidation();

    /**
     * Get the scope category from parameters
     *
     * @param parameters the transformation parameters
     * @return the scope
     */
    private ScopeCategory getScope(final Map<String, String> parameters) {
        return ScopeCategory.from(parameters.get(ImplicitParameters.SCOPE.getKey()));
    }

    public final RunnableAction create(ActionDefinition actionDefinition, Map<String, String> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Parameters cannot be null.");
        }
        validator.checkScopeConsistency(actionDefinition, parameters);

        final Map<String, String> parametersCopy = new HashMap<>(parameters);
        final ScopeCategory scope = getScope(parametersCopy);

        return builder().withName(actionDefinition.getName()) //
                .withParameters(parametersCopy) //
                .withCompile(new CompileDataSetRowAction(parametersCopy, actionDefinition, scope))
                .withRow(new ApplyDataSetRowAction(actionDefinition, parameters, scope)) //
                .build();
    }

}
