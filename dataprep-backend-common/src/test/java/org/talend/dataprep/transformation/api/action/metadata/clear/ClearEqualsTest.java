package org.talend.dataprep.transformation.api.action.metadata.clear;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for ClearInvalid action. Creates one consumer, and test it.
 *
 * @see ClearInvalid
 */
public class ClearEqualsTest {

    /** The action to test. */
    private ClearEquals action;

    private Map<String, String> parameters;

    /**
     * Default constructor.
     */
    public ClearEqualsTest() throws IOException {
        action = new ClearEquals();
    }

    @Test
    public void testName() throws Exception {
        assertThat(action.getName(), is(ClearEquals.ACTION_NAME));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATA_CLEANSING.getDisplayName()));
    }

    @Test
    public void testActionScope() throws Exception {
        assertThat(action.getActionScope(), hasItem("equals"));
    }

    @Test
    public void should_clear_because_equals() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "N");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.STRING) //
                .computedId("0003") //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0001", "David Bowie");
        expectedValues.put("0002", "N");
        expectedValues.put("0003", "");

        parameters = ActionMetadataTestUtils
                .parseParameters(ClearEqualsTest.class.getResourceAsStream("clearEqualsAction.json"));

        parameters.put(ClearEquals.VALUE_PARAMETER, "Something");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_clear_because_not_equals() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "N");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.STRING) //
                .computedId("0003") //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("0001", "David Bowie");
        expectedValues.put("0002", "N");
        expectedValues.put("0003", "Something");

        parameters = ActionMetadataTestUtils
                .parseParameters(ClearEqualsTest.class.getResourceAsStream("clearEqualsAction.json"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_accept_column() {
        for (Type type : Type.values()) {
            assertTrue(action.acceptColumn(getColumn(type)));
        }
    }

}
