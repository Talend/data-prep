package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.NEGATIVE;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.POSITIVE;
import static org.talend.dataprep.transformation.api.transformer.suggestion.rules.GenericRule.GenericRuleBuilder.forActions;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.transformation.api.action.metadata.math.Absolute;
import org.talend.dataprep.transformation.api.action.metadata.math.Ceil;
import org.talend.dataprep.transformation.api.action.metadata.math.Floor;
import org.talend.dataprep.transformation.api.action.metadata.math.Round;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

@Component
public class IntegerRules extends BasicRules {

    /**
     * @return A {@link SuggestionEngineRule rule} that hides "absolute" actions if all numbers >=0.
     */
    @Bean
    public static SuggestionEngineRule absoluteRule() {
        return forActions(Absolute.ABSOLUTE_ACTION_NAME) //
                .when(IS_NUMERIC) //
                .then(columnMetadata -> {
                    if (columnMetadata.getStatistics().getMin() >= 0) {
                        return NEGATIVE;
                    } else {
                        return POSITIVE;
                    }
                }) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that hides floor, ceil & round actions if all integers.
     */
    @Bean
    public static SuggestionEngineRule integerRule() {
        return forActions(Floor.FLOOR_ACTION_NAME, Round.ROUND_ACTION_NAME, Ceil.CELL_ACTION_NAME) //
                .when(IS_NUMERIC) //
                .then(columnMetadata -> {
                    final List<PatternFrequency> patterns = columnMetadata.getStatistics().getPatternFrequencies();
                    if (patterns.size() == 1 && patterns.get(0).getPattern().indexOf('.') > 0) {
                        return POSITIVE;
                    } else {
                        return NEGATIVE;
                    }
                }) //
                .build();
    }
}
