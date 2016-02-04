package org.talend.dataprep.transformation.api.action.metadata.clear;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
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
import org.talend.dataprep.transformation.api.action.metadata.common.ReplaceOnValueHelper;

/**
 * Test class for ClearMatching action. Creates one consumer, and test it.
 *
 * @see ClearMatching
 */
public class ClearMatchingTest
    extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private ClearMatching action;

    private Map<String, String> parameters;

    /**
     * Default constructor.
     */
    public ClearMatchingTest() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(ClearInvalidTest.class.getResourceAsStream("clearEqualsAction.json"));
    }

    @Test
    public void testName() throws Exception {
        assertThat(action.getName(), is( ClearMatching.ACTION_NAME));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATA_CLEANSING.getDisplayName()));
    }

    @Test
    public void should_clear_because_equals() throws Exception {
        // given
        final Map<String, String> firstRowValues = new HashMap<>();
        firstRowValues.put("0001", "David Bowie");
        firstRowValues.put("0002", "N");
        firstRowValues.put("0003", "Something");

        final Map<String, String> secondRowValues = new HashMap<>();
        secondRowValues.put("0001", "Beer");
        secondRowValues.put("0002", "T");
        secondRowValues.put("0003", "NotSomething");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.STRING) //
                .computedId("0003") //
                .build()));

        List<DataSetRow> rows = Arrays.asList(new DataSetRow(rowMetadata, firstRowValues), //
                new DataSetRow(rowMetadata, secondRowValues));

        // when
        ActionTestWorkbench.test(rows, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(rows.get(0).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0001", "David Bowie"), //
                        MapEntry.entry("0002", "N"), //
                        MapEntry.entry("0003", ""));

        Assertions.assertThat(rows.get(1).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0001", "Beer"), //
                        MapEntry.entry("0002", "T"), //
                        MapEntry.entry("0003", "NotSomething"));
    }

    @Test
    public void should_clear_because_pattern_match() throws Exception {
        // given
        final Map<String, String> firstRowValues = new HashMap<>();
        firstRowValues.put("0001", "David Bowie");
        firstRowValues.put("0002", "N");
        firstRowValues.put("0003", "Something");

        final Map<String, String> secondRowValues = new HashMap<>();
        secondRowValues.put("0001", "Beer");
        secondRowValues.put("0002", "T");
        secondRowValues.put("0003", "NotSomething");

        final Map<String, String> thirdRowValues = new HashMap<>();
        thirdRowValues.put("0001", "Wine");
        thirdRowValues.put("0002", "True");
        thirdRowValues.put("0003", "Somethin");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.STRING) //
                .computedId("0003") //
                .build()));

        List<DataSetRow> rows = Arrays.asList(new DataSetRow(rowMetadata, firstRowValues), //
                new DataSetRow(rowMetadata, secondRowValues), //
                new DataSetRow(rowMetadata, thirdRowValues));


        parameters.put( ClearMatching.VALUE_PARAMETER, generateJson( ".*Something", ReplaceOnValueHelper.REGEX_MODE));


        // when
        ActionTestWorkbench.test(rows, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(rows.get(0).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0001", "David Bowie"), //
                        MapEntry.entry("0002", "N"), //
                        MapEntry.entry("0003", ""));

        Assertions.assertThat(rows.get(1).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0001", "Beer"), //
                        MapEntry.entry("0002", "T"), //
                        MapEntry.entry("0003", ""));

        Assertions.assertThat(rows.get(2).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0001", "Wine"), //
                        MapEntry.entry("0002", "True"), //
                        MapEntry.entry("0003", "Somethin"));

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

        parameters.put( ClearMatching.VALUE_PARAMETER, generateJson( "Badibada", ReplaceOnValueHelper.REGEX_MODE));

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

        parameters.put( ClearMatching.VALUE_PARAMETER, generateJson( "true", ReplaceOnValueHelper.EQUALS_IGNORE_CASE_MODE));

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


        parameters.put( ClearMatching.VALUE_PARAMETER,
                        generateJson(Boolean.FALSE.toString(), ReplaceOnValueHelper.EQUALS_IGNORE_CASE_MODE));

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

        parameters.put( ClearMatching.VALUE_PARAMETER, generateJson( "tchoubidoo", ReplaceOnValueHelper.EQUALS_IGNORE_CASE_MODE));

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
    public void should_clear_because_pattern_match_integer() throws Exception {
        // given
        final Map<String, String> firstRowValues = new HashMap<>();
        firstRowValues.put("0001", "David Bowie");
        firstRowValues.put("0002", "N");
        firstRowValues.put("0003", "123");

        final Map<String, String> secondRowValues = new HashMap<>();
        secondRowValues.put("0001", "Beer");
        secondRowValues.put("0002", "T");
        secondRowValues.put("0003", "1234");

        final Map<String, String> thirdRowValues = new HashMap<>();
        thirdRowValues.put("0001", "Wine");
        thirdRowValues.put("0002", "True");
        thirdRowValues.put("0003", "01234");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                                                             .type(Type.INTEGER) //
                                                             .computedId("0003") //
                                                             .build()));

        List<DataSetRow> rows = Arrays.asList(new DataSetRow(rowMetadata, firstRowValues), //
                                              new DataSetRow(rowMetadata, secondRowValues), //
                                              new DataSetRow(rowMetadata, thirdRowValues));

        parameters.put( ClearMatching.VALUE_PARAMETER, generateJson( ".*1234", ReplaceOnValueHelper.REGEX_MODE));

        // when
        ActionTestWorkbench.test(rows, action.create(parameters).getRowAction());

        // then
        Assertions.assertThat(rows.get(0).values()) //
            .isNotEmpty() //
            .hasSize(3) //
            .containsExactly(MapEntry.entry("0001", "David Bowie"), //
                             MapEntry.entry("0002", "N"), //
                             MapEntry.entry("0003", "123"));

        Assertions.assertThat(rows.get(1).values()) //
            .isNotEmpty() //
            .hasSize(3) //
            .containsExactly(MapEntry.entry("0001", "Beer"), //
                             MapEntry.entry("0002", "T"), //
                             MapEntry.entry("0003", ""));

        Assertions.assertThat(rows.get(2).values()) //
            .isNotEmpty() //
            .hasSize(3) //
            .containsExactly(MapEntry.entry("0001", "Wine"), //
                             MapEntry.entry("0002", "True"), //
                             MapEntry.entry("0003", ""));

    }

    @Test
    public void should_accept_column() {
        for (Type type : Type.values()) {
            assertTrue(action.acceptColumn(getColumn(type)));
        }
    }

}
