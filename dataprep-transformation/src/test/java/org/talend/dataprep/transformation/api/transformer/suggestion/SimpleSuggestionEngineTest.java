// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.api.transformer.suggestion;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.transformation.actions.delete.DeleteEmpty;
import org.talend.dataprep.transformation.actions.delete.DeleteInvalid;
import org.talend.dataprep.transformation.actions.fill.FillIfEmpty;
import org.talend.dataprep.transformation.actions.fill.FillInvalid;
import org.talend.dataprep.transformation.actions.math.Absolute;
import org.talend.dataprep.transformation.actions.text.UpperCase;
import org.talend.dataprep.transformation.api.transformer.suggestion.rules.EmptyRules;
import org.talend.dataprep.transformation.api.transformer.suggestion.rules.IntegerRules;
import org.talend.dataprep.transformation.api.transformer.suggestion.rules.InvalidRules;
import org.talend.dataprep.transformation.api.transformer.suggestion.rules.StringRules;
import org.talend.dataprep.transformation.api.transformer.suggestion.rules.TypeDomainRules;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the SimpleSuggestionEngine
 *
 * @see SimpleSuggestionEngine
 */
public class SimpleSuggestionEngineTest {

    /** The suggestion engine to test. */
    private final SimpleSuggestionEngine suggestionEngine = new SimpleSuggestionEngine();

    private ActionRegistry actionRegistry;

    public SimpleSuggestionEngineTest() {
        final List<SuggestionEngineRule> rules = new ArrayList<>();
        final InvalidRules invalidRules = new InvalidRules();

        // Invalid Rules
        rules.add(invalidRules.deleteInvalidRule());
        rules.add(invalidRules.fillInvalidRule());
        rules.add(invalidRules.clearInvalidRule());
        rules.add(invalidRules.standardizeInvalidRule());

        // Empty Rules
        rules.add(EmptyRules.deleteEmptyRule());
        rules.add(EmptyRules.fillEmptyRule());

        // Integer Rules
        rules.add(IntegerRules.absoluteRule());
        rules.add(IntegerRules.integerRule());
        rules.add(IntegerRules.mathRule());

        // Domain Types Rules
        rules.add(TypeDomainRules.dateRule());
        rules.add(TypeDomainRules.emailRule());
        rules.add(TypeDomainRules.urlRule());
        rules.add(TypeDomainRules.phoneRule());
        rules.add(TypeDomainRules.phoneExtractRule());
        rules.add(TypeDomainRules.dataMaskingRule());
        rules.add(TypeDomainRules.countryRule());

        // String Rules
        rules.add(StringRules.trailingSpaceRule());
        rules.add(StringRules.upperCaseRule());
        rules.add(StringRules.lowerCaseRule());
        rules.add(StringRules.properCaseRule());
        rules.add(StringRules.replaceRule());

        suggestionEngine.setRules(rules);
    }

    @Before
    public void setUp() {
        actionRegistry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");
    }

    @Test
    public void shouldSuggest() {
        Assert.assertThat(suggestionEngine.suggest(new DataSet()).size(), is(0));
    }

    @Test
    public void shouldSuggestionsShouldBeSorted() throws IOException {

        final String json = IOUtils.toString(this.getClass().getResourceAsStream("sample_column.json"), UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        final ColumnMetadata columnMetadata = mapper.readValue(json, ColumnMetadata.class);

        List<ActionDefinition> actions = new ArrayList<>();
        actions.add(new FillIfEmpty());
        actions.add(new FillInvalid());
        actions.add(new DeleteInvalid());
        actions.add(new DeleteEmpty());
        actions.add(new Absolute());
        actions.add(new UpperCase());
        final Stream<Suggestion> suggestions = suggestionEngine.score(actions.stream(), columnMetadata);

        int currentScore = Integer.MAX_VALUE;
        for (Suggestion suggestion : suggestions.collect(Collectors.toList())) {
            assertTrue(currentScore >= suggestion.getScore());
            currentScore = suggestion.getScore();
        }
    }

    @Test
    public void shouldTestSuggestColumnValid() throws IOException {
        // given
        final String json = IOUtils.toString(this.getClass().getResourceAsStream("sample_column.json"), UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        final ColumnMetadata columnMetadata = mapper.readValue(json, ColumnMetadata.class);

        String[] expectedResult =
                { "clear_invalid", "delete_invalid", "fillinvalidwithdefault", "standardize_value", "delete_empty" };

        // when
        String[] newActionFormArray = suggestionEngine //
                .score(actionRegistry.findAll(), columnMetadata) //
                .filter(i -> i.getScore() > 0) //
                .limit(5) //
                .map(a -> a.getAction().getName()) //
                .toArray(String[]::new);

        // then
        assertEquals(5, newActionFormArray.length);
        assertArrayEquals(expectedResult, newActionFormArray);
    }

    @Test
    public void shouldTestSuggestColumnString() throws IOException {
        // given
        final String json =
                IOUtils.toString(this.getClass().getResourceAsStream("metadata_gonfleurs_suggestion.json"), UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        final ColumnMetadata columnMetadata = mapper.readValue(json, ColumnMetadata.class);

        String[] expectedResult = { "lowercase", "propercase", "replace_on_value", "uppercase" };

        // when
        String[] newActionFormArray = suggestionEngine //
                .score(actionRegistry.findAll(), columnMetadata) //
                .filter(i -> i.getScore() > 0) //
                .limit(5) //
                .map(a -> a.getAction().getName()) //
                .toArray(String[]::new);

        // then
        assertEquals(4, newActionFormArray.length);
        assertArrayEquals(expectedResult, newActionFormArray);
    }
}
