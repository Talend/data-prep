package org.talend.dataprep.transformation.api.transformer.suggestion;

import java.util.function.BiFunction;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

public interface SuggestionEngineRule extends BiFunction<ActionMetadata, ColumnMetadata, Integer> {

    int HOP = 10;

    int POSITIVE = HOP;

    int NEGATIVE = -1 * HOP;

    int NON_APPLICABLE = 0;
}
