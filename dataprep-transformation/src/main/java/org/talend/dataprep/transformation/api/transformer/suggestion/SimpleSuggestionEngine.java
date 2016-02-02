//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Simple suggestion engine implementation.
 */
@Component
public class SimpleSuggestionEngine implements SuggestionEngine {

    /** Available rules. */
    @Autowired(required = false)
    private List<SuggestionEngineRule> rules = new ArrayList<>();

    /**
     * @see SuggestionEngine#score(Collection, ColumnMetadata)
     */
    @Override
    public List<Suggestion> score(Collection<ActionMetadata> actions, ColumnMetadata column) {
        return actions.stream() //
                .map(actionMetadata -> {
                    int score = 0;
                    for (SuggestionEngineRule rule : rules) {
                        score += rule.apply(actionMetadata, column);
                    }
                    return new Suggestion(actionMetadata, score);
                }) //
                .sorted((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * @see SuggestionEngine#suggest(DataSet)
     */
    @Override
    public List<ActionMetadata> suggest(DataSet dataSet) {
        // really simple implementation here :-)
        return Collections.emptyList();
    }

}
