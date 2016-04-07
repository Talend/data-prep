//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.column;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.parameters.Parameter;


public class CreateNewColumnTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private CreateNewColumn action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(CreateNewColumnTest.class.getResourceAsStream("createNewColumnAction.json"));
    }

    @Test
    public void testActionName() throws Exception {
        assertEquals("create_new_column", action.getName());
    }

    @Test
    public void testActionParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters();
        assertEquals(5, parameters.size());
        assertTrue(parameters.stream().filter(p -> StringUtils.equals(p.getName(), "mode_new_column")).findFirst().isPresent());
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.COLUMN_METADATA.getDisplayName()));
    }

    @Test
    public void should_copy_row_constant() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum");
        expectedValues.put("0003", "tagada");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_copy_row_empty() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum");
        expectedValues.put("0003", "");
        expectedValues.put("0002", "01/01/2015");

        parameters.put(CreateNewColumn.MODE_PARAMETER, CreateNewColumn.EMPTY_MODE);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_copy_row_other_column() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum");
        expectedValues.put("0003", "Bacon ipsum");
        expectedValues.put("0002", "01/01/2015");

        parameters.put(CreateNewColumn.MODE_PARAMETER, CreateNewColumn.COLUMN_MODE);
        parameters.put(CreateNewColumn.SELECTED_COLUMN_PARAMETER, "0001");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_do_nothing_without_any_parameters() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.remove(CreateNewColumn.MODE_PARAMETER);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));
    }

    @Test
    public void should_do_nothing_with_wrong_parameters_1() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.remove(CreateNewColumn.DEFAULT_VALUE_PARAMETER);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));
    }

    @Test
    public void should_do_nothing_with_wrong_parameters_2() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.remove(CreateNewColumn.DEFAULT_VALUE_PARAMETER);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(row.get("0000"), "first");
        assertEquals(row.get("0001"), "second");
        assertEquals(row.get("0002"), "Done !");
    }


    @Test
    public void should_do_nothing_with_wrong_parameters_3() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(CreateNewColumn.MODE_PARAMETER, CreateNewColumn.COLUMN_MODE);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(row.get("0000"), "first");
        assertEquals(row.get("0001"), "second");
        assertEquals(row.get("0002"), "Done !");
    }

    @Test
    public void should_do_nothing_with_wrong_parameters_4() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(CreateNewColumn.MODE_PARAMETER, CreateNewColumn.COLUMN_MODE);
        parameters.put(CreateNewColumn.SELECTED_COLUMN_PARAMETER, "0009");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(row.get("0000"), "first");
        assertEquals(row.get("0001"), "second");
        assertEquals(row.get("0002"), "Done !");
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.ANY)));
    }

}