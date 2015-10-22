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
import org.talend.dataprep.transformation.api.action.metadata.math.Ceil;
import org.talend.dataprep.transformation.api.action.metadata.math.Floor;
import org.talend.dataprep.transformation.api.action.metadata.math.Round;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

public class IntegerRuleTest {

    private SuggestionEngineRule integerRule;

    private ColumnMetadata allIntColumn = column().type(Type.INTEGER).build();

    private ColumnMetadata mostIntColumn = column().type(Type.INTEGER).build();

    private ColumnMetadata stringColumn = column().type(Type.STRING).build();

    @Before
    public void setUp() throws Exception {
        integerRule = IntegerRules.integerRule();
        mostIntColumn.getStatistics().getPatternFrequencies().add(new PatternFrequency("9.9", 10));
    }

    @Test
    public void testOtherAction() throws Exception {
        assertThat(integerRule.apply(new Ceil(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new Round(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new Floor(), stringColumn), is(NON_APPLICABLE));
    }

    @Test
    public void testNegativeMatch() throws Exception {
        assertThat(integerRule.apply(new Ceil(), allIntColumn), is(NEGATIVE));
        assertThat(integerRule.apply(new Round(), allIntColumn), is(NEGATIVE));
        assertThat(integerRule.apply(new Floor(), allIntColumn), is(NEGATIVE));
    }

    @Test
    public void testPositiveMatch() throws Exception {
        assertThat(integerRule.apply(new Ceil(), mostIntColumn), is(POSITIVE));
        assertThat(integerRule.apply(new Round(), mostIntColumn), is(POSITIVE));
        assertThat(integerRule.apply(new Floor(), mostIntColumn), is(POSITIVE));
    }

    @Test
    public void testIgnoreRule() throws Exception {
        assertThat(integerRule.apply(new Ceil(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new Round(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new Floor(), stringColumn), is(NON_APPLICABLE));
    }
}