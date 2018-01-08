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

package org.talend.dataprep.transformation.actions.column;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.actions.date.BaseDateTest;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the Swap action.
 *
 * @see Swap
 */
public class SwapTest extends BaseDateTest<Swap> {

    public SwapTest() {
        super(new Swap());
    }

    /** The action parameters. */
    private Map<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = SwapTest.class.getResourceAsStream("swapAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.COLUMNS.getDisplayName(Locale.US)));
    }

    @Test
    public void swap_columns() throws Exception {
        // given
        List<DataSetRow> rows = Arrays.asList(getRow("5", "beer", "yup"), getRow("10", "wine", "cheese"));
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(rows.get(0).values()) //
                .contains(MapEntry.entry("0000", "beer"), //
                        MapEntry.entry("0001", "5"), //
                        MapEntry.entry("0002", "yup"));

        Assertions.assertThat(rows.get(1).values()) //
                .contains(MapEntry.entry("0000", "wine"), //
                        MapEntry.entry("0001", "10"), //
                        MapEntry.entry("0002", "cheese"));

        // FIXME how to test that ???
        /*
         * Assertions.assertThat(parameters.get( ImplicitParameters.OTHER_COLUMN_ID.getKey())) //
         * .isNotNull().isEqualTo("0001");
         */
    }

    @Test
    public void swap_columns_with_empty() throws Exception {
        // given
        List<DataSetRow> rows = Arrays.asList(getRow("5", "beer", "yup"), getRow("10", "", "cheese"));
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(rows.get(0).values()) //
                .contains(MapEntry.entry("0000", "beer"), //
                        MapEntry.entry("0001", "5"), //
                        MapEntry.entry("0002", "yup"));

        Assertions.assertThat(rows.get(1).values()) //
                .contains(MapEntry.entry("0000", ""), //
                        MapEntry.entry("0001", "10"), //
                        MapEntry.entry("0002", "cheese"));

    }

    @Test
    public void swap_columns_with_blank() throws Exception {
        // given
        List<DataSetRow> rows = Arrays.asList(getRow("5", "beer", "yup"), getRow("10", " ", "cheese"));
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(rows.get(0).values()) //
                .contains(MapEntry.entry("0000", "beer"), //
                        MapEntry.entry("0001", "5"), //
                        MapEntry.entry("0002", "yup"));

        Assertions.assertThat(rows.get(1).values()) //
                .contains(MapEntry.entry("0000", " "), //
                        MapEntry.entry("0001", "10"), //
                        MapEntry.entry("0002", "cheese"));

    }

    @Test
    public void swap_not_fail_unknown_target_column() throws Exception {
        // given
        List<DataSetRow> rows = Arrays.asList(getRow("5", "beer", "yup"), getRow("10", "wine", "cheese"));
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0009");

        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(rows.get(0).values()) //
                .contains(MapEntry.entry("0000", "5"), //
                        MapEntry.entry("0001", "beer"), //
                        MapEntry.entry("0002", "yup"));

        Assertions.assertThat(rows.get(1).values()) //
                .contains(MapEntry.entry("0000", "10"), //
                        MapEntry.entry("0001", "wine"), //
                        MapEntry.entry("0002", "cheese"));

    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(3, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_MULTIPLE_COLUMNS));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CHANGE_TYPE));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CHANGE_ROW));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // Nothing to test, this action is never applied in newcolumn
    }

    @Test
    public void test_apply_inplace() {
        // Nothing to test, this action is never applied in place
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.NA;
    }
}
