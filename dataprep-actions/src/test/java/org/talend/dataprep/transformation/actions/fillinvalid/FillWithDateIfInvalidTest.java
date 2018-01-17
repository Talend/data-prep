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

package org.talend.dataprep.transformation.actions.fillinvalid;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.date.ChangeDatePatternTest;
import org.talend.dataprep.transformation.actions.fill.FillInvalid;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for FillWithDateIfInvalid action.
 *
 * @see FillInvalid
 */
public class FillWithDateIfInvalidTest extends AbstractMetadataBaseTest<FillInvalid> {

    public FillWithDateIfInvalidTest() {
        super(new FillInvalid());
    }

    @PostConstruct
    public void init() {
        action = (FillInvalid) action.adapt(ColumnMetadata.Builder.column().type(Type.DATE).build());
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.NA;
    }

    @Test
    public void should_fill_non_valid_datetime() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "N");
        values.put("0002", "Something");

        final Statistics statistics = getStatistics(
                this.getClass().getResourceAsStream("fillInvalidDateTimeAction_statistics.json"));

        final DataSetRow row = new DataSetRow(values);
        row.setInvalid("0001");
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.DATE.getName());
        rowMetadata.getById("0001").setStatistics(statistics);

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidDateTimeAction.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("09/07/2015 13:31:36", row.get("0001"));
    }

    @Test
    public void should_not_fill_non_valid_datetime() throws Exception {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "09/07/2015 13:31:35");
        values.put("0002", "Something");

        final Statistics statistics = getStatistics(
                this.getClass().getResourceAsStream("fillInvalidDateTimeAction_statistics.json"));

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.DATE.getName());
        rowMetadata.getById("0001").setStatistics(statistics);

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidDateTimeAction.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("09/07/2015 13:31:35", row.get("0001"));
    }

    @Test
    public void test_TDP_591() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("David Bowie").type(Type.STRING)) //
                .with(value("N").type(Type.DATE).statistics(ChangeDatePatternTest.class.getResourceAsStream("statistics_yyyy-MM-dd.json"))) //
                .with(value("15/10/1999").type(Type.DATE)) //
                .build();
        row.setInvalid("0001");
        row.setInvalid("0002");
        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidDateTimeAction.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("2015-07-09", row.get("0001"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_adapt_null() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.NEED_STATISTICS_INVALID));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

    public Statistics getStatistics(InputStream source) throws IOException {
        final String statisticsContent = IOUtils.toString(source, UTF_8);
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(statisticsContent, Statistics.class);
    }

}
