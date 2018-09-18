package org.talend.dataprep.transformation.pipeline.builder;

import java.util.Map;
import java.util.function.Predicate;

import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.preparation.Action;

public class ActionsProfile {

    private final boolean needFullAnalysis;

    private final boolean needOnlyInvalidAnalysis;

    private final Predicate<ColumnMetadata> filterForFullAnalysis;

    private final Predicate<ColumnMetadata> filterForInvalidAnalysis;

    private final Predicate<ColumnMetadata> filterForPatternAnalysis;

    private final Map<Action, ActionDefinition> metadataByAction;

    public ActionsProfile(final boolean needFullAnalysis, final boolean needOnlyInvalidAnalysis,
            final Predicate<ColumnMetadata> filterForFullAnalysis,
            final Predicate<ColumnMetadata> filterForInvalidAnalysis,
            final Predicate<ColumnMetadata> filterForPatternAnalysis, Map<Action, ActionDefinition> metadataByAction) {
        this.needFullAnalysis = needFullAnalysis;
        this.needOnlyInvalidAnalysis = needOnlyInvalidAnalysis;
        this.filterForFullAnalysis = filterForFullAnalysis;
        this.filterForInvalidAnalysis = filterForInvalidAnalysis;
        this.filterForPatternAnalysis = filterForPatternAnalysis;
        this.metadataByAction = metadataByAction;
    }

    public Predicate<ColumnMetadata> getFilterForFullAnalysis() {
        return filterForFullAnalysis;
    }

    public Predicate<ColumnMetadata> getFilterForPatternAnalysis() {
        return filterForPatternAnalysis;
    }

    public Predicate<ColumnMetadata> getFilterForInvalidAnalysis() {
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
