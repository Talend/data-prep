package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.delete.DeleteEmpty;
import org.talend.dataprep.transformation.api.action.metadata.text.ProperCase;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

public class DeleteEmptyRuleTest {

    private SuggestionEngineRule deleteEmptyRule;

    private ColumnMetadata noEmptyColumn = column().type(Type.STRING).build();

    private ColumnMetadata mostEmptyColumn = column().type(Type.STRING).build();

    @Before
    public void setUp() throws Exception {
        deleteEmptyRule = EmptyRules.deleteEmptyRule();
        noEmptyColumn.getStatistics().setEmpty(0);
        mostEmptyColumn.getStatistics().setEmpty(10);
    }

    @Test
    public void testOtherAction() throws Exception {
        assertThat(deleteEmptyRule.apply(new ProperCase(), noEmptyColumn), is(NON_APPLICABLE));
        assertThat(deleteEmptyRule.apply(new ProperCase(), mostEmptyColumn), is(NON_APPLICABLE));
    }

    @Test
    public void testNegativeMatch() throws Exception {
        assertThat(deleteEmptyRule.apply(new DeleteEmpty(), noEmptyColumn), is(NEGATIVE));
    }

    @Test
    public void testPositiveMatch() throws Exception {
        assertThat(deleteEmptyRule.apply(new DeleteEmpty(), mostEmptyColumn), is(POSITIVE));
    }

}
