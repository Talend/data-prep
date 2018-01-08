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
package org.talend.dataprep.transformation.actions.delete;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for DeleteOnValue action. Creates one consumer, and test it.
 *
 * @see DeleteOnValue
 */
public class DeleteOnValueTest extends AbstractMetadataBaseTest<DeleteOnValue> {

    private Map<String, String> parameters;

    public DeleteOnValueTest() {
        super(new DeleteOnValue());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(DeleteOnValueTest.class.getResourceAsStream("deleteOnValueAction.json"));
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.DATA_CLEANSING.getDisplayName(Locale.US)));
    }

    @Test
    public void should_delete() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "Berlin");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("Berlin", row.get("0001"));
    }

    @Test
    public void should_not_delete_even_with_leading_space() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", " Berlin"); // notice the space before ' Berlin'
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals(" Berlin", row.get("0001"));
    }

    @Test
    public void should_not_delete_even_with_trailing_space() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "Berlin "); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("Berlin ", row.get("0001"));
    }

    @Test
    public void should_not_delete_even_with_enclosing_spaces() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", " Berlin "); // notice the spaces enclosing ' Berlin '
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals(" Berlin ", row.get("0001"));
    }

    @Test
    public void should_delete_because_regexp_is_used() throws IOException {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "AAA Berlin BBB"); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters( //
                DeleteOnValueTest.class.getResourceAsStream("deleteOnValueAction.json"));
        regexpParameters.put("value", generateJson(".*Berlin.*", ReplaceOnValueHelper.REGEX_MODE));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, regexpParameters));

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("AAA Berlin BBB", row.get("0001"));
    }

    @Test
    public void test_TDP_663() throws IOException {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "AAA Berlin BBB"); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters( //
                //
                DeleteOnValueTest.class.getResourceAsStream("deleteOnValueAction.json"));
        regexpParameters.put("value", generateJson("*", ReplaceOnValueHelper.REGEX_MODE));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, regexpParameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("AAA Berlin BBB", row.get("0001"));
    }


    @Test
    public void test_TDP_958() throws IOException {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "AAA Ber BBB"); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters( //
                DeleteOnValueTest.class.getResourceAsStream("deleteOnValueAction.json"));
        regexpParameters.put("value", generateJson("", ReplaceOnValueHelper.REGEX_MODE));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("AAA Ber BBB", row.get("0001"));
    }

    @Test
    public void should_not_delete_because_value_not_found() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_not_delete_because_of_case() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "berlin");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("berlin", row.get("0001"));
    }

    @Test
    public void should_not_delete_because_value_different() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "youhou");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
        assertEquals("youhou", row.get("0001"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_ALL));
    }

}
