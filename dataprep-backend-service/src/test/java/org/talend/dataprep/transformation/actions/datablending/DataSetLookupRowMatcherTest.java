package org.talend.dataprep.transformation.actions.datablending;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.row.DataSetRow;

@RunWith(MockitoJUnitRunner.class)
public class DataSetLookupRowMatcherTest {

    @InjectMocks
    private DataSetLookupRowMatcher matcher = new DataSetLookupRowMatcher();

    @Test
    public void shouldPartiallyCache() {
        // given
        matcher.setJoinOnColumn("0000");
        final LookupSelectedColumnParameter parameter = new LookupSelectedColumnParameter();
        parameter.setId("0001");
        matcher.setSelectedColumns(Collections.singletonList(parameter));

        final Map<String, String> values1 = new HashMap<String, String>() {

            {
                put("0000", "NY");
                put("0001", "New York");
                put("0002", "Additional");
                put("0003", "Information");
                put("0004", "Not");
                put("0005", "To");
                put("0006", "Be");
                put("0007", "Cached");
            }
        };
        final Map<String, String> values2 = new HashMap<String, String>() {

            {
                put("0000", "CA");
                put("0001", "California");
                put("0002", "Additional");
                put("0003", "Information");
                put("0004", "Not");
                put("0005", "To");
                put("0006", "Be");
                put("0007", "Cached");
            }
        };
        final DataSetRow row1 = new DataSetRow(values1);
        final DataSetRow row2 = new DataSetRow(values2);
        matcher.setLookupIterator(Arrays.asList(row1, row2).iterator());

        // when
        final DataSetRow matchingRow = matcher.getMatchingRow("0000", "CA");

        // then
        assertEquals(1, matchingRow.values().size());
        assertEquals("California", matchingRow.get("0001"));
    }
}