package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.NEGATIVE;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.POSITIVE;
import static org.talend.dataprep.transformation.api.transformer.suggestion.rules.GenericRule.GenericRuleBuilder.forActions;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.delete.DeleteEmpty;
import org.talend.dataprep.transformation.api.action.metadata.fillempty.FillIfEmpty;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

@Component
public class EmptyRules extends BasicRules {

    private static long getEmptyCount(ColumnMetadata columnMetadata) {
        return Math.max(columnMetadata.getStatistics().getEmpty(), columnMetadata.getQuality().getEmpty());
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that hides "delete empty" if no empty.
     */
    @Bean
    public static SuggestionEngineRule deleteEmptyRule() {
        return forActions(DeleteEmpty.DELETE_EMPTY_ACTION_NAME) //
                .then(columnMetadata -> {
                    if (getEmptyCount(columnMetadata) > 0) {
                        return POSITIVE;
                    }
                    return NEGATIVE;
                }) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that hides "fill empty" if no empty.
     */
    @Bean
    public static SuggestionEngineRule fillEmptyRule() {
        return forActions(FillIfEmpty.FILL_EMPTY_ACTION_NAME) //
                        .then(columnMetadata -> {
                            if (getEmptyCount(columnMetadata) > 0) {
                                return POSITIVE;
                            }
                            return NEGATIVE;
                        }) //
                        .build();
    }
}
