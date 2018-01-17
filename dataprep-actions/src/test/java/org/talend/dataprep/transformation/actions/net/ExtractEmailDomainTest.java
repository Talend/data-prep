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

package org.talend.dataprep.transformation.actions.net;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for ExtractEmailDomain action. Creates one consumer, and test it.
 *
 * @see ExtractEmailDomain
 */
public class ExtractEmailDomainTest extends AbstractMetadataBaseTest<ExtractEmailDomain> {

    private Map<String, String> parameters;

    public ExtractEmailDomainTest() {
        super(new ExtractEmailDomain());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(ExtractEmailDomainTest.class.getResourceAsStream("extractDomainAction.json"));
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.INVISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // Nothing to test, this action is never applied in place
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.SPLIT.getDisplayName(Locale.US)));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "david.bowie@yopmail.com");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "david.bowie@yopmail.com");
        expectedValues.put("0003", "david.bowie");
        expectedValues.put("0004", "yopmail.com");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_values_invalid() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "david.bowie");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "david.bowie");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0002", "01/01/2015");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_metadata() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "email"));
        input.add(createMetadata("0002", "last update"));
        final DataSetRow row = new DataSetRow(new RowMetadata(input));

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "email"));
        expected.add(createMetadata("0003", "email_local"));
        expected.add(createMetadata("0004", "email_domain"));
        expected.add(createMetadata("0002", "last update"));

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row.getRowMetadata().getColumns());
    }

    @Test
    public void test_metadata_with_multiple_executions() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "email"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "email"));
        expected.add(createMetadata("0005", "email_local"));
        expected.add(createMetadata("0006", "email_domain"));
        expected.add(createMetadata("0003", "email_local"));
        expected.add(createMetadata("0004", "email_domain"));
        expected.add(createMetadata("0002", "last update"));

        //when
        ActionTestWorkbench.test(rowMetadata, actionRegistry, factory.create(action, parameters), factory.create(action, parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    @Override
    protected ColumnMetadata.Builder columnBaseBuilder() {
        return super.columnBaseBuilder().headerSize(12).valid(5).invalid(2).empty(0);
    }

    @Test
    public void should_accept_column() {
        ColumnMetadata column = getColumn(Type.STRING);
        column.setDomain("email");
        assertTrue(action.acceptField(column));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.INTEGER)));
        assertFalse(action.acceptField(getColumn(Type.DOUBLE)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));

        ColumnMetadata column = getColumn(Type.STRING);
        column.setDomain("not an email");
        assertFalse(action.acceptField(column));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }
}
