package org.talend.dataprep.transformation.pipeline.builder;

import java.util.Map;
import java.util.function.Predicate;

import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.preparation.Action;

public class ActionsProfile {

    private final boolean needFullAnalysis;

    private final boolean needOnlyInvalidAnalysis;

    private final Predicate<String> filterForFullAnalysis;

    private final Predicate<String> filterForInvalidAnalysis;

    private final Predicate<String> filterForPatternAnalysis;

    private final Map<Action, ActionDefinition> metadataByAction;

    public ActionsProfile(final boolean needFullAnalysis, final boolean needOnlyInvalidAnalysis,
                          final Predicate<String> filterForFullAnalysis, final Predicate<String> filterForInvalidAnalysis,
                          final Predicate<String> filterForPatternAnalysis, Map<Action, ActionDefinition> metadataByAction) {
        this.needFullAnalysis = needFullAnalysis;
        this.needOnlyInvalidAnalysis = needOnlyInvalidAnalysis;
        this.filterForFullAnalysis = filterForFullAnalysis;
        this.filterForInvalidAnalysis = filterForInvalidAnalysis;
        this.filterForPatternAnalysis = filterForPatternAnalysis;
        this.metadataByAction = metadataByAction;
    }

    public Predicate<String> getFilterForFullAnalysis() {
        return filterForFullAnalysis;
    }

    public Predicate<String> getFilterForPatternAnalysis() {
        return filterForPatternAnalysis;
    }

    public Predicate<String> getFilterForInvalidAnalysis() {
        return filterForInvalidAnalysis;
    }

    public boolean needFullAnalysis() {
        return needFullAnalysis;
    }

    public boolean needOnlyInvalidAnalysis() {
        return needOnlyInvalidAnalysis;
    }

    public Map<Action, ActionDefinition> getMetadataByAction() {
        return metadataByAction;
    }
}
