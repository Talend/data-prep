package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.fill.FillIfEmpty;
import org.talend.dataprep.transformation.api.action.metadata.text.ProperCase;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

public class FillEmptyRuleTest {

    private SuggestionEngineRule fillEmptyRule;

    private ColumnMetadata noEmptyColumn = column().type(Type.STRING).build();

    private ColumnMetadata mostEmptyColumn = column().type(Type.STRING).build();

    @Before
    public void setUp() throws Exception {
        fillEmptyRule = EmptyRules.fillEmptyRule();
        noEmptyColumn.getStatistics().setEmpty(0);
        mostEmptyColumn.getStatistics().setEmpty(10);
    }

    @Test
    public void testOtherAction() throws Exception {
        assertThat(fillEmptyRule.apply(new ProperCase(), noEmptyColumn), is(NON_APPLICABLE));
        assertThat(fillEmptyRule.apply(new ProperCase(), mostEmptyColumn), is(NON_APPLICABLE));
    }

    @Test
    public void testNegativeMatch() throws Exception {
        assertThat(fillEmptyRule.apply(new FillIfEmpty(), noEmptyColumn), is(NEGATIVE));
    }

    @Test
    public void testPositiveMatch() throws Exception {
        assertThat(fillEmptyRule.apply(new FillIfEmpty(), mostEmptyColumn), is(EMPTY_MGT));
    }

}
