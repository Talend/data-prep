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

package org.talend.dataprep.transformation.api.action.metadata.datablending;

import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters.COLUMN_ID;
import static org.talend.dataprep.transformation.api.action.metadata.datablending.Lookup.Parameters.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.test.MockTestApplication;
import org.talend.dataprep.test.ServerConfiguration;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionFactory;
import org.talend.dataprep.parameters.Parameter;

/**
 * Unit test for the Lookup action.
 * 
 * @see Lookup
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MockTestApplication.class, ServerConfiguration.class })
@WebAppConfiguration
@IntegrationTest({ "server.port=0" })
public class LookupTest {

    @Value("${local.server.port}")
    public int port;

    @Autowired
    ActionFactory factory;

    /** Spring application context. */
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSetMetadataBuilder metadataBuilder;

    @Autowired
    protected ConfigurableEnvironment environment;

    /** DataPrep ready jackson builder. */
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    /** The action to test. */
    private Lookup action;

    @Before
    public void setUp() {
        // Overrides connection information with random port value
        MockPropertySource connectionInformation = new MockPropertySource() //
                .withProperty("dataset.service.url", "http://localhost:" + port) //
                .withProperty("preparation.service.url", "http://localhost:" + port) //
                .withProperty("transformation.service.url", "http://localhost:" + port);
        environment.getPropertySources().addFirst(connectionInformation);
        action = applicationContext.getBean(Lookup.class);
    }

    @Test
    public void testAccept() throws Exception {
        assertFalse(action.acceptColumn(new ColumnMetadata()));
    }

    @Test
    public void testName() {
        assertEquals("lookup", action.getName());
    }

    @Test
    public void testCategory() {
        assertEquals("data_blending", action.getCategory());
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
        final List<Parameter> parameters = action.getParameters();

        // then
        Assertions.assertThat( parameters ) //
            .isNotEmpty() //
            .hasSize( expectedParametersName.size() );
        parameters.forEach(p -> assertTrue(expectedParametersName.contains(p.getName())));

    }

    @Test
    public void shouldAdapt() {
        // given
        final DataSetMetadata ds = metadataBuilder.metadata().name("great dataset").id("ds#123").build();

        // when
        final Lookup actual = action.adapt(ds);

        // when
        final List<Parameter> parameters = actual.getParameters();
        assertEquals("great dataset", getParamValue(parameters, "lookup_ds_name"));
        assertEquals("ds#123", getParamValue(parameters, "lookup_ds_id"));
    }

    @Test
    public void shouldMerge() throws IOException {
        // given
        Map<String, String> parameters = getUsStatesLookupParameters("us_states");
        DataSetRow row = ActionMetadataTestUtils.getRow("Atlanta", "GA", "Philips Arena");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
                "[{\"id\":\"0001\", \"name\":\"Team\"}, {\"id\":\"0004\", \"Stadium\":\"toto\"}, {\"id\":\"0006\", \"name\":\"Coordinates\"}]");
        parameters.put(LOOKUP_JOIN_ON.getKey(), "0003");
        DataSetRow row = ActionMetadataTestUtils.getRow("Dallas", "TX");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then (check values)
        DataSetRow expected = ActionMetadataTestUtils.getRow("Dallas", "TX", "Dallas Mavericks", "American Airlines Center",
                "32.790556°N 96.810278°W");
        Assert.assertEquals(expected, row);

        // and (check metadata)
        checkMergedMetadata(row.getRowMetadata().getById("0002"), "Team", "string", "");
        checkMergedMetadata(row.getRowMetadata().getById("0003"), "Arena", "string", "");
        checkMergedMetadata(row.getRowMetadata().getById("0004"), "Coordinates", "string", "");
    }

    @Test
    public void shouldMergeEmptyCellsOnMissingLookupData() throws IOException {
        // given
        Map<String, String> parameters = getUsStatesLookupParameters("us_states");
        DataSetRow row = ActionMetadataTestUtils.getRow("Toronto", "ON", "Air Canada Centre");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

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
        Map<String, String> parameters = getUsStatesLookupParameters("us_states");
        DataSetRow[] rows = new DataSetRow[] { ActionMetadataTestUtils.getRow("Atlanta", "GA", "Philips Arena"),
                ActionMetadataTestUtils.getRow("Miami", "FL", "American Airlines Arena"),
                ActionMetadataTestUtils.getRow("Chicago", "IL", "United Center"),
                ActionMetadataTestUtils.getRow("San Antonio", "TX", "AT&T Center"),
                ActionMetadataTestUtils.getRow("Oakland", "CA", "Oracle Arena") };

        // when
        ActionTestWorkbench.test(Arrays.asList(rows), factory.create(action, parameters));

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
}