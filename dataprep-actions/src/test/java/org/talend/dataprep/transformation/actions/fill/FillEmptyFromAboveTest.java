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

package org.talend.dataprep.transformation.actions.fill;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the FillEmptyFromAbove action.
 *
 * @see FillEmptyFromAbove
 */
public class FillEmptyFromAboveTest extends AbstractMetadataBaseTest<FillEmptyFromAbove> {

    public FillEmptyFromAboveTest() {
        super(new FillEmptyFromAbove());
    }

    @PostConstruct
    public void init() {
        action = (FillEmptyFromAbove) action.adapt(ColumnMetadata.Builder.column().type(Type.STRING).build());
    }

    @Test
    public void test_adapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.INVISIBLE_DISABLED;
    }

    @Test
    public void shouldGetParameters() throws Exception {
        // given
        List<String> parameterNames = Arrays.asList("create_new_column", "column_id", "row_id", "scope", "filter");

        // when
        final List<Parameter> parameters = action.getParameters(Locale.US);

        // then
        assertNotNull(parameters);
        // column_id, row_id, scope, filter
        assertEquals(4, parameters.size());
        final List<String> expectedParametersNotFound = parameters.stream().map(Parameter::getName)
                .filter(n -> !parameterNames.contains(n)).collect(Collectors.toList());
        assertTrue(expectedParametersNotFound.toString() + " not found", expectedParametersNotFound.isEmpty());
    }

    @Test
    public void test_apply_in_newcolumn() throws Exception {
        // Always in place
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // given
        Map<String, String> rowContent = new HashMap<>();

        // row 0
        rowContent.put("0000", "David");
        rowContent.put("0001", null);
        final DataSetRow row0 = new DataSetRow(rowContent);

        // row 1
        rowContent.put("0000", "David");
        rowContent.put("0001", "");
        final DataSetRow row1 = new DataSetRow(rowContent);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "200");
        final DataSetRow row2 = new DataSetRow(rowContent);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "");
        final DataSetRow row3 = new DataSetRow(rowContent);

        // row 4
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "2011-08-19");
        final DataSetRow row4 = new DataSetRow(rowContent);

        // row 5
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", null);
        final DataSetRow row5 = new DataSetRow(rowContent);

        // row 6
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "600");
        final DataSetRow row6 = new DataSetRow(rowContent);

        // row 7
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "");
        final DataSetRow row7 = new DataSetRow(rowContent);

        // row 8
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "\t");
        final DataSetRow row8 = new DataSetRow(rowContent);


        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "\f");
        final DataSetRow row81 = new DataSetRow(rowContent);

        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "\n");
        final DataSetRow row82 = new DataSetRow(rowContent);

        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "\r");
        final DataSetRow row83 = new DataSetRow(rowContent);

        // row 9
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "900");
        final DataSetRow row9 = new DataSetRow(rowContent);

        // row 10
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "aaa");
        final DataSetRow row10 = new DataSetRow(rowContent);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");

        // when
        ActionTestWorkbench.test(Arrays.asList(row0,row1, row2,row3, row4,row5, row6,row7, row8, row81, row82, row83, row9, row10), actionRegistry, factory.create(
                action, parameters));

        // then
        assertNull(row0.get("0001"));
        assertEquals("", row1.get("0001"));
        assertEquals("200", row2.get("0001"));
        assertEquals("200", row3.get("0001"));
        assertEquals("2011-08-19", row4.get("0001"));
        assertEquals("2011-08-19", row5.get("0001"));
        assertEquals("600", row6.get("0001"));
        assertEquals("600", row7.get("0001"));
        assertEquals("600", row8.get("0001"));
        assertEquals("600", row81.get("0001"));
        assertEquals("600", row82.get("0001"));
        assertEquals("600", row83.get("0001"));
        assertEquals("900", row9.get("0001"));
        assertEquals("aaa", row10.get("0001"));
    }

    @Test
    public void should_fill_empty_string_with_deleted_rows() throws Exception {
        // given
        Map<String, String> rowContent = new HashMap<>();

        // row 0
        rowContent.put("0000", "David");
        rowContent.put("0001", null);
        final DataSetRow row0 = new DataSetRow(rowContent);

        // row 1
        rowContent.put("0000", "David");
        rowContent.put("0001", "");
        final DataSetRow row1 = new DataSetRow(rowContent);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "200");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setDeleted(true);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "");
        final DataSetRow row3 = new DataSetRow(rowContent);

        // row 4
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "2011-08-19");
        final DataSetRow row4 = new DataSetRow(rowContent);

        // row 5
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", null);
        final DataSetRow row5 = new DataSetRow(rowContent);

        // row 6
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "600");
        final DataSetRow row6 = new DataSetRow(rowContent);
        row6.setDeleted(true);

        // row 7
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "");
        final DataSetRow row7 = new DataSetRow(rowContent);

        // row 8
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "\t");
        final DataSetRow row8 = new DataSetRow(rowContent);


        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "\f");
        final DataSetRow row81 = new DataSetRow(rowContent);

        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "\n");
        final DataSetRow row82 = new DataSetRow(rowContent);

        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "\r");
        final DataSetRow row83 = new DataSetRow(rowContent);

        // row 9
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "900");
        final DataSetRow row9 = new DataSetRow(rowContent);

        // row 10
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "aaa");
        final DataSetRow row10 = new DataSetRow(rowContent);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");

        // when
        ActionTestWorkbench.test(Arrays.asList(row0,row1, row2,row3, row4,row5, row6,row7, row8, row81, row82, row83, row9, row10), actionRegistry, factory.create(
                action, parameters));

        // then
        assertNull(row0.get("0001"));
        assertEquals("", row1.get("0001"));
        assertEquals("200", row2.get("0001"));
        assertEquals("", row3.get("0001"));
        assertEquals("2011-08-19", row4.get("0001"));
        assertEquals("2011-08-19", row5.get("0001"));
        assertEquals("600", row6.get("0001"));
        assertEquals("2011-08-19", row7.get("0001"));
        assertEquals("2011-08-19", row8.get("0001"));
        assertEquals("2011-08-19", row81.get("0001"));
        assertEquals("2011-08-19", row82.get("0001"));
        assertEquals("2011-08-19", row83.get("0001"));
        assertEquals("900", row9.get("0001"));
        assertEquals("aaa", row10.get("0001"));
    }

    @Test
    public void should_fill_empty_string_filter() throws Exception {
        // given
        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "");
        final DataSetRow row1 = new DataSetRow(rowContent);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "200");
        final DataSetRow row2 = new DataSetRow(rowContent);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "");
        final DataSetRow row3 = new DataSetRow(rowContent);

        // row 4
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "400");
        final DataSetRow row4 = new DataSetRow(rowContent);

        // row 5
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", null);
        final DataSetRow row5 = new DataSetRow(rowContent);

        // row 6
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "600");
        final DataSetRow row6 = new DataSetRow(rowContent);

        // row 7
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "");
        final DataSetRow row7 = new DataSetRow(rowContent);

        // row 8
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "\t");
        final DataSetRow row8 = new DataSetRow(rowContent);

        // row 9
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "900");
        final DataSetRow row9 = new DataSetRow(rowContent);

        // row 10
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "aaa");
        final DataSetRow row10 = new DataSetRow(rowContent);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("filter", "200");

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5, row6, row7, row8, row9, row10), actionRegistry,
                factory.create(action, parameters));

        // then
        assertEquals("", row1.get("0001"));
        assertEquals("200", row2.get("0001"));
        assertEquals("", row3.get("0001"));
        assertEquals("400", row4.get("0001"));
        assertEquals(null, row5.get("0001"));
        assertEquals("600", row6.get("0001"));
        assertEquals("", row7.get("0001"));
        assertEquals("\t", row8.get("0001"));
        assertEquals("900", row9.get("0001"));
        assertEquals("aaa", row10.get("0001"));
    }

    @Test
    public void should_not_copy_blanks() throws Exception {
        // given
        Map<String, String> rowContent = new HashMap<>();

        // row 0
        rowContent.put("0000", "David");
        String davidFirstBlankValue = "\u2006";
        rowContent.put("0001", davidFirstBlankValue);
        final DataSetRow row0 = new DataSetRow(rowContent);

        // row 1
        rowContent.put("0000", "David");
        String davidSecondBlankValue = "  \t\r";
        rowContent.put("0001", davidSecondBlankValue);
        final DataSetRow row1 = new DataSetRow(rowContent);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");

        // when
        ActionTestWorkbench.test(Arrays.asList(row0,row1), actionRegistry, factory.create(
                action, parameters));

        // then
        assertEquals(davidFirstBlankValue, row0.get("0001"));
        assertEquals(davidSecondBlankValue, row1.get("0001"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.FORBID_DISTRIBUTED));
    }
}
