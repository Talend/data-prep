// ============================================================================
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

package org.talend.dataprep.transformation.actions.math;

import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.CREATE_NEW_COLUMN;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the Pow action.
 *
 * @see ModuloTest
 */
public class ModuloTest extends AbstractMetadataBaseTest<Modulo> {

    /**
     * The action parameters.
     */
    private Map<String, String> parameters;

    public ModuloTest() {
        super(new Modulo());
    }

    @Override
    protected CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Before
    public void setUp() throws Exception {
        parameters = new HashMap<>();
        parameters.put("column_id", "0000");
        parameters.put("scope", "column");
        parameters.put("mode", "Another column");
        parameters.put("selected_column", "0001");
        parameters.put(CREATE_NEW_COLUMN, Boolean.TRUE.toString());
    }

    @Test
    public void testModulo() {
        assertEquals(new BigDecimal("1.2"), action.modulo(new BigDecimal("3.2"), new BigDecimal("2")));
        assertEquals(new BigDecimal("0.8"), action.modulo(new BigDecimal("3"), new BigDecimal("2.2")));
        assertEquals(new BigDecimal("0.8"), action.modulo(new BigDecimal("-3.2"), new BigDecimal("2")));
        assertEquals(new BigDecimal("-1.4"), action.modulo(new BigDecimal("3"), new BigDecimal("-2.2")));
        assertEquals(new BigDecimal("-1"), action.modulo(new BigDecimal("-3.2"), new BigDecimal("-2.2")));

        assertEquals(new BigDecimal("1"), action.modulo(new BigDecimal("3"), new BigDecimal("2")));
        assertEquals(new BigDecimal("1"), action.modulo(new BigDecimal("-3"), new BigDecimal("2")));
        assertEquals(new BigDecimal("-1"), action.modulo(new BigDecimal("3"), new BigDecimal("-2")));
        assertEquals(new BigDecimal("-1"), action.modulo(new BigDecimal("-3"), new BigDecimal("-2")));

        assertEquals(new BigDecimal("0"), action.modulo(new BigDecimal("10"), new BigDecimal("1")));
        assertEquals(new BigDecimal("0"), action.modulo(new BigDecimal("10"), new BigDecimal("-1")));
        assertEquals(new BigDecimal("0"), action.modulo(new BigDecimal("10"), new BigDecimal("1")));
        assertEquals(new BigDecimal("0"), action.modulo(new BigDecimal("10"), new BigDecimal("-1")));

        assertEquals(new BigDecimal("3"), action.modulo(new BigDecimal("10"), new BigDecimal("7")));
        assertEquals(new BigDecimal("-4"), action.modulo(new BigDecimal("10"), new BigDecimal("-7")));
        assertEquals(new BigDecimal("4"), action.modulo(new BigDecimal("-10"), new BigDecimal("7")));
        assertEquals(new BigDecimal("-3"), action.modulo(new BigDecimal("-10"), new BigDecimal("-7")));

        assertEquals(new BigDecimal("0"), action.modulo(new BigDecimal("10"), new BigDecimal("10")));
        assertEquals(new BigDecimal("0"), action.modulo(new BigDecimal("10"), new BigDecimal("-10")));
        assertEquals(new BigDecimal("0"), action.modulo(new BigDecimal("-10"), new BigDecimal("10")));
        assertEquals(new BigDecimal("0"), action.modulo(new BigDecimal("-10"), new BigDecimal("-10")));

        assertEquals(new BigDecimal("0.888888898090093"), action.modulo(new BigDecimal("-1.234567891011121"), new BigDecimal("2.12345678910121415")));
        assertEquals(new BigDecimal("0.123456789101112"), action.modulo(new BigDecimal("0.123456789101112"), new BigDecimal("2.12345678910121415")));
        assertEquals(new BigDecimal("-1.234567891011121"), action.modulo(new BigDecimal("-1.234567891011121"), new BigDecimal("-7.91012141512345678")));
        assertEquals(new BigDecimal("-5.070615265797878"), action.modulo(new BigDecimal("2.839506149325579"), new BigDecimal("-7.91012141512345678")));
    }

    @Override
    public void test_apply_inplace() throws Exception {
        // given
        DataSetRow row = getRow("6", "A", "Done !");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "5");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("1", row.get("0000"));
    }

    @Override
    public void test_apply_in_newcolumn() throws Exception {
        // given
        DataSetRow row = getRow("6", "A", "Done !");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "5");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("1", row.get("0003"));
    }

    @Test
    public void should_calc_mod() {
        // given
        DataSetRow row = getRow("6", "A", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "5");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("1", row.get("0003"));
    }

    @Test
    public void should_calc_mod_with_negative_value() {
        // given
        DataSetRow row = getRow("-7", "A", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "3");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("2", row.get("0003"));
    }

    @Test
    public void should_calc_mod_with_negative_value_and_parameter() {
        // given
        DataSetRow row = getRow("-6", "A", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "-5");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("-1", row.get("0003"));
    }

    @Test
    public void should_calc_mod_with_decimal_value() {
        // given
        final DataSetRow row = builder() //
                .with(value("6.5").type(Type.STRING).name("0000")) //
                .with(value("5").type(Type.STRING).name("0001")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("1.5", row.get("0003"));
    }

    @Test
    public void should_calc_mod_with_decimal_param() {
        // given
        final DataSetRow row = builder() //
                .with(value("6").type(Type.STRING).name("0000")) //
                .with(value("2.5").type(Type.STRING).name("0001")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("1", row.get("0003"));
    }

    @Test
    public void should_not_calc_mod_with_empty_divisor() {
        // given
        DataSetRow row = getRow("6", "B", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    @Test
    public void should_not_calc_mod_with_char() {
        // given
        DataSetRow row = getRow("6", "Z", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "aaaa");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    @Test
    public void should_not_calc_with_divisor_0() {
        // given
        DataSetRow row = getRow("6", "T", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "0");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    @Test
    public void should_not_calc_mod_with_alpha_other_column() {
        // given
        final DataSetRow row = builder() //
                .with(value("6.5").type(Type.STRING).name("0000")) //
                .with(value("a").type(Type.STRING).name("0001")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    @Test
    public void should_not_calc_without_divisor() {
        // given
        DataSetRow row = getRow("6", "U", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

    private void assertColumnWithResultCreated(DataSetRow row) {
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_mod").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }
}
