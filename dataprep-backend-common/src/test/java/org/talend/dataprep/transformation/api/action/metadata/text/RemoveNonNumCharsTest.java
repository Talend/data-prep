// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for LowerCase action. Creates one consumer, and test it.
 *
 * @see LowerCase
 */
public class RemoveNonNumCharsTest {

    /** The action to test. */
    private RemoveNonNumChars action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new RemoveNonNumChars();

        parameters = ActionMetadataTestUtils.parseParameters( //
                //
                RemoveNonNumCharsTest.class.getResourceAsStream("remove_non_num_chars.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS_ADVANCED.getDisplayName()));
    }

    /**
     * @see LowerCase#create(Map)
     */
    @Test
    public void test_basic() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "Vincent");
        values.put("entity", "€10k");
        values.put("joined", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("name", "Vincent");
        expectedValues.put("entity", "10");
        expectedValues.put("joined", "May 20th 2015");

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "entity");

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_some_values(){
        assertEquals("10",action.apply("€10k"));
        assertEquals("10",action.apply("10"));
        assertEquals("0123456789",action.apply("0123456789"));
        assertEquals("10-",action.apply("€10-"));
        assertEquals("10.5",action.apply("€10.5K"));
        assertEquals("-10,5",action.apply("-$10,5K"));
        assertEquals("1010",action.apply("aa10aa10"));
        assertEquals(" 1 2 3- ,",action.apply("A 1 b2 ç3!%£*ê}~(-+*/&\":# ,"));
        assertEquals(" 123",action.apply("é@ àèÏ123éç"));
    }

    @Test
    public void test_some_special_values(){
        assertEquals("",action.apply(""));
        assertEquals("",action.apply(null));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }
}
