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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.math.RemoveFractionalPart;
import org.talend.dataprep.transformation.actions.math.RoundCeil;
import org.talend.dataprep.transformation.actions.math.RoundFloor;
import org.talend.dataprep.transformation.actions.math.RoundHalfUp;
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
        assertThat(integerRule.apply(new RoundCeil(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new RoundHalfUp(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new RoundFloor(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new RemoveFractionalPart(), stringColumn), is(NON_APPLICABLE));
    }

    @Test
    public void testNegativeMatch() throws Exception {
        assertThat(integerRule.apply(new RemoveFractionalPart(), allIntColumn), is(NEGATIVE));
        assertThat(integerRule.apply(new RoundHalfUp(), allIntColumn), is(NEGATIVE));
    }

    @Test
    public void testPositiveMatch() throws Exception {
        assertThat(integerRule.apply(new RemoveFractionalPart(), mostIntColumn), is(LOW));
        assertThat(integerRule.apply(new RoundHalfUp(), mostIntColumn), is(LOW));
    }

    @Test
    public void testIgnoreRule() throws Exception {
        assertThat(integerRule.apply(new RoundCeil(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new RoundHalfUp(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new RoundFloor(), stringColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new RemoveFractionalPart(), stringColumn), is(NON_APPLICABLE));

        assertThat(integerRule.apply(new RoundCeil(), mostIntColumn), is(NON_APPLICABLE));
        assertThat(integerRule.apply(new RoundFloor(), mostIntColumn), is(NON_APPLICABLE));
    }
}