//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.transformer.suggestion;

import java.util.List;
import java.util.stream.Stream;

import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;

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
     * @param actions A collection of {@link ActionDefinition actions} to be ranked.
     * @param column The {@link ColumnMetadata column information} to be used to rank actions.
     * @return A ordered collection of {@link Suggestion suggestions}.
     */
    Stream<Suggestion> score(Stream<ActionDefinition> actions, ColumnMetadata column);

    /**
     * Returns a list of {@link ActionDefinition actions} to improve quality of data set's content. Implementations may
     * not provide suggestions, but are required to <b>at least</b> return an empty list of {@link ActionDefinition
     * actions}.
     *
     * @param dataSet A {@link DataSet data set} that contains data to improve.
     * @return A ordered list of actions to execute to improve data set quality.
     */
    List<ActionDefinition> suggest(DataSet dataSet);
}
