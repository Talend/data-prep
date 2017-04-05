// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.text;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

/**
 * Test class for RemoveRepeatedChars action. Creates one consumer, and test it.
 *
 * @see RemoveRepeatedChars
 */
public class RemoveRepeatedCharsTest extends AbstractMetadataBaseTest {

    /** The action for removing consecutive. */
    private RemoveRepeatedChars action = new RemoveRepeatedChars();

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(RemoveRepeatedCharsTest.class.getResourceAsStream("removeRepeatedCharsAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS.getDisplayName()));
    }

    @Test
    public void should_remove_repeated_whiteSpace() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "ab   c  d");
        final DataSetRow row = new DataSetRow(values);
        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, "whitespace");
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("ab c d", row.get("0000"));
    }

    @Test
    public void should_remove_repeated_tab() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "abc\t\t\td");
        final DataSetRow row = new DataSetRow(values);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, "whitespace");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("abc\td", row.get("0000"));
    }

    @Test
    public void should_remove_repeated_return() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "abc\r\r\rd");
        final DataSetRow row = new DataSetRow(values);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, "whitespace");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("abc\rd", row.get("0000"));
    }

    @Test
    public void should_remove_repeated_unicode_whitespace() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "\u2028\u2028abcd");
        final DataSetRow row = new DataSetRow(values);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, "whitespace");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("\u2028abcd", row.get("0000"));
    }

    @Test
    public void should_not_remove_repeated_return_wrap() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "abc\r\n\r\nd");
        final DataSetRow row = new DataSetRow(values);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, "whitespace");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("abc\r\nd", row.get("0000"));
    }

    @Test
    public void should_not_remove_null() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", null);
        final DataSetRow row = new DataSetRow(values);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, "whitespace");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(null, row.get("0000"));
    }

    @Test
    public void should_remove_custom_value() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "haand");
        final DataSetRow row = new DataSetRow(values);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, RemoveRepeatedChars.CUSTOM);
        parameters.put(RemoveRepeatedChars.CUSTOM_REPEAT_CHAR_PARAMETER, "a");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("hand", row.get("0000"));
    }

    @Test
    public void should_not_remove_custom_value() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "haand");
        final DataSetRow row = new DataSetRow(values);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, RemoveRepeatedChars.CUSTOM);
        parameters.put(RemoveRepeatedChars.CUSTOM_REPEAT_CHAR_PARAMETER, "n");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("haand", row.get("0000"));
    }

    @Test
    public void should_not_remove_custom_string() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "hanand");
        final DataSetRow row = new DataSetRow(values);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, RemoveRepeatedChars.CUSTOM);
        parameters.put(RemoveRepeatedChars.CUSTOM_REPEAT_CHAR_PARAMETER, "an");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("hanand", row.get("0000"));
    }

    @Test
    public void should_not_remove_custom_null() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "haand");
        final DataSetRow row = new DataSetRow(values);

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, RemoveRepeatedChars.CUSTOM);
        parameters.put(RemoveRepeatedChars.CUSTOM_REPEAT_CHAR_PARAMETER, null);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("haand", row.get("0000"));
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
}
