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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the SimpleSuggestionEngine
 *
 * @see SimpleSuggestionEngine
 */
public class SimpleSuggestionEngineTest {

    /** The suggestion engine to test. */
    private SimpleSuggestionEngine engine;

    /**
     * Default constructor.
     */
    public SimpleSuggestionEngineTest() {
        engine = new SimpleSuggestionEngine();

        // ReflectionUtils to save the use of a spring context
        List<SuggestionEngineRule> rules = new ArrayList<>();
        final InvalidRules invalidRules = new InvalidRules();
        rules.add(invalidRules.deleteInvalidRule());
        rules.add(invalidRules.fillInvalidRule());
        rules.add(EmptyRules.deleteEmptyRule());
        rules.add(EmptyRules.fillEmptyRule());
        rules.add(IntegerRules.absoluteRule());
        rules.add(IntegerRules.integerRule());
        ReflectionTestUtils.setField(engine, "rules", rules);
    }

    @Test
    public void shouldSuggest() {
        Assert.assertThat(engine.suggest(new DataSet()).size(), is(0));
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
        final Stream<Suggestion> suggestions = engine.score(actions.stream(), columnMetadata);

        int currentScore = Integer.MAX_VALUE;
        for (Suggestion suggestion : suggestions.collect(Collectors.toList())) {
            assertTrue(currentScore >= suggestion.getScore());
            currentScore = suggestion.getScore();
        }
    }

}
