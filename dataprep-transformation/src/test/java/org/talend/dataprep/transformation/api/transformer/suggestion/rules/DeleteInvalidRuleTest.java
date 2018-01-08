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
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.delete.DeleteInvalid;
import org.talend.dataprep.transformation.actions.text.ProperCase;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

public class DeleteInvalidRuleTest {

    private SuggestionEngineRule deleteInvalidRule;

    private ColumnMetadata allValidColumn = column().type(Type.STRING).build();

    private ColumnMetadata mostValidColumn = column().type(Type.STRING).build();

    @Before
    public void setUp() throws Exception {
        deleteInvalidRule = new InvalidRules().deleteInvalidRule();
        allValidColumn.getStatistics().setInvalid(0);
        mostValidColumn.getStatistics().setInvalid(10);
    }

    @Test
    public void testOtherAction() throws Exception {
        assertThat(deleteInvalidRule.apply(new ProperCase(), allValidColumn), is(NON_APPLICABLE));
        assertThat(deleteInvalidRule.apply(new ProperCase(), mostValidColumn), is(NON_APPLICABLE));
    }

    @Test
    public void testNegativeMatch() throws Exception {
        assertThat(deleteInvalidRule.apply(new DeleteInvalid(), allValidColumn), is(NEGATIVE));
    }

    @Test
    public void testPositiveMatch() throws Exception {
        assertThat(deleteInvalidRule.apply(new DeleteInvalid(), mostValidColumn), is(INVALID_MGT));
    }

}
