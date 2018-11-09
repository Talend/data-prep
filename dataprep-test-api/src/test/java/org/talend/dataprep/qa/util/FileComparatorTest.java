package org.talend.dataprep.qa.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class FileComparatorTest {

    private static final String TEST_KEY = "testKey";

    private static final String FILE_4L3C = "/data/unitTests/4L3C.csv";

    private static final String FILE_10L3C_BASE = "/data/unitTests/10L3C_base.csv";

    private static final String FILE_10L3C_MIX = "/data/unitTests/10L3C_mix.csv";

    private Map<String, Integer> map;

    @Before
    public void setup() {
        map = new HashMap<>();
    }

    @Test
    public void putOrIncrementTestNewKey() {
        FileComparator.putOrIncrement(map, TEST_KEY);
        assertTrue(map.containsKey(TEST_KEY));
        assertTrue(map.get(TEST_KEY).equals(1));
    }

    @Test
    public void putOrIncrementTestExistingKey() {
        map.put(TEST_KEY, 5);
        FileComparator.putOrIncrement(map, TEST_KEY);
        assertTrue(map.containsKey(TEST_KEY));
        assertEquals(6, (int) map.get(TEST_KEY));
    }

    @Test
    public void removeOrDecrementTestNewKey() {
        boolean result = FileComparator.removeOrDecrement(map, TEST_KEY);
        assertFalse(result);
    }

    @Test
    public void removeOrDecrementTestExistingKeyDecrement() {
        map.put(TEST_KEY, 5);
        boolean result = FileComparator.removeOrDecrement(map, TEST_KEY);
        assertEquals(4, (int) map.get(TEST_KEY));
        assertTrue(result);
    }

    @Test
    public void removeOrDecrementTestExistingKeyDeletion() {
        map.put(TEST_KEY, 1);
        boolean result = FileComparator.removeOrDecrement(map, TEST_KEY);
        assertFalse(map.containsKey(TEST_KEY));
        assertTrue(result);
    }

    @Test
    public void doesFilesContainSameLinesTestSameFile() throws IOException {
        try (InputStream inputStream1 = FileComparatorTest.class.getResourceAsStream(FILE_10L3C_BASE); //
                InputStream inputStream2 = FileComparatorTest.class.getResourceAsStream(FILE_10L3C_BASE)) {
            boolean result = FileComparator.doesFilesContainSameLines(inputStream2, inputStream1);
            assertTrue(result);
        }
    }

    @Test
    public void doesFilesContainSameLinesTestMixFileOrder1() throws IOException {
        try (InputStream inputStream1 = FileComparatorTest.class.getResourceAsStream(FILE_10L3C_BASE); //
                InputStream inputStream2 = FileComparatorTest.class.getResourceAsStream(FILE_10L3C_MIX)) {
            boolean result = FileComparator.doesFilesContainSameLines(inputStream2, inputStream1);
            assertTrue(result);
        }
    }

    @Test
    public void doesFilesContainSameLinesTestMixFileOrder2() throws IOException {
        try (InputStream inputStream1 = FileComparatorTest.class.getResourceAsStream(FILE_10L3C_MIX); //
                InputStream inputStream2 = FileComparatorTest.class.getResourceAsStream(FILE_10L3C_BASE)) {
            boolean result = FileComparator.doesFilesContainSameLines(inputStream2, inputStream1);
            assertTrue(result);
        }
    }

    @Test
    public void doesFilesContainSameLinesTestMissingLineFile() throws IOException {
        try (InputStream inputStream1 = FileComparatorTest.class.getResourceAsStream(FILE_10L3C_BASE); //
                InputStream inputStream2 = FileComparatorTest.class.getResourceAsStream(FILE_4L3C)) {
            boolean result = FileComparator.doesFilesContainSameLines(inputStream2, inputStream1);
            assertFalse(result);
        }
    }
}
