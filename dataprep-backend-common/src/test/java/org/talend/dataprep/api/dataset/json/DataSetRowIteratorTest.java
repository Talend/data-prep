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

package org.talend.dataprep.api.dataset.json;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * Unit test for the DataSetRowIterator.
 * 
 * @see DataSetRowIterator
 */
public class DataSetRowIteratorTest {

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
}
