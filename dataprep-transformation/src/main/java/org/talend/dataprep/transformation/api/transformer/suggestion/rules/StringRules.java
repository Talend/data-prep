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

import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.*;
import static org.talend.dataprep.transformation.api.transformer.suggestion.rules.GenericRule.GenericRuleBuilder.forActions;

import java.util.List;
import java.util.StringTokenizer;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.transformation.actions.text.*;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

@Component
public class StringRules extends BasicRules {

    /**
     * @return A {@link SuggestionEngineRule rule} that hides Trim ("remove leading & trailing spaces") if no leading
     * and trailing spaces
     */
    @Bean
    public static SuggestionEngineRule trailingSpaceRule() {
        return forActions(Trim.TRIM_ACTION_NAME) //
                .when(IS_STRING) //
                .then(columnMetadata -> {
                    final List<PatternFrequency> patterns = columnMetadata.getStatistics().getPatternFrequencies();
                    for (PatternFrequency pattern : patterns) {
                        final String patternAsString = pattern.getPattern();
                        // At least a pattern has a space (at beginning or end) in it, Trim should be suggested
                        if (!patternAsString.isEmpty() && //
                                (patternAsString.charAt(0) == ' '
                                        || patternAsString.charAt(patternAsString.length() - 1) == ' ')) {
                            return HIGH;
                        }
                    }
                    return NEGATIVE;
                }) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that hides Upper Case if all values are in upper case.
     */
    @Bean
    public static SuggestionEngineRule upperCaseRule() {
        return forActions(UpperCase.UPPER_CASE_ACTION_NAME) //
                .when(IS_STRING) //
                .then(columnMetadata -> {
                    final List<PatternFrequency> patterns = columnMetadata.getStatistics().getPatternFrequencies();
                    for (PatternFrequency pattern : patterns) {
                        final String patternAsString = pattern.getPattern();
                        // At least a pattern has a lower case in it, Upper case should be suggested.
                        if (!patternAsString.isEmpty() && patternAsString.indexOf('a') >= 0) {
                            return LOW;
                        }
                    }
                    return NEGATIVE;
                }) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that hides Lower Case if all values are in lower case.
     */
    @Bean
    public static SuggestionEngineRule lowerCaseRule() {
        return forActions(LowerCase.LOWER_CASE_ACTION_NAME) //
                .when(IS_STRING) //
                .then(columnMetadata -> {
                    final List<PatternFrequency> patterns = columnMetadata.getStatistics().getPatternFrequencies();
                    for (PatternFrequency pattern : patterns) {
                        final String patternAsString = pattern.getPattern();
                        // At least a pattern has a upper case in it, Lower case should be suggested.
                        if (!patternAsString.isEmpty() && patternAsString.indexOf('A') >= 0) {
                            return LOW;
                        }
                    }
                    return NEGATIVE;
                }) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that hides title case if all values are in title case
     */
    @Bean
    public static SuggestionEngineRule properCaseRule() {
        return forActions(ProperCase.PROPER_CASE_ACTION_NAME) //
                .when(IS_STRING) //
                .then(StringRules::computeScoreForProperCaseAction) //
                .build();
    }

    /**
     * Compute score for case actions.
     * 
     * @param columnMetadata the column metadata to analyze.
     * @return the score for case actions.
     */
    private static Integer computeScoreForProperCaseAction(ColumnMetadata columnMetadata) {
        final List<PatternFrequency> patterns = columnMetadata.getStatistics().getPatternFrequencies();
        for (PatternFrequency pattern : patterns) {
            final String patternAsString = pattern.getPattern();
            // split words
            StringTokenizer tokenizer = new StringTokenizer(patternAsString, " ");
            while (tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken();
                if (!token.isEmpty()) {
                    // First character of word is not proper case, Proper Case should be suggested.
                    if (token.charAt(0) != 'A') {
                        return LOW;
                    }
                    // A remaining character of word is not proper case, Proper Case should be
                    // suggested.
                    for (int i = 1; i < token.length(); i++) {
                        if (token.charAt(i) != 'a') {
                            return LOW;
                        }
                    }
                }
            }
        }
        return NEGATIVE;
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that suggests 'Replace' on text columns
     */
    @Bean
    public static SuggestionEngineRule replaceRule() {
        return forActions(ReplaceOnValue.REPLACE_ON_VALUE_ACTION_NAME) //
                .when(IS_STRING) //
                .then(columnMetadata -> LOW) //
                .build();
    }

}
