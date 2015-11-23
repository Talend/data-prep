package org.talend.dataprep.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Unit test for the LineBasedFormatGuesser.
 * 
 * @see LineBasedFormatGuesser
 */
public class LineBasedFormatGuesserTest extends AbstractSchemaTestUtils {

    /** The format guesser to test. */
    @Autowired
    LineBasedFormatGuesser guesser;

    /**
     * Text file
     */
    @Test
    public void should_not_guess() throws IOException {
        FormatGuesser.Result actual = guesser.guess(getRequest(new ByteArrayInputStream(new byte[0]), "#1"), "UTF-8");
        Assert.assertNotNull(actual);
        assertTrue(actual.getFormatGuess() instanceof UnsupportedFormatGuess);
    }


    @Test(expected = IllegalArgumentException.class)
    public void read_null_csv_file() throws Exception {
        guesser.guess(null, "UTF-8").getFormatGuess();
    }

    /**
     * Standard csv file.
     */
    @Test
    public void should_guess_CSV() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("standard.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#2"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormatGuess() instanceof CSVFormatGuess);
        }
    }

    /**
     * csv file with 2 possible separators : ';' or '/', ';' should be selected
     */
    @Test
    public void should_guess_best_separator() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("mixed_separators.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#3"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormatGuess() instanceof CSVFormatGuess);
            char separator = actual.getParameters().get(CSVFormatGuess.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(separator, ';');
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-181
     */
    @Test
    public void should_guess_best_separator_out_of_two() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-181.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#4"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormatGuess() instanceof CSVFormatGuess);
            char separator = actual.getParameters().get(CSVFormatGuess.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(separator, ';');
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-258
     */
    @Test
    public void should_guess_separator_with_ISO_8859_1_encoded_file() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("iso-8859-1.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#5"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormatGuess() instanceof CSVFormatGuess);
            char separator = actual.getParameters().get(CSVFormatGuess.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(separator, ';');
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-863
     */
    @Test
    public void should_guess_valid_separator_when_most_likely_separator_is_not_valid() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-863.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#6"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormatGuess() instanceof CSVFormatGuess);
            char separator = actual.getParameters().get(CSVFormatGuess.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', separator);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-832
     */
    @Test
    public void should_guess_valid_separator_from_access_log_file() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-832.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#7"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormatGuess() instanceof CSVFormatGuess);
            char separator = actual.getParameters().get(CSVFormatGuess.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(' ', separator);
        }
    }

    @Test
    public void should_not_detect_char_or_digit_separator_candidate() {
        Map<Character, Separator> separatorMap = new HashMap<>();
        char[] cases = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        for (char candidate : cases) {
            guesser.processCharAsSeparatorCandidate(candidate, separatorMap, 0);
        }
        assertTrue(separatorMap.isEmpty());
    }

    @Test
    public void shouldComputeScore() {
        // given
        Separator sep = new Separator('s');
        incrementCount(12, 1, sep);
        incrementCount(10, 2, sep);
        // nothing on the third line
        incrementCount(11, 4, sep);
        incrementCount(13, 5, sep);
        incrementCount(12, 6, sep);

        // when
        guesser.computeScore(sep, 6);

        // then
        assertEquals(9.66, sep.getAveragePerLine(), 0.01);
        assertEquals(6.27, sep.getStandardDeviation(), 0.01); // https://www.mathsisfun.com/data/standard-deviation-calculator.html
    }

    private void incrementCount(int count, int lineNumber, Separator separator) {
        for (int i = 0; i < count; i++) {
            separator.incrementCount(lineNumber);
        }
    }

}