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

package org.talend.dataprep.transformation.api.action.metadata.fillinvalid;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.setStatistics;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.date.ChangeDatePatternTest;
import org.talend.dataprep.transformation.api.action.metadata.fill.FillIfEmpty;
import org.talend.dataprep.transformation.api.action.metadata.fill.FillInvalid;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for FillWithDateIfInvalid action.
 * 
 * @see FillInvalid
 */
public class FillWithDateIfInvalidTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private FillInvalid action;

    @PostConstruct
    public void init() {
        action = (FillInvalid) action.adapt(ColumnMetadata.Builder.column().type(Type.DATE).build());
    }

    @Test
    public void should_fill_non_valid_datetime() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "N");
        values.put("0003", "Something");

        final Statistics statistics = getStatistics(
                this.getClass().getResourceAsStream("fillInvalidDateTimeAction_statistics.json"));

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.DATE) //
                .computedId("0002") //
                .invalidValues(newHashSet("N")) //
                .statistics(statistics) //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidDateTimeAction.json"));

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals("09/07/2015 13:31:36", row.get("0002"));
    }

    @Test
    public void should_not_fill_non_valid_datetime() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "09/07/2015 13:31:35");
        values.put("0003", "Something");

        final Statistics statistics = getStatistics(
                this.getClass().getResourceAsStream("fillInvalidDateTimeAction_statistics.json"));

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.DATE) //
                .computedId("0002") //
                .invalidValues(newHashSet("N")) //
                .statistics(statistics) //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidDateTimeAction.json"));

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals("09/07/2015 13:31:35", row.get("0002"));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-457
     */
    @Test
    public void should_fill_non_valid_date_not_in_invalid_values() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "not a date !!!!"); // invalid date
        values.put("0003", "Something");

        final Statistics statistics = getStatistics(this.getClass().getResourceAsStream("fillInvalidDateAction_statistics.json"));

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.DATE) //
                .computedId("0002") //
                .invalidValues(new HashSet<>()) // no invalid values
                .statistics(statistics) //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidDateTimeAction.json"));

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals("07/09/2015", row.get("0002"));

        final Set<String> invalidValues = row.getRowMetadata().getById("0002").getQuality().getInvalidValues();
        assertEquals(1, invalidValues.size());
        assertTrue(invalidValues.contains("not a date !!!!"));
    }

    @Test
    public void test_TDP_591() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "N");
        values.put("0003", "100");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.DATE) //
                .computedId("0002") //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);
        setStatistics(row, "0002", ChangeDatePatternTest.class.getResourceAsStream("statistics_yyyy-MM-dd.json"));

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidDateTimeAction.json"));

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assert.assertEquals("2015-07-09", row.get("0002"));
        Assert.assertEquals("David Bowie", row.get("0001"));
    }

    @Test
    public void should_fill_empty_string_other_column() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "Ouf");
        values.put("0003", "15/10/1999");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(ColumnMetadata.Builder.column().type(Type.DATE).computedId("0002").build());
        rowMetadata.addColumn(ColumnMetadata.Builder.column().type(Type.DATE).computedId("0003").build());

        final DataSetRow row = new DataSetRow(rowMetadata, values);
        setStatistics(row, "0002", ChangeDatePatternTest.class.getResourceAsStream("statistics_yyyy-MM-dd.json"));

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillInvalidDateTimeAction.json"));

        // when
        parameters.put(FillIfEmpty.MODE_PARAMETER, FillIfEmpty.OTHER_COLUMN_MODE);
        parameters.put(FillIfEmpty.SELECTED_COLUMN_PARAMETER, "0003");
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        Assert.assertEquals("15/10/1999", row.get("0003"));
        Assert.assertEquals("1999-10-15", row.get("0002"));
        Assert.assertEquals("David Bowie", row.get("0001"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.ANY)));
    }

    @Test
    public void should_adapt_null() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
    }

    public Statistics getStatistics(InputStream source) throws IOException {
        final String statisticsContent = IOUtils.toString(source);
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(statisticsContent, Statistics.class);
    }

}
