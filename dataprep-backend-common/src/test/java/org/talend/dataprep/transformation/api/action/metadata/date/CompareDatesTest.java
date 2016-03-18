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
package org.talend.dataprep.transformation.api.action.metadata.date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;

/**
 * Test class for CompareDates action. Creates one consumer, and test it.
 *
 * @see ComputeTimeSince
 */
public class CompareDatesTest extends BaseDateTests {

    /** The action to test. */
    @Autowired
    private CompareDates action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        final InputStream json = ComputeTimeSince.class.getResourceAsStream("compareDatesAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(json);
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATE.getDisplayName()));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void simple_greater_result_with_constant() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "02/01/2012");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        final DataSetRow row = new DataSetRow(rowMetadata, values);
        parameters.put(CompareDates.CONSTANT_VALUE, "02/21/2008");

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.GT);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(2) //
                .containsExactly(MapEntry.entry("0000", "02/01/2012"), //
                        MapEntry.entry("0001", "true"));

    }

    @Test
    public void simple_equals_result_with_constant() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "02/01/2012");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        final DataSetRow row = new DataSetRow(rowMetadata, values);
        parameters.put(CompareDates.CONSTANT_VALUE, "02/01/2012");

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.EQ);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(2) //
                .containsExactly(MapEntry.entry("0000", "02/01/2012"), //
                        MapEntry.entry("0001", "true"));

    }

    @Test
    public void simple_not_greater_result_with_constant() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "02/01/2012");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        final DataSetRow row = new DataSetRow(rowMetadata, values);
        parameters.put(CompareDates.CONSTANT_VALUE, "02/02/2012");

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.GT);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(2) //
                .containsExactly(MapEntry.entry("0000", "02/01/2012"), //
                        MapEntry.entry("0001", "false"));

    }

    @Test
    public void simple_greater_result_with_column() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "02/01/2012");
        values.put("0001", "02/28/1973");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        rowMetadata.addColumn(createMetadata("0001", "first update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.GT);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "02/01/2012"), //
                        MapEntry.entry("0001", "02/28/1973"), //
                        MapEntry.entry("0002", "true"));

    }

    @Test
    public void simple_equals_result_with_column() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "02/01/2012");
        values.put("0001", "02/01/2012");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        rowMetadata.addColumn(createMetadata("0001", "first update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.EQ);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "02/01/2012"), //
                        MapEntry.entry("0001", "02/01/2012"), //
                        MapEntry.entry("0002", "true"));

    }

    @Test
    public void simple_not_greater_result_with_column() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "02/01/2012");
        values.put("0001", "02/02/2012");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        rowMetadata.addColumn(createMetadata("0001", "first update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.GT);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "02/01/2012"), //
                        MapEntry.entry("0001", "02/02/2012"), //
                        MapEntry.entry("0002", "false"));

    }

    @Test
    public void simple_greater_result_with_constant_with_invalid_dates_row() throws Exception {

        // given
        final Map<String, String> firstRowvalues = new HashMap<>();
        firstRowvalues.put("0000", "02/01/2012");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        parameters.put(CompareDates.CONSTANT_VALUE, "02/21/2008");

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.GT);


        final Map<String, String> secondRowvalues = new HashMap<>();
        secondRowvalues.put("0000", "Beer");

        final Map<String, String> thirdRowvalues = new HashMap<>();
        thirdRowvalues.put("0000", "02/01/2001");

        List<DataSetRow> rows = Arrays.asList(new DataSetRow(rowMetadata, firstRowvalues), //
                new DataSetRow(rowMetadata, secondRowvalues), //
                new DataSetRow(rowMetadata, thirdRowvalues));

        // when
        ActionTestWorkbench.test( rows, factory.create(action, parameters));

        // then
        Assertions.assertThat(rows.get( 0 ).values()) //
            .hasSize(2) //
            .containsExactly(MapEntry.entry("0000", "02/01/2012"), //
                             MapEntry.entry("0001", "true"));

        Assertions.assertThat(rows.get( 1 ).values()) //
            .hasSize(2) //
            .containsExactly(MapEntry.entry("0000", "Beer"), //
                             MapEntry.entry("0001", ""));

        Assertions.assertThat(rows.get( 2 ).values()) //
            .hasSize(2) //
            .containsExactly(MapEntry.entry("0000", "02/01/2001"), //
                             MapEntry.entry("0001", "false"));

    }

    @Test
    public void compare_date_with_empty_should_have_empty_result() throws Exception{
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "02/01/2012");
        values.put("0001", "");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        rowMetadata.addColumn(createMetadata("0001", "first update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.EQ);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", "02/01/2012"), //
                        MapEntry.entry("0001", ""), //
                        MapEntry.entry("0002", ""));
    }

    @Test
    public void compare_empty_with_date_should_have_empty_result() throws Exception{
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "");
        values.put("0001", "02/01/2012");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        rowMetadata.addColumn(createMetadata("0001", "first update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.EQ);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", ""), //
                        MapEntry.entry("0001", "02/01/2012"), //
                        MapEntry.entry("0002", ""));
    }

    @Test
    public void compare_empty_with_empty_should_have_empty_result() throws Exception{
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "");
        values.put("0001", "");

        RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(createMetadata("0000", "last update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        rowMetadata.addColumn(createMetadata("0001", "first update", Type.DATE, "statistics_MM_dd_yyyy.json"));
        final DataSetRow row = new DataSetRow(rowMetadata, values);

        parameters.put(CompareDates.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        parameters.put(CompareDates.COMPARE_MODE, CompareDates.EQ);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assertions.assertThat(row.values()) //
                .hasSize(3) //
                .containsExactly(MapEntry.entry("0000", ""), //
                        MapEntry.entry("0001", ""), //
                        MapEntry.entry("0002", ""));
    }

}
