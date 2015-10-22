package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.NEGATIVE;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.POSITIVE;
import static org.talend.dataprep.transformation.api.transformer.suggestion.rules.GenericRule.GenericRuleBuilder.forActions;

import java.util.List;
import java.util.StringTokenizer;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.transformation.api.action.metadata.text.LowerCase;
import org.talend.dataprep.transformation.api.action.metadata.text.ProperCase;
import org.talend.dataprep.transformation.api.action.metadata.text.Trim;
import org.talend.dataprep.transformation.api.action.metadata.text.UpperCase;
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
                        if (!patternAsString.isEmpty()) {
                            if (patternAsString.charAt(0) == ' ' || patternAsString.charAt(patternAsString.length() - 1) == ' ') {
                                // At least a pattern has a space (at beginning or end) in it, Trim should be suggested.
                                return POSITIVE;
                            }
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
                        if (!patternAsString.isEmpty()) {
                            if (patternAsString.indexOf('a') >= 0) {
                                // At least a pattern has a lower case in it, Upper case should be suggested.
                                return POSITIVE;
                            }
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
                        if (!patternAsString.isEmpty()) {
                            if (patternAsString.indexOf('A') >= 0) {
                                // At least a pattern has a upper case in it, Lower case should be suggested.
                                return POSITIVE;
                            }
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
                .then(columnMetadata -> {
                    final List<PatternFrequency> patterns = columnMetadata.getStatistics().getPatternFrequencies();
                    for (PatternFrequency pattern : patterns) {
                        final String patternAsString = pattern.getPattern();
                        StringTokenizer tokenizer = new StringTokenizer(patternAsString, " ");
                        while (tokenizer.hasMoreTokens()) {
                            final String token = tokenizer.nextToken();
                            if (!token.isEmpty()) {
                                if (token.charAt(0) != 'A') {
                                    // First character of word is not proper case, Proper Case should be suggested.
                                    return POSITIVE;
                                }
                                for (int i = 1; i < token.length(); i++) {
                                    if (token.charAt(i) != 'a') {
                                        // A remaining character of word is not proper case, Proper Case should be
                                        // suggested.
                                        return POSITIVE;
                                    }
                                }
                            }
                        }
                    }
                    return NEGATIVE;
                }) //
                .build();
    }
}
