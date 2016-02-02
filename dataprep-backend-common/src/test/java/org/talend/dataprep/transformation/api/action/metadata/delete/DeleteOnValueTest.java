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
package org.talend.dataprep.transformation.api.action.metadata.delete;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ReplaceOnValueHelper;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Test class for DeleteOnValue action. Creates one consumer, and test it.
 *
 * @see DeleteOnValue
 */
public class DeleteOnValueTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private DeleteOnValue action;

    /** The dataprep ready jackson builder. */
    @Autowired
    public Jackson2ObjectMapperBuilder builder;
    
    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(DeleteOnValueTest.class.getResourceAsStream("deleteOnValueAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATA_CLEANSING.getDisplayName()));
    }

    @Test
    public void should_delete() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "Berlin");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals("Berlin", row.get("city"));
    }

    @Test
    public void should_not_delete_even_with_leading_space() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", " Berlin"); // notice the space before ' Berlin'
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals(" Berlin", row.get("city"));
    }

    @Test
    public void should_not_delete_even_with_trailing_space() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "Berlin "); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals("Berlin ", row.get("city"));
    }

    @Test
    public void should_not_delete_even_with_enclosing_spaces() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", " Berlin "); // notice the spaces enclosing ' Berlin '
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals(" Berlin ", row.get("city"));
    }

    @Test
    public void should_delete_because_regexp_is_used() throws IOException {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "AAA Berlin BBB"); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters( //
                DeleteOnValueTest.class.getResourceAsStream("deleteOnValueAction.json"));
        regexpParameters.put("value", generateJson(".*Berlin.*", ReplaceOnValueHelper.REGEX_MODE));

        // when
        ActionTestWorkbench.test(row, action.create(regexpParameters).getRowAction());

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals("AAA Berlin BBB", row.get("city"));
    }

    @Test
    public void test_TDP_663() throws IOException {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "AAA Berlin BBB"); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters( //
                //
                DeleteOnValueTest.class.getResourceAsStream("deleteOnValueAction.json"));
        regexpParameters.put("value", generateJson("*", ReplaceOnValueHelper.REGEX_MODE));

        // when
        ActionTestWorkbench.test(row, action.create(regexpParameters).getRowAction());

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals("AAA Berlin BBB", row.get("city"));
    }


    @Test
    public void test_TDP_958() throws IOException {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "AAA Ber BBB"); // notice the space after 'Berlin '
        final DataSetRow row = new DataSetRow(values);

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters( //
                DeleteOnValueTest.class.getResourceAsStream("deleteOnValueAction.json"));
        regexpParameters.put("value", generateJson("", ReplaceOnValueHelper.REGEX_MODE));

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals("AAA Ber BBB", row.get("city"));
    }

    @Test
    public void should_not_delete_because_value_not_found() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
    }

    @Test
    public void should_not_delete_because_of_case() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "berlin");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals("berlin", row.get("city"));
    }

    @Test
    public void should_not_delete_because_value_different() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "David Bowie");
        values.put("city", "youhou");
        final DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        //then
        assertFalse(row.isDeleted());
        assertEquals("David Bowie", row.get("name"));
        assertEquals("youhou", row.get("city"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptColumn(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptColumn(getColumn(Type.ANY)));
    }

    private String generateJson(String token, String operator) {
        ReplaceOnValueHelper r = new ReplaceOnValueHelper(token, operator);
        try {
            return builder.build().writeValueAsString(r);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
    
}
