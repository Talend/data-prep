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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for RemoveFractionalPart action. Creates one consumer, and test it.
 *
 * @see RemoveFractionalPart
 */
public class RemoveFractionalPartTest extends AbstractRoundTest<RemoveFractionalPart> {

    public RemoveFractionalPartTest() {
        super(new RemoveFractionalPart());
    }

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(RemoveFractionalPartTest.class.getResourceAsStream("removeFractionalPartAction.json"));
    }

    @Test
    public void testName() {
        assertEquals(RemoveFractionalPart.ACTION_NAME, action.getName());
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.NUMBERS.getDisplayName(Locale.US)));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new LinkedHashMap<>();
        values.put("0000", "-5.1");
        values.put("0001", "3.0");
        values.put("0002", "Done !");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0000", "-5.1");
        expectedValues.put("0001", "3.0");
        expectedValues.put("0002", "Done !");
        expectedValues.put("0003", "-5");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_rounded").type(Type.DOUBLE).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() {
        // given
        DataSetRow row = getRow("-5.1", "3", "Done !");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(row.values().size(), 3);

        // then
        DataSetRow expected = getRow("-5", "3", "Done !");
        assertEquals(expected, row);
    }

    @Test
    public void testPositive() {
        testCommon("5.0", "5");
        testCommon("5.1", "5");
        testCommon("5.5", "5");
        testCommon("5.8", "5");
    }

    @Test
    public void testNegative() {
        testCommon("-5.0", "-5");
        testCommon("-5.4", "-5");
        testCommon("-5.6", "-5");
    }

    @Test
    public void test_huge_number() {
        testCommon("1234567890.1", "1234567890");
        testCommon("891234567897.9", "891234567897");
        testCommon("891234567899.9", "891234567899");
        testCommon("999999999999.9", "999999999999");
    }

    @Test
    public void test_percentage_number() {
        testCommon("5%", "0");
        testCommon("50%", "0");
        testCommon("500%", "5");
    }

    @Test
    public void test_huge_number_negative() {
        testCommon("-1234567890.1", "-1234567890");
        testCommon("-891234567897.9", "-891234567897");
        testCommon("-891234567899.9", "-891234567899");
        testCommon("-999999999999.9", "-999999999999");
    }

    @Test
    public void testInteger() {
        testCommon("5", "5");
        testCommon("-5", "-5");
    }

    @Test
    public void testString() {
        testCommon("tagada", "tagada");
        testCommon("", "");
        testCommon("null", "null");
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

    @Override
    protected Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    protected List<String> getExpectedParametersName() {
        return Arrays.asList("create_new_column", "column_id", "row_id", "scope", "filter");
    }
}
