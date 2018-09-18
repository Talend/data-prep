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

package org.talend.dataprep.api.dataset.json;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for the DataSetRowIterator.
 *
 * @see DataSetRowIterator
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSetRowIteratorTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldReadSerializedSalesforceDatasetInCache() throws IOException {

        // given
        DataSet dataSet = from(this.getClass().getResourceAsStream("dataset-sample_cache.json"));
        List<DataSetRow> expectedDatasetRows = dataSet.getRecords().collect(Collectors.toList());

        // when
        final InputStream json = DataSetRowIteratorTest.class.getResourceAsStream("TCOMPContent_cache_serialized");
        List<DataSetRow> actualDatasetRows = new ArrayList<>();

        JsonParser parser = mapper.getFactory().createParser(json);
        final DataSetRowIterator iterator = new DataSetRowIterator(parser, dataSet.getMetadata().getRowMetadata());
        AtomicLong tdpId = new AtomicLong(1);
        while (iterator.hasNext()) {
            DataSetRow datasetRow = iterator.next();
            if (!datasetRow.isEmpty()) {
                datasetRow.setTdpId(tdpId.getAndIncrement());
                actualDatasetRows.add(datasetRow.clone());
            }
        }

        // then
        Assert.assertEquals(expectedDatasetRows.size(), actualDatasetRows.size());
        for (int i = 0; i <= actualDatasetRows.size() - 1; i++) {
            DataSetRow actualDataSetrow = actualDatasetRows.get(i);
            DataSetRow expectedDataSetrow = expectedDatasetRows.get(i);
            Assert.assertEquals(actualDataSetrow.getInternalValues(), expectedDataSetrow.getInternalValues());
            Assert.assertEquals(actualDataSetrow.getRowMetadata().hashCode(),
                    expectedDataSetrow.getRowMetadata().hashCode());
        }
    }

    @Test
    public void should_iterate_row_with_id() throws IOException {

        // given
        List<DataSetRow> expectedRows = new ArrayList<>();
        expectedRows.add(getDataSetRow(2, "Sheriff Woody", "Tom Hanks", "1995–present"));
        expectedRows.add(getDataSetRow(3, "Buzz Lightyear", "", "1995–present"));
        expectedRows.add(getDataSetRow(5, "Mr. Potato Head", "Don Rickles", "1995–present"));

        // when
        final InputStream json = DataSetRowIteratorTest.class.getResourceAsStream("datasetrow.json");
        DataSetRowIterator iterator = new DataSetRowIterator(json);

        List<DataSetRow> actual = new ArrayList<>();
        while (iterator.hasNext()) {
            actual.add(iterator.next().clone());
        }

        // then
        Assert.assertEquals(expectedRows, actual);
    }

    @Test
    public void should_iterate_row_without_id() throws IOException {

        // given
        List<DataSetRow> expectedRows = new ArrayList<>();
        expectedRows.add(getDataSetRow("Sheriff Woody", "Tom Hanks", "1995–present"));
        expectedRows.add(getDataSetRow("Buzz Lightyear", "", "1995–present"));
        expectedRows.add(getDataSetRow("Mr. Potato Head", "Don Rickles", "1995–present"));

        // when
        final InputStream json = DataSetRowIteratorTest.class.getResourceAsStream("datasetrow.json");
        DataSetRowIterator iterator = new DataSetRowIterator(json);

        List<DataSetRow> actual = new ArrayList<>();
        while (iterator.hasNext()) {
            actual.add(iterator.next().clone());
        }

        // then
        Assert.assertNotEquals(expectedRows, actual); // TDP id are read as-is
    }

    private DataSetRow getDataSetRow(final long tdpId, String... data) {
        DataSetRow row = getDataSetRow(data);
        row.setTdpId(tdpId);
        return row;
    }

    private DataSetRow getDataSetRow(String... data) {
        final DecimalFormat format = new DecimalFormat("0000");
        final Map<String, String> values = new HashMap<>();
        for (int i = 0; i < data.length; i++) {
            values.put(format.format(i), data[i]);
        }
        return new DataSetRow(values);
    }

    /**
     * @param json A valid JSON stream, may be <code>null</code>.
     * @return The {@link DataSetMetadata} instance parsed from stream or <code>null</code> if parameter is null. If
     * stream is empty, also returns <code>null</code>.
     */
    public DataSet from(InputStream json) {
        try {
            JsonParser parser = mapper.getFactory().createParser(json);
            return mapper.readerFor(DataSet.class).readValue(parser);

        } catch (IOException e1) {
            e1.printStackTrace();
            return null;

        }
    }
}
