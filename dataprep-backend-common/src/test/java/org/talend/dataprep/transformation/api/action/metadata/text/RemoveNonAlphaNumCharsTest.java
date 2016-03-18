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
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for LowerCase action. Creates one consumer, and test it.
 *
 * @see LowerCase
 */
public class RemoveNonAlphaNumCharsTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private RemoveNonAlphaNumChars action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(RemoveNonAlphaNumCharsTest.class.getResourceAsStream("remove_non_alpha_num_chars.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS_ADVANCED.getDisplayName()));
    }

    @Test
    public void test_basic() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "Vincent");
        values.put("entity", "€10k");
        values.put("joined", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("name", "Vincent");
        expectedValues.put("entity", "10k");
        expectedValues.put("joined", "May 20th 2015");

        //when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_some_values() {
        assertEquals("10k", action.apply("-10k€"));
        assertEquals("105K", action.apply("€10.5K"));
        assertEquals("105K", action.apply("$10,5K"));
        assertEquals("aa10aa10", action.apply("aa10aa10"));

        assertEquals(" une belle voiture ", action.apply(" une belle voiture "));
        assertEquals("voiciuntest", action.apply("-voici_un#test"));

        assertEquals("àéïOù23", action.apply("àéïOù&~23"));
        assertEquals("", action.apply("£µ§€¥"));
    }

    @Test
    public void test_some_special_values() {
        assertEquals("", action.apply(""));
        assertEquals("", action.apply(null));
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
