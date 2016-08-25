package org.talend.dataprep.transformation.pipeline.builder;

import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.ColumnMetadata;

public class ActionsProfile {

    private final boolean needFullAnalysis;

    private final boolean needOnlyInvalidAnalysis;

    private final Predicate<ColumnMetadata> filterForFullAnalysis;

    private final Predicate<ColumnMetadata> filterForInvalidAnalysis;

    private final Predicate<ColumnMetadata> filterForPatternAnalysis;

    public ActionsProfile(final boolean needFullAnalysis, final boolean needOnlyInvalidAnalysis,
            final Predicate<ColumnMetadata> filterForFullAnalysis, final Predicate<ColumnMetadata> filterForInvalidAnalysis,
            final Predicate<ColumnMetadata> filterForPatternAnalysis) {
        this.needFullAnalysis = needFullAnalysis;
        this.needOnlyInvalidAnalysis = needOnlyInvalidAnalysis;
        this.filterForFullAnalysis = filterForFullAnalysis;
        this.filterForInvalidAnalysis = filterForInvalidAnalysis;
        this.filterForPatternAnalysis = filterForPatternAnalysis;
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
}
