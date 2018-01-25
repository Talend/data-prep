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

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;
import static org.talend.dataprep.transformation.actions.math.ChangeNumberFormat.*;

/**
 * Unit test for the ChangeNumberFormat action.
 *
 * @see ChangeNumberFormat
 */
public class ChangeNumberFormatTest extends AbstractMetadataBaseTest<ChangeNumberFormat> {

    private Map<String, String> parameters;

    public ChangeNumberFormatTest() {
        super(new ChangeNumberFormat());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("changeNumberFormatAction.json"));
    }

    @Test
    public void testName() throws Exception {
        assertEquals("change_number_format", action.getName());
    }

    @Test
    public void testParameters() throws Exception {
        assertThat(action.getParameters(Locale.US).size(), is(7));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void shouldReturnBehaviour() throws Exception {
        final Set<Behavior> actual = action.getBehavior();
        assertEquals(1, actual.size());
        assertTrue(actual.contains(Behavior.VALUES_COLUMN));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.NUMBERS.getDisplayName(Locale.US)));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

    @Test
    public void test_apply_in_newcolumn() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "0012.50", "tata");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "0012.50", "tata", "12.5");
        assertEquals(expectedRow.values(), row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0001_formatted").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_in_newcolumn_with_empty_value() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "", "tata");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "", "tata", "");
        assertEquals(expectedRow.values(), row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0001_formatted").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "0012.50", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "12.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_percentage() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "12.50%", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "0.12", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_EU() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "0012,50", "tata");
        parameters.put(FROM_SEPARATORS, EU_SEPARATORS);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "12.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_seps() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1_012/50", "tata");
        parameters.put(FROM_SEPARATORS, CUSTOM);
        parameters.put(FROM + DECIMAL + SEPARATOR, "/");
        parameters.put(FROM + GROUPING + SEPARATOR, "_");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1,012.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_EU_group_1() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1 012,50", "tata");
        parameters.put(FROM_SEPARATORS, EU_SEPARATORS);
        parameters.put(TARGET_PATTERN, EU_PATTERN);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1 012,5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_EU_to_SC() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1 012,50", "tata");
        parameters.put(FROM_SEPARATORS, EU_SEPARATORS);
        parameters.put(TARGET_PATTERN, SCIENTIFIC);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1.012E3", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_EU_group_2() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1.012,50", "tata");
        parameters.put(FROM_SEPARATORS, EU_SEPARATORS);
        parameters.put(TARGET_PATTERN, EU_PATTERN);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1 012,5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_2354() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "993,886", "tata");
        parameters.put(FROM_SEPARATORS, US_SEPARATORS);
        parameters.put(TARGET_PATTERN, EU_PATTERN);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "993 886", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_from_CH() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1'012.50", "tata");
        parameters.put(TARGET_PATTERN, US_PATTERN);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1,012.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_do_nothing_from_wrong_pattern() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1'012.50", "tata");
        parameters.put(TARGET_PATTERN, "wrong_pattern");

        // when --> IllegalArgumentException
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1'012.50", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_do_nothing_from_wrong_custom_separator() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1'012.50", "tata");
        parameters.put(FROM_SEPARATORS, CUSTOM);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1'012.50", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_from_CH_2() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1'012.50", "tata");
        parameters.put(FROM_SEPARATORS, CH_SEPARATORS);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1,012.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_to_CH() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1.012,50", "tata");
        parameters.put(FROM_SEPARATORS, EU_SEPARATORS);
        parameters.put(TARGET_PATTERN, CH_PATTERN);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1'012.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_SCIENTIFIC() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1.23E+3", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1,230", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_not_a_number() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "tagada", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "tagada", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_pattern() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "0012", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#.000");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "12.000", "tata");
        assertEquals(expectedRow.values(), row.values());

    }

    @Test
    public void should_process_row_alt_pattern_percentage() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "0.12", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#%");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "12%", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_pattern_alt_seps() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1012.45", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#,##0.000");
        parameters.put(TARGET + DECIMAL + SEPARATOR, "/");
        parameters.put(TARGET + GROUPING + SEPARATOR, "_");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1_012/450", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_pattern_same_seps() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1012.45", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#,##0.000");
        parameters.put(TARGET + DECIMAL + SEPARATOR, ".");
        parameters.put(TARGET + GROUPING + SEPARATOR, ".");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1012.450", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_pattern_alt_seps_empty() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1012.45", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#,##0.000");
        parameters.put(TARGET + DECIMAL + SEPARATOR, "/");
        parameters.put(TARGET + GROUPING + SEPARATOR, "");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1012/450", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_alt_pattern_alt_seps_empty_custom() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "1012.45", "tata");
        parameters.put(TARGET_PATTERN, CUSTOM);
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "#,##0.000");
        parameters.put(TARGET + DECIMAL + SEPARATOR, "/");
        parameters.put(TARGET + GROUPING + SEPARATOR, CUSTOM);
        parameters.put(TARGET + GROUPING + SEPARATOR + "_" + CUSTOM, "");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "1012/450", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_1108_invalid_pattern() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "012.50", "tata");
        parameters.put(TARGET_PATTERN, "custom");
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "# ##0,#");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "012.50", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void test_TDP_1108_empty_pattern() throws Exception {
        // given
        final DataSetRow row = getRow("toto", "012.50", "tata");
        parameters.put(TARGET_PATTERN, "custom");
        parameters.put(TARGET_PATTERN + "_" + CUSTOM, "");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final DataSetRow expectedRow = getRow("toto", "12.5", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    @Test
    public void should_process_row_when_value_is_empty() throws Exception {
        // given
        DataSetRow row = getRow("toto", "", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then (values should be unchanged)
        final DataSetRow expectedRow = getRow("toto", "", "tata");
        assertEquals(expectedRow.values(), row.values());
    }

    /**
     * Format number does not work if there is a null or an invalid in the column.
     * Values after a null or an invalid value (which is not a number) were not formatted by the transformation.
     * Have look at https://jira.talendforge.org/browse/TDP-1361
     */
    @Test
    public void TDP_1361() throws Exception {
        // given
        final DataSetRow row1 = getRow("toto", "1.5E-11", "tata");
        final DataSetRow row2 = getRow("titi", "azerty", "tata");
        final DataSetRow row3 = getRow("tutu", "010", "tata");
        Collection<DataSetRow> rows = Arrays.asList(row1, row2, row3);
        parameters.put(FROM_SEPARATORS, US_SEPARATORS);
        parameters.put(TARGET_PATTERN, SCIENTIFIC);

        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then (values should now be compliant with scientific notation)
        final DataSetRow expectedRow1 = getRow("toto", "1.5E-11", "tata");
        final DataSetRow expectedRow2 = getRow("titi", "azerty", "tata");
        final DataSetRow expectedRow3 = getRow("tutu", "1E1", "tata");
        assertEquals(expectedRow1.values(), row1.values());
        assertEquals(expectedRow2.values(), row2.values());
        assertEquals(expectedRow3.values(), row3.values());
    }

}
