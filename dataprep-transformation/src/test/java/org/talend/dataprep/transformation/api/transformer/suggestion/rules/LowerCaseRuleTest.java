// ============================================================================
//
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
import org.talend.dataprep.transformation.actions.text.LowerCase;
import org.talend.dataprep.transformation.actions.text.ProperCase;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

public class LowerCaseRuleTest {

    private SuggestionEngineRule lowerCaseRule;

    private ColumnMetadata intColumn = column().type(Type.INTEGER).build();

    private ColumnMetadata stringColumn = column().type(Type.STRING).build();

    private ColumnMetadata stringLowerCaseColumn = column().type(Type.STRING).build();

    @Before
    public void setUp() throws Exception {
        lowerCaseRule = StringRules.lowerCaseRule();
        stringColumn.getStatistics().getPatternFrequencies().add(new PatternFrequency("Aaaaa", 10));
        stringLowerCaseColumn.getStatistics().getPatternFrequencies().add(new PatternFrequency("aaaa", 10));
    }

    @Test
    public void testOtherAction() throws Exception {
        assertThat(lowerCaseRule.apply(new ProperCase(), stringLowerCaseColumn), is(NON_APPLICABLE));
    }

    @Test
    public void testNegativeMatch() throws Exception {
        assertThat(lowerCaseRule.apply(new LowerCase(), stringLowerCaseColumn), is(NEGATIVE));
    }

    @Test
    public void testPositiveMatch() throws Exception {
        assertThat(lowerCaseRule.apply(new LowerCase(), stringColumn), is(LOW));
    }

    @Test
    public void testIgnoreRule() throws Exception {
        assertThat(lowerCaseRule.apply(new LowerCase(), intColumn), is(NON_APPLICABLE));
    }

}
