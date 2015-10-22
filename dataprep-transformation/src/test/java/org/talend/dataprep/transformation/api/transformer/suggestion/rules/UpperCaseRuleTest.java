package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.text.ProperCase;
import org.talend.dataprep.transformation.api.action.metadata.text.UpperCase;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

public class UpperCaseRuleTest {

    private SuggestionEngineRule upperCaseRule;

    private ColumnMetadata intColumn = column().type(Type.INTEGER).build();

    private ColumnMetadata stringColumn = column().type(Type.STRING).build();

    private ColumnMetadata stringUpperCaseColumn = column().type(Type.STRING).build();

    @Before
    public void setUp() throws Exception {
        upperCaseRule = StringRules.upperCaseRule();
        stringColumn.getStatistics().getPatternFrequencies().add(new PatternFrequency("aaaa", 10));
        stringUpperCaseColumn.getStatistics().getPatternFrequencies().add(new PatternFrequency("AAAA", 10));
    }

    @Test
    public void testOtherAction() throws Exception {
        assertThat(upperCaseRule.apply(new ProperCase(), stringColumn), is(NON_APPLICABLE));
    }

    @Test
    public void testNegativeMatch() throws Exception {
        assertThat(upperCaseRule.apply(new UpperCase(), stringUpperCaseColumn), is(NEGATIVE));
    }

    @Test
    public void testPositiveMatch() throws Exception {
        assertThat(upperCaseRule.apply(new UpperCase(), stringColumn), is(POSITIVE));
    }

    @Test
    public void testIgnoreRule() throws Exception {
        assertThat(upperCaseRule.apply(new UpperCase(), intColumn), is(NON_APPLICABLE));
    }

}
