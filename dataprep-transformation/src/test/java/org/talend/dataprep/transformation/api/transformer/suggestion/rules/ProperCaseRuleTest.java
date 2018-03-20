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
import org.talend.dataprep.transformation.actions.text.LowerCase;
import org.talend.dataprep.transformation.actions.text.ProperCase;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

public class ProperCaseRuleTest {

    private SuggestionEngineRule trailingSpaceRule;

    private ColumnMetadata intColumn = column().type(Type.INTEGER).build();

    private ColumnMetadata stringColumn = column().type(Type.STRING).build();

    private ColumnMetadata stringWithProperCaseColumn = column().type(Type.STRING).build();

    @Before
    public void setUp() throws Exception {
        trailingSpaceRule = StringRules.properCaseRule();
        stringColumn.getStatistics().getPatternFrequencies().add(new PatternFrequency("AaaA", 10));
        stringWithProperCaseColumn.getStatistics().getPatternFrequencies().add(new PatternFrequency(" Aaa Aa A", 10));
    }

    @Test
    public void testOtherAction() throws Exception {
        assertThat(trailingSpaceRule.apply(new LowerCase(), stringColumn), is(NON_APPLICABLE));
    }

    @Test
    public void testNegativeMatch() throws Exception {
        assertThat(trailingSpaceRule.apply(new ProperCase(), stringWithProperCaseColumn), is(NEGATIVE));
    }

    @Test
    public void testPositiveMatch() throws Exception {
        assertThat(trailingSpaceRule.apply(new ProperCase(), stringColumn), is(LOW));
    }

    @Test
    public void testIgnoreRule() throws Exception {
        assertThat(trailingSpaceRule.apply(new ProperCase(), intColumn), is(NON_APPLICABLE));
    }

}
