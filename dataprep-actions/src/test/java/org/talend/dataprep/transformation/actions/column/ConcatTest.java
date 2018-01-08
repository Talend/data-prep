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
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
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
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the Concat action.
 *
 * @see Concat
 */
public class ConcatTest extends AbstractMetadataBaseTest<Concat> {

    /** The action parameters. */
    private Map<String, String> parameters;

    public ConcatTest() {
        super(new Concat());
    }

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = ConcatTest.class.getResourceAsStream("concatAction.json");
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
    public void testParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        assertThat(parameters.size(), is(8));

        // Test on items label for TDP-2943:
        final SelectParameter selectParameter = (SelectParameter) parameters.get(6);
        assertEquals("Other column", selectParameter.getItems().get(0).getLabel());
        assertEquals("Value", selectParameter.getItems().get(1).getLabel());
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "<first-second>");
        assertEquals(expected, row);
    }

    @Test
    public void test_apply_inplace() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("<first-second>", "second", "Done !");
        assertEquals(expected, row);
    }

    @Test
    public void should_set_new_column_name() {
        // given
        final DataSetRow row = builder() //
                .with(value("first").type(Type.STRING).name("source")) //
                .with(value("second").type(Type.STRING).name("selected")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("source_selected").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void should_set_new_column_name_without_other_column() {
        // given
        final DataSetRow row = builder() //
                .with(value("first").type(Type.STRING).name("source")) //
                .with(value("second").type(Type.STRING).name("selected")) //
                .with(value("Done !").type(Type.STRING)) //
                .build();
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        parameters.put(Concat.MODE_PARAMETER, Concat.CONSTANT_MODE);
        parameters.remove(Concat.SELECTED_COLUMN_PARAMETER);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("<source>").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void should_apply_without_separator() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.SEPARATOR_PARAMETER);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "<firstsecond>");
        assertEquals(expected, row);
    }

    @Test
    public void should_not_apply_without_first_value() {
        // given
        DataSetRow row = getRow("", "second", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", ""), //
                MapEntry.entry("0001", "second"), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<second>"));
    }

    @Test
    public void should_apply_without_first_value_and_both_not_empty() {
        // given
        DataSetRow row = getRow("", "second", "Done !");
        parameters.put(Concat.SEPARATOR_CONDITION, Concat.ALWAYS);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", ""), //
                MapEntry.entry("0001", "second"), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<-second>"));
    }

    @Test
    public void should_not_apply_with_first_value_blank() {
        // given
        DataSetRow row = getRow(" ", "second", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", " "), //
                MapEntry.entry("0001", "second"), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<second>"));
    }

    @Test
    public void should_apply_with_first_value_blank_and_always() {
        // given
        DataSetRow row = getRow(" ", "second", "Done !");
        parameters.put(Concat.SEPARATOR_CONDITION, Concat.ALWAYS);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", " "), //
                MapEntry.entry("0001", "second"), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<-second>"));
    }

    @Test
    public void should_apply_without_first_value_blank_and_both_not_empty() {
        // given
        DataSetRow row = getRow(" ", "second", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", " "), //
                MapEntry.entry("0001", "second"), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<second>"));
    }

    @Test
    public void should_apply_without_second_value_and_always() {
        // given
        DataSetRow row = getRow("first", "", "Done !");
        parameters.put(Concat.SEPARATOR_CONDITION, Concat.ALWAYS);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", "first"), //
                MapEntry.entry("0001", ""), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<first->"));
    }

    @Test
    public void should_apply_without_second_value() {
        // given
        DataSetRow row = getRow("first", "", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", "first"), //
                MapEntry.entry("0001", ""), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<first>"));
    }

    @Test
    public void should_apply_without_second_value_and_both_not_empty() {
        // given
        DataSetRow row = getRow("first", "", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", "first"), //
                MapEntry.entry("0001", ""), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<first>"));
    }

    @Test
    public void should_not_apply_with_blank_second_value() {
        // given
        DataSetRow row = getRow("first", "  ", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", "first"), //
                MapEntry.entry("0001", "  "), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<first>"));
    }

    @Test
    public void should_apply_with_blank_second_value_and_always() {
        // given
        DataSetRow row = getRow("first", "  ", "Done !");
        parameters.put(Concat.SEPARATOR_CONDITION, Concat.ALWAYS);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", "first"), //
                MapEntry.entry("0001", "  "), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<first->"));
    }

    @Test
    public void should_apply_with_both_blank_and_always() {
        // given
        DataSetRow row = getRow(" ", " ", "Done !");
        parameters.put(Concat.SEPARATOR_CONDITION, Concat.ALWAYS);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", " "), //
                MapEntry.entry("0001", " "), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<->"));
    }

    @Test
    public void should_apply_with_both_empty_and_always() {
        // given
        DataSetRow row = getRow("", "", "Done !");
        parameters.put(Concat.SEPARATOR_CONDITION, Concat.ALWAYS);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", ""), //
                MapEntry.entry("0001", ""), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<->"));
    }

    @Test
    public void should_apply_with_blank_second_value_and_both_not_empty() {
        // given
        DataSetRow row = getRow("first", "  ", "Done !");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()).contains(MapEntry.entry("0000", "first"), //
                MapEntry.entry("0001", "  "), //
                MapEntry.entry("0002", "Done !"), //
                MapEntry.entry("0003", "<first>"));
    }

    @Test
    public void should_apply_without_prefix() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.PREFIX_PARAMETER);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "first-second>");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_without_other_column() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(Concat.MODE_PARAMETER, Concat.CONSTANT_MODE);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "<first>");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_without_suffix() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.SUFFIX_PARAMETER);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "<first-second");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_without_any_parameters() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.PREFIX_PARAMETER);
        parameters.remove(Concat.SEPARATOR_PARAMETER);
        parameters.remove(Concat.SUFFIX_PARAMETER);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "firstsecond");
        assertEquals(expected, row);
    }

    @Test
    public void should_not_apply_because_missing_selected_parameter() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.SELECTED_COLUMN_PARAMETER);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(row.get("0000"), "first");
        assertEquals(row.get("0001"), "second");
        assertEquals(row.get("0002"), "Done !");
    }

    @Test
    public void should_not_apply_because_selected_column_not_found() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(Concat.SELECTED_COLUMN_PARAMETER, "123548");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(row.get("0000"), "first");
        assertEquals(row.get("0001"), "second");
        assertEquals(row.get("0002"), "Done !");
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.DATE)));
        assertTrue(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

}
