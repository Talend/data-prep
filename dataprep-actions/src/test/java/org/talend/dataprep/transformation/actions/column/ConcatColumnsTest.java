package org.talend.dataprep.transformation.actions.column;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

public class ConcatColumnsTest extends AbstractMetadataBaseTest<ConcatColumns> {

    /** The action parameters. */
    private Map<String, String> parameters;

    public ConcatColumnsTest() {
        super(new ConcatColumns());
    }

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = ConcatColumnsTest.class.getResourceAsStream("concatColumnsAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Override
    protected CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.INVISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() {
        //nothing to do here, can't be apply in place
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "firstsecond");
        assertEquals(expected, row);
    }

}