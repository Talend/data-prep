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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;

/**
 * Simple suggestion engine implementation.
 */
@Component
public class SimpleSuggestionEngine implements SuggestionEngine {

    /** Available rules. */
    @Autowired(required = false)
    private List<SuggestionEngineRule> rules = new ArrayList<>();

    /**
     * @see SuggestionEngine#score(Stream, ColumnMetadata)
     */
    @Override
    public Stream<Suggestion> score(Stream<ActionDefinition> actions, ColumnMetadata column) {
        return actions.map(actionMetadata -> { //
                    int score = 0;
                    for (SuggestionEngineRule rule : rules) {
                        score += rule.apply(actionMetadata, column);
                    }
                    return new Suggestion(actionMetadata, score);
                }) //
                .sorted((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));
    }

    /**
     * @see SuggestionEngine#suggest(DataSet)
     */
    @Override
    public List<ActionDefinition> suggest(DataSet dataSet) {
        // really simple implementation here :-)
        return Collections.emptyList();
    }

}
