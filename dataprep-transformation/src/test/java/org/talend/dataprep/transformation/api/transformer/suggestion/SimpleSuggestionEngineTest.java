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
import static org.junit.Assert.assertTrue;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.action.ActionForm;
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
import org.talend.dataprep.transformation.service.TransformationService;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the SimpleSuggestionEngine
 *
 * @see SimpleSuggestionEngine
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleSuggestionEngineTest {

    /** The suggestion engine to test. */
    private SimpleSuggestionEngine simpleSuggestionEnginengine;

    /** The transformation service. */
    @InjectMocks
    private TransformationService transformationService;

    /** The suggestion engine to test. */
    private SimpleSuggestionEngine suggestionEngine;

    private ActionRegistry actionRegistry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");

    /**
     * Default constructor.
     */
    public SimpleSuggestionEngineTest() {
        simpleSuggestionEnginengine = new SimpleSuggestionEngine();

        // ReflectionUtils to save the use of a spring context
        List<SuggestionEngineRule> rules = new ArrayList<>();

        final InvalidRules invalidRules = new InvalidRules();

        // Invalide Rules
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

        // String Rules
        rules.add(StringRules.trailingSpaceRule());
        rules.add(StringRules.upperCaseRule());
        rules.add(StringRules.lowerCaseRule());
        rules.add(StringRules.properCaseRule());
        rules.add(StringRules.replaceRule());

        ReflectionTestUtils.setField(simpleSuggestionEnginengine, "rules", rules);
    }

    @Test
    public void shouldSuggest() {
        Assert.assertThat(simpleSuggestionEnginengine.suggest(new DataSet()).size(), is(0));
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
        final Stream<Suggestion> suggestions = simpleSuggestionEnginengine.score(actions.stream(), columnMetadata);

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

        ReflectionTestUtils.setField(transformationService, "actionRegistry", actionRegistry);
        ReflectionTestUtils.setField(transformationService, "suggestionEngine", simpleSuggestionEnginengine);

        List<String> oldActionFormList = oldWayToSuggest(columnMetadata);

        String[] expectedResult = { "clear_invalid", "delete_invalid", "fillinvalidwithdefault", "delete_empty",
                "fillemptywithdefault" };

        // when
        String[] newActionFormArray = transformationService //
                .suggest(columnMetadata, 5) //
                .map(ActionForm::getName) //
                .toArray(String[]::new);

        // then
        Assert.assertThat(oldActionFormList, IsIterableContainingInOrder.contains(newActionFormArray));

        // then
        Assert.assertEquals(5, newActionFormArray.length);
        Assert.assertThat(oldActionFormList, IsIterableContainingInOrder.contains(newActionFormArray));
        Assert.assertArrayEquals(expectedResult, newActionFormArray);
    }

    @Test
    public void shouldTestSuggestColumnString() throws IOException {

        // given
        final String json = IOUtils.toString(this.getClass().getResourceAsStream("metadata_gonfleurs_suggestion.json"), UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        final ColumnMetadata columnMetadata = mapper.readValue(json, ColumnMetadata.class);

        ReflectionTestUtils.setField(transformationService, "actionRegistry", actionRegistry);
        ReflectionTestUtils.setField(transformationService, "suggestionEngine", simpleSuggestionEnginengine);

        List<String> oldActionFormList = oldWayToSuggest(columnMetadata);
        String[] expectedResult = { "lowercase", "propercase", "replace_on_value", "uppercase" };

        // when
        String[] newActionFormArray = transformationService //
                .suggest(columnMetadata, 5) //
                .map(ActionForm::getName) //
                .toArray(String[]::new);

        // then
        Assert.assertEquals(4, newActionFormArray.length);
        Assert.assertThat(oldActionFormList, IsIterableContainingInOrder.contains(newActionFormArray));
        Assert.assertArrayEquals(expectedResult, newActionFormArray);
    }

    private List<String> oldWayToSuggest(ColumnMetadata columnMetadata) {
        // old way to compute the list
        // look for all actions applicable to the column type
        final Stream<Suggestion> suggestions = simpleSuggestionEnginengine
                .score(actionRegistry.findAll().filter(am -> am.acceptField(columnMetadata)), columnMetadata);

        return suggestions //
                .filter(s -> s.getScore() > 0) // Keep only strictly positive score (negative and 0 indicates not applicable)
                .limit(5) //
                .map(Suggestion::getAction) // Get the action for positive suggestions
                .map(am -> am.adapt(columnMetadata)) // Adapt default values (e.g. column name)
                .map(ad -> ad.getActionForm(getLocale())) //
                .map(ActionForm::getName).collect(Collectors.toList());
    }

}
