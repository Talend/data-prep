// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.datablending;

import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.COLUMN_ID;
import static org.talend.dataprep.transformation.actions.datablending.Lookup.Parameters.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the Lookup action.
 *
 * @see Lookup
 */
public class LookupTest extends AbstractMetadataBaseTest<Lookup> {

    public LookupTest() {
        super(new Lookup());
    }

    @Test
    public void testAccept() throws Exception {
        assertFalse(action.acceptField(new ColumnMetadata()));
    }

    @Test
    public void testName() {
        assertEquals("lookup", action.getName());
    }

    @Test
    public void testCategory() {
        assertEquals("data blending", action.getCategory(Locale.US));
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

    @Test
    public void testParameters() {
        // given
        final List<String> expectedParametersName = Arrays.asList( //
                "column_id", //
                "filter", //
                "lookup_ds_name", //
                "lookup_ds_id", //
                "lookup_join_on", //
                "lookup_join_on_name", //
                "lookup_selected_cols");

        // when
        final List<Parameter> parameters = action.getParameters(Locale.US);

        // then
        Assertions.assertThat(parameters) //
                .isNotEmpty() //
                .hasSize(expectedParametersName.size());
        parameters.forEach(p -> assertTrue(expectedParametersName.contains(p.getName())));

    }

    @Test
    public void shouldAdapt() {
        // given
        final DataSetMetadata ds = new DataSetMetadata("ds#123", "great dataset", "", 0, 0, new RowMetadata(), "1.0.0");

        // when
        final Lookup actual = action.adapt(ds);

        // when
        final List<Parameter> parameters = actual.getParameters(Locale.US);
        assertEquals("great dataset", getParamValue(parameters, "lookup_ds_name"));
        assertEquals("ds#123", getParamValue(parameters, "lookup_ds_id"));
    }

    @Test
    public void shouldMerge() throws IOException {
        // given
        Map<String, String> parameters = getUsStatesLookupParameters("us_states");
        DataSetRow row = ActionMetadataTestUtils.getRow("Atlanta", "GA", "Philips Arena");
        cacheUsStates();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then (check values)
        DataSetRow expected = ActionMetadataTestUtils.getRow("Atlanta", "GA", "Philips Arena", "Georgia", "Atlanta");
        Assert.assertEquals(expected, row);

        // and (check metadata)
        checkMergedMetadata(row.getRowMetadata().getById("0003"), "State", "string", "US_STATE");
        checkMergedMetadata(row.getRowMetadata().getById("0004"), "Capital", "string", "CITY");
    }

    @Test
    public void shouldMergeMultipleColumns() throws IOException {
        // given
        Map<String, String> parameters = getUsStatesLookupParameters("nba");
        parameters.put(LOOKUP_SELECTED_COLS.getKey(),
                "[{\"id\":\"0001\", \"name\":\"Team\"}, {\"id\":\"0004\", \"name\":\"Stadium\"}, {\"id\":\"0006\", \"name\":\"Coordinates\"}]");
        parameters.put(LOOKUP_JOIN_ON.getKey(), "0003");
        DataSetRow row = ActionMetadataTestUtils.getRow("Dallas", "TX");
        cacheNBA();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then (check values)
        DataSetRow expected = ActionMetadataTestUtils.getRow("Dallas", "TX", "Dallas Mavericks", "American Airlines Center",
                "32.790556°N 96.810278°W");
        Assert.assertEquals(expected, row);

        // and (check metadata)
        checkMergedMetadata(row.getRowMetadata().getById("0002"), "Team", "string", "");
        checkMergedMetadata(row.getRowMetadata().getById("0003"), "Stadium", "string", "");
        checkMergedMetadata(row.getRowMetadata().getById("0004"), "Coordinates", "string", "");
    }

    @Test
    public void shouldMergeEmptyCellsOnMissingLookupData() throws IOException {
        // given
        Map<String, String> parameters = getUsStatesLookupParameters("us_states");
        DataSetRow row = ActionMetadataTestUtils.getRow("Toronto", "ON", "Air Canada Centre");
        cacheUsStates();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then (value)
        DataSetRow expected = ActionMetadataTestUtils.getRow("Toronto", "ON", "Air Canada Centre", "", "");
        Assert.assertEquals(expected, row);

        // and (metadata)
        checkMergedMetadata(row.getRowMetadata().getById("0003"), "State", "string", "US_STATE");
        checkMergedMetadata(row.getRowMetadata().getById("0004"), "Capital", "string", "CITY");

    }

    @Test
    public void shouldMergeEmptyCellsOnMissingSourceData() throws IOException {
        // given
        Map<String, String> parameters = getUsStatesLookupParameters("us_states");
        DataSetRow row = ActionMetadataTestUtils.getRow("Huntington", "", "");
        cacheUsStates();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then (value)
        DataSetRow expected = ActionMetadataTestUtils.getRow("Huntington", "", "", "", "");
        Assert.assertEquals(expected, row);

        // and (metadata)
        checkMergedMetadata(row.getRowMetadata().getById("0003"), "State", "string", "US_STATE");
        checkMergedMetadata(row.getRowMetadata().getById("0004"), "Capital", "string", "CITY");
    }

    @Test
    public void shouldMergeSeveralRows() throws IOException {
        // given
        cacheUsStates();
        Map<String, String> parameters = getUsStatesLookupParameters("us_states");
        DataSetRow[] rows = new DataSetRow[] { ActionMetadataTestUtils.getRow("Atlanta", "GA", "Philips Arena"),
                ActionMetadataTestUtils.getRow("Miami", "FL", "American Airlines Arena"),
                ActionMetadataTestUtils.getRow("Chicago", "IL", "United Center"),
                ActionMetadataTestUtils.getRow("San Antonio", "TX", "AT&T Center"),
                ActionMetadataTestUtils.getRow("Oakland", "CA", "Oracle Arena") };

        // when
        ActionTestWorkbench.test(Arrays.asList(rows), actionRegistry, factory.create(action, parameters));

        // then (check values)
        DataSetRow[] expectedRows = new DataSetRow[] {
                ActionMetadataTestUtils.getRow("Atlanta", "GA", "Philips Arena", "Georgia", "Atlanta"),
                ActionMetadataTestUtils.getRow("Miami", "FL", "American Airlines Arena", "Florida", "Tallahassee"),
                ActionMetadataTestUtils.getRow("Chicago", "IL", "United Center", "Illinois", "Springfield"),
                ActionMetadataTestUtils.getRow("San Antonio", "TX", "AT&T Center", "Texas", "Austin"),
                ActionMetadataTestUtils.getRow("Oakland", "CA", "Oracle Arena", "California", "Sacramento") };
        for (int i = 0; i < rows.length; i++) {
            Assert.assertEquals(expectedRows[i], rows[i]);
        }
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

    /**
     * Check that the given merged column match the given parameters.
     *
     * @param mergedColumn the column merged by the lookup action
     * @param name the expected column name.
     * @param type the expected column type.
     * @param domain the expected column domain.
     */
    private void checkMergedMetadata(ColumnMetadata mergedColumn, String name, String type, String domain) {
        assertEquals(name, mergedColumn.getName());
        assertEquals(type, mergedColumn.getType());
        assertEquals(domain, mergedColumn.getDomain());
    }

    /**
     * @return the wanted parameter value or null.
     */
    private String getParamValue(List<Parameter> parameters, String name) {
        return parameters.stream() //
                .filter(p -> StringUtils.equals(p.getName(), name)) //
                .map(Parameter::getDefault) //
                .findFirst() //
                .get();
    }

    /**
     * @return US States specific looup parameters.
     */
    private Map<String, String> getUsStatesLookupParameters(String lookup) throws IOException {
        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("lookupAction.json"));
        parameters.put(LOOKUP_DS_ID.getKey(), lookup);
        parameters.put(LOOKUP_JOIN_ON.getKey(), "0000");
        parameters.put(COLUMN_ID.getKey(), "0001");
        return parameters;
    }

    private void cacheUsStates() {
        LightweightExportableDataSet usStates = new LightweightExportableDataSet();

        ColumnMetadata[] columnArrays = {
                ColumnMetadata.Builder.column().name("Postal").domain("US_STATE").type(Type.STRING).build(),
                ColumnMetadata.Builder.column().name("State").domain("US_STATE").type(Type.STRING).build(),
                ColumnMetadata.Builder.column().name("Capital").domain("CITY").type(Type.STRING).build() };
        List<ColumnMetadata> columns = Arrays.stream(columnArrays).collect(Collectors.toList());
        usStates.setMetadata(new RowMetadata(columns));

        Map<String, String>[] rows = new HashMap[5];
        rows[0] = getValuesMap("GA", "Georgia", "Atlanta");
        rows[1] = getValuesMap("FL", "Florida", "Tallahassee");
        rows[2] = getValuesMap("IL", "Illinois", "Springfield");
        rows[3] = getValuesMap("TX", "Texas", "Austin");
        rows[4] = getValuesMap("CA", "California", "Sacramento");

        Arrays.stream(rows).forEach(r -> usStates.addRecord((String) r.get("0000"), r));

        LookupDatasetsManager.put("us_states", usStates);
    }

    private void cacheNBA() {
        LightweightExportableDataSet usStates = new LightweightExportableDataSet();
        Map<String, String> values = getValuesMap("Southwest", "Dallas Mavericks", "Dallas", "TX", "American Airlines Center", "",
                "32.790556°N 96.810278°W");
        DataSetRow row = new DataSetRow(values);
        row.getRowMetadata().getColumns().get(1).setName("Team");
        row.getRowMetadata().getColumns().get(4).setName("Stadium");
        row.getRowMetadata().getColumns().get(6).setName("Coordinates");

        usStates.setMetadata(row.getRowMetadata());
        usStates.addRecord("TX", values);

        LookupDatasetsManager.put("nba", usStates);
    }

    public static Map<String, String> getValuesMap(String... values) {
        DecimalFormat format = new DecimalFormat("0000");
        Map<String, String> map = new HashMap<>();
        IntStream.range(0, values.length).forEach(i -> map.put(format.format(i), values[i]));
        return map;
    }

}
