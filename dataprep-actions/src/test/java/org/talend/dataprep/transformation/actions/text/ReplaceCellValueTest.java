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

package org.talend.dataprep.transformation.actions.text;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.FORBID_DISTRIBUTED;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.VALUES_COLUMN;
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.*;
import static org.talend.dataprep.transformation.actions.text.ReplaceCellValue.NEW_VALUE_PARAMETER;
import static org.talend.dataprep.transformation.actions.text.ReplaceCellValue.ORIGINAL_VALUE_PARAMETER;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.FlagNames;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

/**
 * Unit test for the ReplaceCellValue class.
 *
 * @see ReplaceCellValue
 */
public class ReplaceCellValueTest extends AbstractMetadataBaseTest<ReplaceCellValue> {

    public ReplaceCellValueTest() {
        super(new ReplaceCellValue());
    }

    @Test
    public void test_action_name() throws Exception {
        assertEquals("replace_cell_value", action.getName());
    }

    @Test
    public void test_category() throws Exception {
        assertEquals("strings", action.getCategory(Locale.US));
    }

    @Test
    public void test_parameters() {
        // when
        final List<Parameter> actionParams = action.getParameters(Locale.US);

        // then
        assertThat(actionParams, hasSize(7));

        final List<String> paramNames = actionParams.stream().map(Parameter::getName).collect(toList());
        assertThat(paramNames, IsIterableContainingInAnyOrder.containsInAnyOrder( //
                ActionsUtils.CREATE_NEW_COLUMN,
                COLUMN_ID.getKey(), //
                SCOPE.getKey(), //
                ROW_ID.getKey(), //
                ORIGINAL_VALUE_PARAMETER, //
                FILTER.getKey(), //
                NEW_VALUE_PARAMETER) //
        );
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void should_not_compile_no_replacement_value() throws Exception {

        // given
        ActionContext context = getActionContext(new SimpleEntry<>(ROW_ID.getKey(), "2"));

        // when
        action.compile(context);

        // then
        assertEquals(CANCELED, context.getActionStatus());
    }

    @SafeVarargs
    private final ActionContext getActionContext(SimpleEntry<String, String>... entries) {
        Map<String, String> parameters = new HashMap<>();
        for (SimpleEntry<String, String> entry : entries) {
            parameters.put(entry.getKey(), entry.getValue());
        }
        ActionContext context = new ActionContext(new TransformationContext());
        context.setParameters(parameters);
        return context;
    }

    @Test
    public void should_not_compile_no_row_value() throws Exception {

        // given
        ActionContext context = getActionContext(new SimpleEntry<>(NEW_VALUE_PARAMETER, "toto"));

        // when
        action.compile(context);

        // then
        assertEquals(CANCELED, context.getActionStatus());
    }

    @Test
    public void should_not_compile_invalid_row_value() throws Exception {

        // given
        ActionContext context = getActionContext( //
                new SimpleEntry<>(NEW_VALUE_PARAMETER, "toto"), //
                new SimpleEntry<>(ROW_ID.getKey(), "toto") //
        );

        // when
        action.compile(context);

        // then
        assertEquals(CANCELED, context.getActionStatus());
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Long rowId = 1L;
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Joe");
        final DataSetRow row = new DataSetRow(values);
        row.setTdpId(rowId);
        DataSetRow expectedRow = getRow("Joe", "Jimmy");
        expectedRow.setTdpId(rowId);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ORIGINAL_VALUE_PARAMETER, "Joe");
        parameters.put(NEW_VALUE_PARAMETER, "Jimmy");
        parameters.put(SCOPE.getKey().toLowerCase(), "cell");
        parameters.put(COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(ROW_ID.getKey(), String.valueOf(rowId));
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedRow, row);
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(1).name("0000_replace").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0001");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() {

        // given
        final Long rowId = 1L;
        final String joe = "Joe";
        final DataSetRow row = getRow(joe);
        row.setTdpId(rowId);

        final Map<String, String> parameters = getParameters(rowId, joe, "Jimmy");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get("0000"), is("Jimmy"));
    }

    @Test
    public void should_not_replace_value_not_the_target_row() {

        // given
        final Long rowId = 1L;
        final String joe = "Joe";
        final DataSetRow row = getRow(joe);
        row.setTdpId(2L);

        final Map<String, String> parameters = getParameters(rowId, "Jimmy", joe);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get("0000"), is(joe));
    }

    @Test
    public void should_tag_invalid_value() {
        // given
        final DataSetRow row = getRow("True");
        row.setTdpId(1L);
        final ColumnMetadata columnMetadata = row.getRowMetadata().getColumns().get(0);
        columnMetadata.setType(Type.BOOLEAN.getName()); // Column is a boolean
        columnMetadata.setTypeForced(true);
        final Map<String, String> parameters = getParameters(1L, "True", "NotABoolean");

        // when
        final AnalyzerService analyzerService = new AnalyzerService();
        ActionTestWorkbench.test(Collections.singleton(row), analyzerService, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get("0000"), is("NotABoolean"));
        assertThat(row.getInternalValues().get(FlagNames.TDP_INVALID), is(",0000"));
    }

    @Test
    public void should_accept_string_column() {
        // given
        final ColumnMetadata column = ActionMetadataTestUtils.getColumn(STRING);
        // when then
        assertTrue(action.acceptField(column));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(FORBID_DISTRIBUTED));
        assertTrue(action.getBehavior().contains(VALUES_COLUMN));
    }

    private Map<String, String> getParameters(Long rowId, String originalValue, String replacement) {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ORIGINAL_VALUE_PARAMETER, originalValue);
        parameters.put(NEW_VALUE_PARAMETER, replacement);
        parameters.put(SCOPE.getKey().toLowerCase(), "cell");
        parameters.put(COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(ROW_ID.getKey(), String.valueOf(rowId));
        return parameters;
    }
}
