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

import java.util.function.Function;
import java.util.function.Predicate;

import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

public class GenericRule implements SuggestionEngineRule {

    private final String[] actionNames;

    private final Predicate<ColumnMetadata> filter;

    private final Function<ColumnMetadata, Integer> rule;

    private GenericRule(Predicate<ColumnMetadata> filter, Function<ColumnMetadata, Integer> rule, String... actionNames) {
        this.filter = filter;
        this.rule = rule;
        this.actionNames = actionNames;
    }

    @Override
    public Integer apply(ActionDefinition actionMetadata, ColumnMetadata columnMetadata) {
        if (filter.test(columnMetadata)) {
            for (String actionName : actionNames) {
                if (actionName.equals(actionMetadata.getName())) {
                    return rule.apply(columnMetadata);
                }
            }
            return NON_APPLICABLE;
        }
        return NON_APPLICABLE;
    }

    public static class GenericRuleBuilder {

        private String[] actionsNames = new String[0];

        private Predicate<ColumnMetadata> filter = columnMetadata -> true;

        private Function<ColumnMetadata, Integer> rule = columnMetadata -> SuggestionEngineRule.NON_APPLICABLE;

        public GenericRuleBuilder(String... actionsNames) {
            this.actionsNames = actionsNames;
        }

        public static GenericRuleBuilder forActions(String... actionsNames) {
            return new GenericRuleBuilder(actionsNames);
        }

        public GenericRuleBuilder when(Predicate<ColumnMetadata> filter) {
            this.filter = filter;
            return this;
        }

        public GenericRuleBuilder then(Function<ColumnMetadata, Integer> rule) {
            this.rule = rule;
            return this;
        }

        public GenericRule build() {
            return new GenericRule(filter, rule, actionsNames);
        }
    }
}
