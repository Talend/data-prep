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

package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.EMPTY_MGT;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.NEGATIVE;
import static org.talend.dataprep.transformation.api.transformer.suggestion.rules.GenericRule.GenericRuleBuilder.forActions;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.actions.delete.DeleteEmpty;
import org.talend.dataprep.transformation.actions.fill.FillIfEmpty;
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
                        return EMPTY_MGT;
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
                                return EMPTY_MGT;
                            }
                            return NEGATIVE;
                        }) //
                        .build();
    }
}
