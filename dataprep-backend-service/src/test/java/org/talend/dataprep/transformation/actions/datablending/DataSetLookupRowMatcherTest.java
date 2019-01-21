package org.talend.dataprep.transformation.actions.datablending;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.util.UnitTestsUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DataSetLookupRowMatcherTest {

    @InjectMocks
    private DataSetLookupRowMatcher matcherMock = new DataSetLookupRowMatcher();

    private DataSetRow row1;

    private DataSetRow row2;

    private DataSetRow row3;

    private Map<String, String> values1;

    private Map<String, String> values2;

    private Map<String, String> values3;

    @Before
    public void setUp() throws Exception {
        values1 = new HashMap<String, String>() {

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
        values2 = new HashMap<String, String>() {

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

        values3 = new HashMap<String, String>() {

            {
                put("0000", "AL");
                put("0001", "ALABAMA");
                put("0002", "Not");
                put("0003", "Cached");
                put("0004", "Additional");
                put("0005", "Information");
                put("0006", "In");
                put("0007", "Map");
            }
        };

        row1 = new DataSetRow(values1);
        row2 = new DataSetRow(values2);
        row3 = new DataSetRow(values3);
    }

    @Test
    public void shouldPartiallyCache() {
        // given
        matcherMock.setJoinOnColumn("0000");
        final LookupSelectedColumnParameter parameter = new LookupSelectedColumnParameter();
        parameter.setId("0001");

        matcherMock.setSelectedColumns(Collections.singletonList(parameter));
        matcherMock.setLookupIterator(Arrays.asList(row1, row2).iterator());

        // when
        final Map<String, String> matchingRow = matcherMock.getMatchingRow("0000", "CA");

        // then
        assertEquals("California", matchingRow.get("0001"));
    }

    @Test
    public void shouldCacheOnceMatchingValues() throws NoSuchFieldException, IllegalAccessException {
        // given
        matcherMock.setJoinOnColumn("0000");
        final LookupSelectedColumnParameter parameter = new LookupSelectedColumnParameter();
        parameter.setId("0001");
        matcherMock.setSelectedColumns(Collections.singletonList(parameter));
        matcherMock.setLookupIterator(Arrays.asList(row1, row2, row3).iterator());

        // Field injection
        Map<String, Map<String, String>> cacheMatchingValues = new HashMap();
        UnitTestsUtil.injectFieldInClass(matcherMock, "cacheMatchingValues", cacheMatchingValues);

        // when
        final Map<String, String> matchingFirstRow = matcherMock.getMatchingRow("0000", "NY");
        final Map<String, String> matchingSecondRowFirstTime = matcherMock.getMatchingRow("0000", "CA");
        final Map<String, String> matchingSecondRowSecondTime = matcherMock.getMatchingRow("0000", "CA");
        final Map<String, String> matchingThirdRowFirstTime = matcherMock.getMatchingRow("0000", "AL");
        final Map<String, String> matchingFirstRowSecondTime = matcherMock.getMatchingRow("0000", "NY");

        // then
        assertEquals(3, cacheMatchingValues.size());
        assertEquals(values1, matchingFirstRow);
        assertEquals(values2, matchingSecondRowFirstTime);
        assertEquals(values2, matchingSecondRowSecondTime);
        assertEquals(values3, matchingThirdRowFirstTime);
        assertEquals(values1, matchingFirstRowSecondTime);
    }

    @Test
    public void shouldCacheOnceNoMatchingValues() throws NoSuchFieldException, IllegalAccessException {
        // given
        matcherMock.setJoinOnColumn("0000");
        final LookupSelectedColumnParameter parameter = new LookupSelectedColumnParameter();
        parameter.setId("0001");

        matcherMock.setSelectedColumns(Collections.singletonList(parameter));
        matcherMock.setLookupIterator(Arrays.asList(row1, row2).iterator());

        Set<String> cacheNoMatchingValues = new HashSet<>();
        UnitTestsUtil.injectFieldInClass(matcherMock, "cacheNoMatchingValues", cacheNoMatchingValues);

        Map<String, String> defaultEmptyRow = new HashMap<>();
        row1.getRowMetadata().getColumns().forEach(column -> {
            defaultEmptyRow.put(column.getId(), EMPTY);
        });
        UnitTestsUtil.injectFieldInClass(matcherMock, "defaultEmptyRow", defaultEmptyRow);

        // when
        final Map<String, String> matchingRowNewValue1 = matcherMock.getMatchingRow("0000", "Test");
        final Map<String, String> matchingRowNewValue2 = matcherMock.getMatchingRow("0000", "Cached");
        final Map<String, String> matchingRowAlreadyCachedValue1 = matcherMock.getMatchingRow("0000", "Test");

        // then
        assertEquals(2, cacheNoMatchingValues.size());
        assertTrue(cacheNoMatchingValues.contains("Test"));
        assertTrue(cacheNoMatchingValues.contains("Cached"));
        assertEquals(defaultEmptyRow, matchingRowNewValue1);
        assertEquals(defaultEmptyRow, matchingRowNewValue2);
        assertEquals(defaultEmptyRow, matchingRowAlreadyCachedValue1);
    }

    @Test
    public void shouldReturnDefaultEmptyRow() throws NoSuchFieldException, IllegalAccessException {
        // given
        UnitTestsUtil.injectFieldInClass(matcherMock, "defaultEmptyDatasetRow", row1);

        // when
        RowMetadata rowMetadata = matcherMock.getRowMetadata();

        // then
        assertEquals(8, rowMetadata.getColumns().size());
    }

    @Test
    public void shouldReturnDefaultEmptyRowOnNullJoinValue() throws NoSuchFieldException, IllegalAccessException {
        // given
        matcherMock.setJoinOnColumn("0000");
        final LookupSelectedColumnParameter parameter = new LookupSelectedColumnParameter();
        parameter.setId("0001");

        matcherMock.setSelectedColumns(Collections.singletonList(parameter));
        matcherMock.setLookupIterator(Arrays.asList(row1, row2).iterator());

        Map<String, String> defaultEmptyRow = new HashMap<>();
        row1.getRowMetadata().getColumns().forEach(column -> {
            defaultEmptyRow.put(column.getId(), EMPTY);
        });
        UnitTestsUtil.injectFieldInClass(matcherMock, "defaultEmptyRow", defaultEmptyRow);

        // when
        final Map<String, String> matchingRow = matcherMock.getMatchingRow("0000", null);

        // then
        assertEquals(defaultEmptyRow, matchingRow);
    }

    @Test
    public void shouldReturnDefaultEmptyRowOnNoMatch() throws NoSuchFieldException, IllegalAccessException {
        // given
        matcherMock.setJoinOnColumn("0000");
        final LookupSelectedColumnParameter parameter = new LookupSelectedColumnParameter();
        parameter.setId("0001");
        matcherMock.setSelectedColumns(Collections.singletonList(parameter));
        matcherMock.setLookupIterator(Arrays.asList(row1, row2).iterator());

        // inject field
        Map<String, String> defaultEmptyRow = new HashMap<>();
        row1.getRowMetadata().getColumns().forEach(column -> {
            defaultEmptyRow.put(column.getId(), EMPTY);
        });
        UnitTestsUtil.injectFieldInClass(matcherMock, "defaultEmptyRow", defaultEmptyRow);

        // when
        final Map<String, String> matchingRowEmpty = matcherMock.getMatchingRow("0000", "");
        final Map<String, String> matchingRowTest = matcherMock.getMatchingRow("0000", "Test");

        // then
        assertEquals(defaultEmptyRow, matchingRowEmpty);
        assertEquals(defaultEmptyRow, matchingRowTest);
    }

    @Test
    public void differentMapShoudBeDisjoint() throws NoSuchFieldException, IllegalAccessException {
        // given
        matcherMock.setJoinOnColumn("0000");
        final LookupSelectedColumnParameter parameter = new LookupSelectedColumnParameter();
        parameter.setId("0001");
        matcherMock.setSelectedColumns(Collections.singletonList(parameter));
        matcherMock.setLookupIterator(Arrays.asList(row1, row2, row3).iterator());

        // Field injection
        Map<String, Map<String, String>> cacheMatchingValues = new HashMap();
        UnitTestsUtil.injectFieldInClass(matcherMock, "cacheMatchingValues", cacheMatchingValues);

        Map<String, String> defaultEmptyRow = new HashMap<>();
        row1.getRowMetadata().getColumns().forEach(column -> {
            defaultEmptyRow.put(column.getId(), EMPTY);
        });
        UnitTestsUtil.injectFieldInClass(matcherMock, "defaultEmptyRow", defaultEmptyRow);

        Set<String> cacheNoMatchingValues = new HashSet<>();
        UnitTestsUtil.injectFieldInClass(matcherMock, "cacheNoMatchingValues", cacheNoMatchingValues);

        // when
        final Map<String, String> matchingFirstRow = matcherMock.getMatchingRow("0000", "NY");
        final Map<String, String> matchingSecondRowFirstTime = matcherMock.getMatchingRow("0000", "CA");
        final Map<String, String> notMatchingFirstValueFirstTime = matcherMock.getMatchingRow("0000", "Test");
        final Map<String, String> matchingSecondRowSecondTime = matcherMock.getMatchingRow("0000", "CA");
        final Map<String, String> notMatchingFirstValueSecondTime = matcherMock.getMatchingRow("0000", "Test");
        final Map<String, String> notMatchingSecondValueFirstTime = matcherMock.getMatchingRow("0000", "Re-Test");
        final Map<String, String> notMatchingRowEmpty = matcherMock.getMatchingRow("0000", "");
        final Map<String, String> matchingThirdRowFirstTime = matcherMock.getMatchingRow("0000", "AL");
        final Map<String, String> notMatchingRowNull = matcherMock.getMatchingRow("0000", null);
        final Map<String, String> matchingFirstRowSecondTime = matcherMock.getMatchingRow("0000", "NY");

        // then
        assertEquals(3, cacheMatchingValues.size());
        assertEquals(3, cacheNoMatchingValues.size());
        assert (Collections.disjoint(cacheMatchingValues.entrySet(), cacheNoMatchingValues));
        assertEquals(values1, matchingFirstRow);
        assertEquals(values2, matchingSecondRowFirstTime);
        assertEquals(defaultEmptyRow, notMatchingFirstValueFirstTime);
        assertEquals(values2, matchingSecondRowSecondTime);
        assertEquals(defaultEmptyRow, notMatchingFirstValueSecondTime);
        assertEquals(defaultEmptyRow, notMatchingSecondValueFirstTime);
        assertEquals(defaultEmptyRow, notMatchingRowEmpty);
        assertEquals(values3, matchingThirdRowFirstTime);
        assertEquals(defaultEmptyRow, notMatchingRowNull);
        assertEquals(values1, matchingFirstRowSecondTime);
    }
}
