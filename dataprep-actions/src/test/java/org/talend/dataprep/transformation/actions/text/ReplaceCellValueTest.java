// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.*;
import static org.talend.dataprep.transformation.actions.text.ReplaceCellValue.NEW_VALUE_PARAMETER;
import static org.talend.dataprep.transformation.actions.text.ReplaceCellValue.ORIGINAL_VALUE_PARAMETER;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

/**
 * Unit test for the ReplaceCellValue class.
 *
 * @see ReplaceCellValue
 */
public class ReplaceCellValueTest extends AbstractMetadataBaseTest {

    private ReplaceCellValue action = new ReplaceCellValue();

    @Test
    public void test_action_name() throws Exception {
        assertEquals("replace_cell_value", action.getName());
    }

    @Test
    public void test_category() throws Exception {
        assertEquals("strings", action.getCategory());
    }

    @Test
    public void test_parameters() {
        // when
        final List<Parameter> actionParams = action.getParameters();

        // then
        assertThat(actionParams, hasSize(6));

        final List<String> paramNames = actionParams.stream().map(Parameter::getName).collect(toList());
        assertThat(paramNames,
                IsIterableContainingInAnyOrder.containsInAnyOrder( //
                        COLUMN_ID.getKey(), //
                        SCOPE.getKey(), //
                        ROW_ID.getKey(), //
                        ORIGINAL_VALUE_PARAMETER, //
                        FILTER.getKey(), //
                        NEW_VALUE_PARAMETER) //
        );
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

    private ActionContext getActionContext(SimpleEntry<String, String>... entries) {
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
    public void should_replace_value() {

        // given
        final Long rowId = 1L;
        final String joe = "Joe";
        final DataSetRow row = getDataSetRow(rowId, joe);

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
        final DataSetRow row = getDataSetRow(2L, joe);

        final Map<String, String> parameters = getParameters(rowId, "Jimmy", joe);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row.get("0000"), is(joe));
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
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(FORBID_DISTRIBUTED));
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

    private DataSetRow getDataSetRow(Long rowId, String firstValue) {

        final Map<String, String> values = new HashMap<>();
        values.put("0000", firstValue);

        final DataSetRow row = new DataSetRow(values);
        row.setTdpId(rowId);
        return row;
    }
}
