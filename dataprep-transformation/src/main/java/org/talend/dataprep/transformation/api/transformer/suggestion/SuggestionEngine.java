package org.talend.dataprep.transformation.api.transformer.suggestion;

import java.util.Collection;
import java.util.List;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

public interface SuggestionEngine {

    /**
     * <p>
     * Scores all <code>actions</code> for given <code>column</code>. Each suggestion is ranked from 0 to 1 and returned
     * list is sorted in decreasing rank order (highest rank first, lowest rank last).
     * </p>
     * <p>
     * This method only operates on {@link ColumnMetadata}, meaning it can <b>not</b> decide based on content, only
     * based on metadata.
     * </p>
     * 
     * @param actions A collection of {@link ActionMetadata actions} to be ranked.
     * @param column The {@link ColumnMetadata column information} to be used to rank actions.
     * @return A ordered collection of {@link Suggestion suggestions}.
     */
    List<Suggestion> score(Collection<ActionMetadata> actions, ColumnMetadata column);

    /**
     * Returns a list of {@link ActionMetadata actions} to improve quality of data set's content. Implementations may
     * not provide suggestions, but are required to <b>at least</b> return an empty list of {@link ActionMetadata
     * actions}.
     * 
     * @param dataSet A {@link DataSet data set} that contains data to improve.
     * @return A ordered list of actions to execute to improve data set quality.
     */
    List<ActionMetadata> suggest(DataSet dataSet);
}
