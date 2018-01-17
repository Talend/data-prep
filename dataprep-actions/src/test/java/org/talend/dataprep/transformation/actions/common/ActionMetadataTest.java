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

package org.talend.dataprep.transformation.actions.common;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.category.ScopeCategory.*;

import java.util.*;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.ClassPathActionRegistry;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

public class ActionMetadataTest {

    private final CellTransformation cellTransformation = new CellTransformation();

    private final LineTransformation lineTransformation = new LineTransformation();

    private final ColumnTransformation columnTransformation = new ColumnTransformation();

    private final TableTransformation tableTransformation = new TableTransformation();

    protected final ActionFactory factory = new ActionFactory();

    protected final ActionRegistry actionRegistry = new ClassPathActionRegistry("org.talend.dataprep.transformation.actions");

    @Test
    public void acceptScope_should_pass_with_cell_transformation() throws Exception {
        // when
        final boolean result = cellTransformation.acceptScope(CELL);

        // then
        assertThat(result, is(true));
    }

    @Test
    public void acceptScope_should_pass_with_line_transformation() throws Exception {
        // when
        final boolean result = lineTransformation.acceptScope(LINE);

        // then
        assertThat(result, is(true));
    }

    @Test
    public void acceptScope_should_pass_with_column_transformation() throws Exception {
        // when
        final boolean result = columnTransformation.acceptScope(COLUMN);

        // then
        assertThat(result, is(true));
    }

    @Test
    public void acceptScope_should_pass_with_table_transformation() throws Exception {
        //when
        final boolean result = tableTransformation.acceptScope(DATASET);

        // then
        assertThat(result, is(true));
    }

    @Test
    public void acceptScope_should_fail_with_non_cell_transformation() throws Exception {
        // when
        final boolean result = columnTransformation.acceptScope(CELL);

        // then
        assertThat(result, is(false));
    }

    @Test
    public void acceptScope_should_fail_with_non_line_transformation() throws Exception {
        // when
        final boolean result = cellTransformation.acceptScope(LINE);

        // then
        assertThat(result, is(false));
    }

    @Test
    public void acceptScope_should_fail_with_non_column_transformation() throws Exception {
        // when
        final boolean result = tableTransformation.acceptScope(COLUMN);

        // then
        assertThat(result, is(false));
    }

    @Test
    public void acceptScope_should_fail_with_non_table_transformation() throws Exception {
        //when
        final boolean result = columnTransformation.acceptScope(DATASET);

        // then
        assertThat(result, is(false));
    }

    @Test
    public void default_parameters_should_contains_implicit_parameters() throws Exception {
        // when
        final List<Parameter> defaultParams = columnTransformation.getParameters(Locale.US);

        // then
        assertThat(defaultParams, hasItems(ImplicitParameters.getParameters(Locale.US).toArray(new Parameter[3])));
    }

    @Test
    public void create_should_throw_exception_when_scope_parameters_are_not_consistent() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();

        // when
        try {
            factory.create(columnTransformation, parameters);
            fail("should have thrown TDPException because scope parameters are inconsistent (scope is missing)");
        }

        // then
        catch (final TalendRuntimeException e) {
            assertThat(e.getCode(), Matchers.is(BaseErrorCodes.MISSING_ACTION_SCOPE));
        }
    }

    @Test
    public void create_result_should_call_execute_on_cell() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "cell");
        parameters.put("column_id", "0000");
        parameters.put("row_id", "58");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0000", "toto");
        final DataSetRow row = new DataSetRow(rowValues);
        row.setTdpId(58L);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(cellTransformation, parameters));

        // then
        assertThat(row.get("0000"), is("TOTO"));
    }

    @Test
    public void create_result_should_not_call_execute_on_cell_with_wrong_row_id() throws Exception {
        // given
        final DataSetRow row = builder() //
                .value("test", Type.STRING) //
                .value("toto", Type.STRING) //
                .build();
        row.setTdpId(60L);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "cell");
        parameters.put("column_id", "0001");
        parameters.put("row_id", "58");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(cellTransformation, parameters));

        // then
        assertThat(row.get("0001"), is("toto"));
    }

    @Test
    public void create_result_should_call_execute_on_line() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "line");
        parameters.put("row_id", "58");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0001", "toto");
        rowValues.put("0002", "tata");
        final DataSetRow row = new DataSetRow(rowValues);
        row.setTdpId(58L);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(lineTransformation, parameters));

        // then
        assertThat(row.get("0001"), is("TOTO"));
        assertThat(row.get("0002"), is("TATA"));
    }

    @Test
    public void create_result_should_not_call_execute_on_line_with_wrong_row_id() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "line");
        parameters.put("row_id", "58");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0001", "toto");
        rowValues.put("0002", "tata");
        final DataSetRow row = new DataSetRow(rowValues);
        row.setTdpId(60L);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(lineTransformation, parameters));

        // then
        assertThat(row.get("0001"), is("toto"));
        assertThat(row.get("0002"), is("tata"));
    }

    @Test
    public void create_result_should_call_execute_on_column() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("column_id", "0001");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0001", "toto");
        rowValues.put("0002", "tata");
        final DataSetRow row = new DataSetRow(rowValues);
        row.setTdpId(58L);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(columnTransformation, parameters));

        // then
        assertThat(row.get("0001"), is("TOTO"));
        assertThat(row.get("0002"), is("tata"));
    }

    @Test
    public void create_result_should_call_execute_on_table() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "dataset");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0000", "toto");
        rowValues.put("0001", "tata");
        final DataSetRow row = new DataSetRow(rowValues);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(tableTransformation, parameters));

        // then
        assertThat(row.get("0000"), is("TOTO"));
        assertThat(row.get("0001"), is("TATA"));
    }
}

// ------------------------------------------------------------------------------------------------------------------
// -----------------------------------------IMPLEMENTATIONS CLASSES--------------------------------------------------
// ------------------------------------------------------------------------------------------------------------------

class CellTransformation extends AbstractActionMetadata implements CellAction {

    @Override
    public String getName() {
        return "CellTransformation";
    }

    @Override
    public String getCategory(Locale locale) {
        return null;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return false;
    }

    @Override
    public void applyOnCell(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        row.set(columnId, value.toUpperCase());
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.emptySet();
    }
}

class LineTransformation extends AbstractActionMetadata implements RowAction {

    @Override
    public String getName() {
        return "LineTransformation";
    }

    @Override
    public String getCategory(Locale locale) {
        return null;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return false;
    }

    @Override
    public void applyOnLine(DataSetRow row, ActionContext context) {
        for (final Map.Entry<String, Object> entry : row.values().entrySet()) {
            row.set(entry.getKey(), entry.getValue().toString().toUpperCase());
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.emptySet();
    }
}

class ColumnTransformation extends AbstractActionMetadata implements ColumnAction {

    @Override
    public String getName() {
        return "ColumnTransformation";
    }

    @Override
    public String getCategory(Locale locale) {
        return null;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return false;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        row.set(columnId, value.toUpperCase());
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.emptySet();
    }
}

class TableTransformation extends AbstractActionMetadata implements DataSetAction {

    @Override
    public String getName() {
        return "TableTransformation";
    }

    @Override
    public String getCategory(Locale locale) {
        return null;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return false;
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        for (final Map.Entry<String, Object> entry : row.values().entrySet()) {
            row.set(entry.getKey(), entry.getValue().toString().toUpperCase());
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.emptySet();
    }
}
