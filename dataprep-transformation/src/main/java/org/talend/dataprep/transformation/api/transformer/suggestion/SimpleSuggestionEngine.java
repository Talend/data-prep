package org.talend.dataprep.transformation.api.transformer.suggestion;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

@Component
public class SimpleSuggestionEngine implements SuggestionEngine {

    @Autowired(required = false)
    private List<SuggestionEngineRule> rules = new ArrayList<>();

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
                .sorted(Comparator.comparing(Suggestion::getScore))
                .collect(Collectors.toList());
    }

    @Override
    public List<ActionMetadata> suggest(DataSet dataSet) {
        return Collections.emptyList();
    }

}
