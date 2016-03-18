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

package org.talend.dataprep.transformation.api.action.metadata.net;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for ExtractEmailDomain action. Creates one consumer, and test it.
 *
 * @see ExtractEmailDomain
 */
public class ExtractEmailDomainTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private ExtractEmailDomain action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(ExtractEmailDomainTest.class.getResourceAsStream("extractDomainAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.SPLIT.getDisplayName()));
    }

    @Test
    public void test_values() {
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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        ActionTestWorkbench.test(rowMetadata, factory.create(action, parameters), factory.create(action, parameters));

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
        assertTrue(action.acceptColumn(column));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.INTEGER)));
        assertFalse(action.acceptColumn(getColumn(Type.DOUBLE)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));

        ColumnMetadata column = getColumn(Type.STRING);
        column.setDomain("not an email");
        assertFalse(action.acceptColumn(column));
    }
}
