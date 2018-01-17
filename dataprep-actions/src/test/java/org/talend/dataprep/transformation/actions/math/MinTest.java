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

package org.talend.dataprep.transformation.actions.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the Min action.
 *
 * @see Min
 */
public class MinTest extends AbstractMetadataBaseTest<Min> {

    /** The action parameters. */
    private Map<String, String> parameters;

    public MinTest() {
        super(new Min());
    }

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = MinTest.class.getResourceAsStream( "minAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }


    @Test
    public void test_apply_inplace() throws Exception {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "7");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(3, row.getRowMetadata().size());
        assertEquals("5.0", row.get("0000"));
    }


    @Test
    public void test_apply_in_newcolumn() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "7");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals( "5.0", row.get( "0003" ));
    }

    @Test
    public void min_with_constant_percentage() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "70%");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals( "0.7", row.get( "0003" ));
    }

    @Test
    public void min_with_undefined_constant() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals( StringUtils.EMPTY, row.get( "0003" ));
    }

    @Test
    public void min_with_NaN_constant() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "beer");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals( StringUtils.EMPTY, row.get( "0003" ));
    }

    @Test
    public void min_with_other_column() {
        // given
        DataSetRow row = getRow("5", "1", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals( "1.0", row.get( "0003" ));
    }

    @Test
    public void min_with_undefined_other_column() {
        // given
        DataSetRow row = getRow("5", "8", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "000xx");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals( StringUtils.EMPTY, row.get( "0003" ));
    }

    @Test
    public void min_with_NaN_other_column() {
        // given
        DataSetRow row = getRow("5", "8", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0002");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals( StringUtils.EMPTY, row.get( "0003" ));
    }

    @Test
    public void min_currency_value_with_constant() {
        // given
        DataSetRow row = getRow("$5", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "7");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals( "5.0", row.get( "0003" ));
    }

    @Test
    public void min_currency_value_with_other_column() {
        // given
        DataSetRow row = getRow("5", "$1", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals( "1.0", row.get( "0003" ));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

    private void assertColumnWithResultCreated(DataSetRow row) {
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_min").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }
}
