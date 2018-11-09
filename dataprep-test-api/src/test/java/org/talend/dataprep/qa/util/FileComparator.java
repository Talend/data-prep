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

package org.talend.dataprep.qa.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileComparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileComparator.class);

    /**
     * Compare two files in order to check if they contain the same lines (but maybe not in the same order).
     *
     * @param expected the file expected to match the actual one
     * @param actual the actual file
     * @return <code>true</code> if both files contains the same lines, <code>false</code> else.
     * @throws IOException in case of reading file problem.
     */
    public static boolean doesFilesContainSameLines(@NotNull InputStream expected, @NotNull InputStream actual)
            throws IOException {
        LineIterator actualLI = IOUtils.lineIterator(actual, StandardCharsets.UTF_8);
        LineIterator expectedLI = IOUtils.lineIterator(expected, StandardCharsets.UTF_8);
        Map<String, Integer> unmatchedLinesFromActualFile = new HashMap<>();
        Map<String, Integer> unmatchedLinesFromExpectedFile = new HashMap<>();

        while (actualLI.hasNext() || expectedLI.hasNext()) {
            String actualLine = actualLI.hasNext() ? actualLI.nextLine() : null;
            String expectedLine = expectedLI.hasNext() ? expectedLI.nextLine() : null;
            if (actualLine != null && expectedLine != null) {
                if (!actualLine.equals(expectedLine)) {
                    if (!removeOrDecrement(unmatchedLinesFromExpectedFile, actualLine)) {
                        putOrIncrement(unmatchedLinesFromActualFile, actualLine);
                    }
                    if (!removeOrDecrement(unmatchedLinesFromActualFile, expectedLine)) {
                        putOrIncrement(unmatchedLinesFromExpectedFile, expectedLine);
                    }
                }
            } else if (actualLine != null) {
                putOrIncrement(unmatchedLinesFromActualFile, actualLine);
            } else {
                putOrIncrement(unmatchedLinesFromExpectedFile, expectedLine);
            }
        }
        if (unmatchedLinesFromActualFile.isEmpty() && unmatchedLinesFromExpectedFile.isEmpty()) {
            return true;
        }
        if (!unmatchedLinesFromActualFile.isEmpty()) {
            LOGGER.warn("Lines present only in actual file :\n" + getKeys(unmatchedLinesFromActualFile));
        }
        if (!unmatchedLinesFromExpectedFile.isEmpty()) {
            LOGGER.warn("Lines present only in expected file :\n" + getKeys(unmatchedLinesFromExpectedFile));
        }
        return false;
    }

    private static String getKeys(@NotNull Map<String, Integer> map) {
        StringBuilder str = new StringBuilder();
        map.keySet().forEach(k -> {
            str
                    .append(map.get(k)) //
                    .append("\t") //
                    .append(k) //
                    .append("\n");
        });
        return str.toString();
    }

    /**
     * Decrement a value associated with the given key in the given {@link Map}, remove the key if value reach 0.
     *
     * @param map the {@link Map} containing key and value to decrement.
     * @param key the key associated with the value to decrement.
     * @return true if decremented, false if the key doesn't exists in map.
     */
    public static boolean removeOrDecrement(@NotNull Map<String, Integer> map, @NotNull String key) {
        boolean decremented = false;
        Integer value = map.get(key);
        if (value != null) {
            value--;
            if (value == 0) {
                map.remove(key);
            } else {
                map.put(key, value);
            }
            decremented = true;
        }
        return decremented;
    }

    /**
     * Increment a value associated with a the given key in the given {@link Map}, if the key doesn't exist it's added
     * with 1 as value.
     *
     * @param map the {@link Map} where the value is incremented or the key added.
     * @param key the key to increment or to add.
     */
    public static void putOrIncrement(@NotNull Map<String, Integer> map, @NotNull String key) {
        Integer value = map.get(key);
        if (value == null) {
            map.put(key, 1);
        } else {
            map.put(key, value + 1);
        }
    }

}
