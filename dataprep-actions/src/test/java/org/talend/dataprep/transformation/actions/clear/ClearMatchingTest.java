package org.talend.dataprep.transformation.actions.clear;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ReplaceOnValueHelper;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for ClearMatching action. Creates one consumer, and test it.
 *
 * @see ClearMatching
 */
public class ClearMatchingTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    private ClearMatching action = new ClearMatching();

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
        assertThat(action.getName(), is(ClearMatching.ACTION_NAME));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATA_CLEANSING.getDisplayName()));
    }

    @Test
    public void should_clear_because_equals() throws Exception {
        // given
        final Map<String, String> firstRowValues = new HashMap<>();
        firstRowValues.put("0000", "David Bowie");
        firstRowValues.put("0001", "N");
        firstRowValues.put("0002", "Something");

        final Map<String, String> secondRowValues = new HashMap<>();
        secondRowValues.put("0000", "Beer");
        secondRowValues.put("0001", "T");
        secondRowValues.put("0002", "NotSomething");

        List<DataSetRow> rows = Arrays.asList(new DataSetRow(firstRowValues), //
                new DataSetRow(secondRowValues));
        for (DataSetRow row : rows) {
            final RowMetadata rowMetadata = row.getRowMetadata();
            rowMetadata.getById("0002").setType(Type.STRING.getName());
        }


        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(rows.get(0).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "David Bowie"), //
                        MapEntry.entry("0001", "N"), //
                        MapEntry.entry("0002", ""));

        Assertions.assertThat(rows.get(1).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "Beer"), //
                        MapEntry.entry("0001", "T"), //
                        MapEntry.entry("0002", "NotSomething"));
    }

    @Test
    public void should_clear_because_pattern_match() throws Exception {
        // given
        final Map<String, String> firstRowValues = new HashMap<>();
        firstRowValues.put("0000", "David Bowie");
        firstRowValues.put("0001", "N");
        firstRowValues.put("0002", "Something");

        final Map<String, String> secondRowValues = new HashMap<>();
        secondRowValues.put("0000", "Beer");
        secondRowValues.put("0001", "T");
        secondRowValues.put("0002", "NotSomething");

        final Map<String, String> thirdRowValues = new HashMap<>();
        thirdRowValues.put("0000", "Wine");
        thirdRowValues.put("0001", "True");
        thirdRowValues.put("0002", "Somethin");

        List<DataSetRow> rows = Arrays.asList(new DataSetRow(firstRowValues), //
                new DataSetRow(secondRowValues), //
                new DataSetRow(thirdRowValues));
        for (DataSetRow row : rows) {
            final RowMetadata rowMetadata = row.getRowMetadata();
            rowMetadata.getById("0002").setType(Type.STRING.getName());
        }


        parameters.put(ClearMatching.VALUE_PARAMETER, generateJson(".*Something", ReplaceOnValueHelper.REGEX_MODE));

        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(rows.get(0).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "David Bowie"), //
                        MapEntry.entry("0001", "N"), //
                        MapEntry.entry("0002", ""));

        Assertions.assertThat(rows.get(1).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "Beer"), //
                        MapEntry.entry("0001", "T"), //
                        MapEntry.entry("0002", ""));

        Assertions.assertThat(rows.get(2).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "Wine"), //
                        MapEntry.entry("0001", "True"), //
                        MapEntry.entry("0002", "Somethin"));

    }

    @Test
    public void should_not_clear_because_not_equals() throws Exception {
        // given
        final DataSetRow row = builder() //
                .value("David Bowie", Type.STRING) //
                .value("N", Type.STRING) //
                .value("Something", Type.STRING) //
                .build();
        parameters.put(ClearMatching.VALUE_PARAMETER, generateJson("Badibada", ReplaceOnValueHelper.REGEX_MODE));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "David Bowie"), //
                        MapEntry.entry("0001", "N"), //
                        MapEntry.entry("0002", "Something"));
    }

    @Test
    public void should_clear_boolean_because_equals() throws Exception {
        // given
        final DataSetRow row = builder() //
                .value("David Bowie", Type.STRING) //
                .value("Something", Type.STRING) //
                .value("True", Type.BOOLEAN) //
                .build();
        parameters.put(ClearMatching.VALUE_PARAMETER, Boolean.TRUE.toString());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "David Bowie"), //
                        MapEntry.entry("0001", "Something"), //
                        MapEntry.entry("0002", ""));
    }

    @Test
    public void should_clear_boolean_because_equals_ignore_case() throws Exception {
        // given
        final DataSetRow row = builder() //
                .value("David Bowie", Type.STRING) //
                .value("Something", Type.STRING) //
                .value("False", Type.BOOLEAN) //
                .build();
        parameters.put(ClearMatching.VALUE_PARAMETER, Boolean.FALSE.toString());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "David Bowie"), //
                        MapEntry.entry("0001", "Something"), //
                        MapEntry.entry("0002", ""));
    }

    @Test
    public void should_not_clear_boolean_because_not_equals() throws Exception {
        // given
        final DataSetRow row = builder() //
                .value("David Bowie", Type.STRING) //
                .value("True", Type.STRING) //
                .value("Something", Type.BOOLEAN) //
                .build();
        parameters.put(ClearMatching.VALUE_PARAMETER, "tchoubidoo");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "David Bowie"), //
                        MapEntry.entry("0001", "True"), //
                        MapEntry.entry("0002", "Something"));
    }

    @Test
    public void should_clear_because_pattern_match_integer() throws Exception {
        // given
        final DataSetRow row1 = builder() //
                .value("David Bowie", Type.STRING) //
                .value("N", Type.STRING) //
                .value("123", Type.INTEGER) //
                .build();

        final DataSetRow row2 = builder() //
                .value("Beer", Type.STRING) //
                .value("T", Type.STRING) //
                .value("1234", Type.INTEGER) //
                .build();

        final DataSetRow row3 = builder() //
                .value("Wine", Type.STRING) //
                .value("True", Type.STRING) //
                .value("01234", Type.INTEGER) //
                .build();
        parameters.put(ClearMatching.VALUE_PARAMETER, generateJson(".*1234", ReplaceOnValueHelper.REGEX_MODE));
        List<DataSetRow> rows = Arrays.asList(row1, row2, row3);

        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        Assertions.assertThat(rows.get(0).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "David Bowie"), //
                        MapEntry.entry("0001", "N"), //
                        MapEntry.entry("0002", "123"));

        Assertions.assertThat(rows.get(1).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "Beer"), //
                        MapEntry.entry("0001", "T"), //
                        MapEntry.entry("0002", ""));

        Assertions.assertThat(rows.get(2).values()) //
                .isNotEmpty() //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "Wine"), //
                        MapEntry.entry("0001", "True"), //
                        MapEntry.entry("0002", ""));

    }

    @Test
    public void should_accept_column() {
        for (Type type : Type.values()) {
            assertTrue(action.acceptField(getColumn(type)));
        }
    }

}
