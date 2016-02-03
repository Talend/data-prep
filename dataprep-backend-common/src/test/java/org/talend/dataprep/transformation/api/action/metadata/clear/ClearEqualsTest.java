package org.talend.dataprep.transformation.api.action.metadata.clear;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for ClearEquals action. Creates one consumer, and test it.
 *
 * @see ClearEquals
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

        parameters = ActionMetadataTestUtils.parseParameters(ClearEqualsTest.class.getResourceAsStream("clearEqualsAction.json"));

        parameters.put(ClearEquals.VALUE_PARAMETER, "Something");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(row.values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0001", "David Bowie"), //
                        MapEntry.entry("0002", "N"), //
                        MapEntry.entry("0003", ""));

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

        parameters = ActionMetadataTestUtils.parseParameters(ClearEqualsTest.class.getResourceAsStream("clearEqualsAction.json"));

        parameters.put(ClearEquals.VALUE_PARAMETER, "Badibada");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(row.values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0001", "David Bowie"), //
                        MapEntry.entry("0002", "N"), //
                        MapEntry.entry("0003", "Something"));
    }

    @Test
    public void should_clear_boolean_because_equals() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "Something");
        values.put("0003", "True");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.BOOLEAN) //
                .computedId("0003") //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        parameters = ActionMetadataTestUtils.parseParameters(ClearEqualsTest.class.getResourceAsStream("clearEqualsAction.json"));

        parameters.put(ClearEquals.VALUE_PARAMETER, Boolean.TRUE.toString());

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(row.values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0001", "David Bowie"), //
                        MapEntry.entry("0002", "Something"), //
                        MapEntry.entry("0003", ""));
    }

    @Test
    public void should_clear_boolean_because_equals_ignore_case() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "Something");
        values.put("0003", "False");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                                                             .type(Type.BOOLEAN) //
                                                             .computedId("0003") //
                                                             .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        parameters = ActionMetadataTestUtils.parseParameters(ClearEqualsTest.class.getResourceAsStream("clearEqualsAction.json"));

        parameters.put(ClearEquals.VALUE_PARAMETER, Boolean.FALSE.toString());

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(row.values()) //
            .isNotEmpty() //
            .hasSize(3) //
            .containsExactly(MapEntry.entry("0001", "David Bowie"), //
                             MapEntry.entry("0002", "Something"), //
                             MapEntry.entry("0003", ""));
    }

    @Test
    public void should_not_clear_boolean_because_not_equals() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "True");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.BOOLEAN) //
                .computedId("0003") //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        parameters = ActionMetadataTestUtils.parseParameters(ClearEqualsTest.class.getResourceAsStream("clearEqualsAction.json"));

        parameters.put(ClearEquals.VALUE_PARAMETER, "tchoubidoo");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(row.values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0001", "David Bowie"), //
                        MapEntry.entry("0002", "True"), //
                        MapEntry.entry("0003", "Something"));
    }

    @Test
    public void should_accept_column() {
        for (Type type : Type.values()) {
            assertTrue(action.acceptColumn(getColumn(type)));
        }
    }

}
