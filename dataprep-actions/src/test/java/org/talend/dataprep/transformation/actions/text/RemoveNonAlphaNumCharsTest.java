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

package org.talend.dataprep.transformation.actions.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for LowerCase action. Creates one consumer, and test it.
 *
 * @see LowerCase
 */
public class RemoveNonAlphaNumCharsTest extends AbstractMetadataBaseTest<RemoveNonAlphaNumChars> {

    private Map<String, String> parameters;

    public RemoveNonAlphaNumCharsTest() {
        super(new RemoveNonAlphaNumChars());
    }

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
        assertThat(action.getCategory(Locale.US), is(ActionCategory.STRINGS_ADVANCED.getDisplayName(Locale.US)));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "€10k");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "€10k");
        expectedValues.put("0003", "10k");
        expectedValues.put("0002", "May 20th 2015");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_only_alpha").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "Vincent");
        values.put("0001", "€10k");
        values.put("0002", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "Vincent");
        expectedValues.put("0001", "10k");
        expectedValues.put("0002", "May 20th 2015");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
        assertEquals("µ", action.apply("£µ§€¥"));
    }

    @Test
    public void test_some_special_values() {
        assertEquals("", action.apply(""));
        assertEquals("", action.apply(null));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

    @Test
    public void test_special_values() {
        assertEquals("ÃƒÂÃÂÂšÃÂ105k", action.apply("ÃƒÂ¢Ã¢Â€ÂšÃ‚Â¬10.5k"));
        assertEquals("aaßaa", action.apply("aaßaa"));
        assertEquals("aa亜亜亜aa", action.apply("aa亜亜亜aa"));
        assertEquals("aaあああaa", action.apply("aaあああaa"));
    }
}

