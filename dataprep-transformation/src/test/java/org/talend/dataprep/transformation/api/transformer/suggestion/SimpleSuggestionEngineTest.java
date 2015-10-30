package org.talend.dataprep.transformation.api.transformer.suggestion;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.delete.DeleteEmpty;
import org.talend.dataprep.transformation.api.action.metadata.delete.DeleteInvalid;
import org.talend.dataprep.transformation.api.action.metadata.fillempty.FillIfEmpty;
import org.talend.dataprep.transformation.api.action.metadata.fillinvalid.FillInvalid;
import org.talend.dataprep.transformation.api.action.metadata.math.Absolute;
import org.talend.dataprep.transformation.api.action.metadata.text.UpperCase;
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
        rules.add(new InvalidRules().deleteInvalidRule());
        rules.add(new InvalidRules().fillInvalidRule());
        rules.add(new EmptyRules().deleteEmptyRule());
        rules.add(new EmptyRules().fillEmptyRule());
        rules.add(new IntegerRules().absoluteRule());
        rules.add(new IntegerRules().integerRule());
        ReflectionTestUtils.setField(engine, "rules", rules);
    }

    @Test
    public void shouldSuggest() {
        Assert.assertThat(engine.suggest(new DataSet()).size(), is(0));
    }

    @Test
    public void shouldSuggestionsShouldBeSorted() throws IOException {

        final String json = IOUtils.toString(this.getClass().getResourceAsStream("sample_column.json"));
        ObjectMapper mapper = new ObjectMapper();
        final ColumnMetadata columnMetadata = mapper.readValue(json, ColumnMetadata.class);

        List<ActionMetadata> actions = new ArrayList<>();
        actions.add(new FillIfEmpty());
        actions.add(new FillInvalid());
        actions.add(new DeleteInvalid());
        actions.add(new DeleteEmpty());
        actions.add(new Absolute());
        actions.add(new UpperCase());
        final List<Suggestion> suggestions = engine.score(actions, columnMetadata);

        int currentScore = Integer.MAX_VALUE;
        for (Suggestion suggestion : suggestions) {
            assertTrue(currentScore >= suggestion.getScore());
            currentScore = suggestion.getScore();
        }
    }

}