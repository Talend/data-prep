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

package org.talend.dataprep.schema.csv;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.schema.FormatGuesser;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.unsupported.UnsupportedFormatGuess;

/**
 * <h3>CSV implementation of the formatGuesser</h3>
 *
 * <p>
 * Read the first 100 lines or 64k chars to check if given dataset is a CSV as well as its potential separator.
 * </p>
 *
 * <p>
 * Separator are chosen out of these rules :
 * <ul>
 * <li>highest average occurrence per line</li>
 * <li>lowest standard deviation (all the lines should have the same number of separators)</li>
 * <li>is known to be an allowed separator (see LineBasedFormatGuesser#validSeparators)</li>
 * </ul>
 * </p>
 *
 * @see FormatGuesser
 */
@Component
public class CSVFormatGuesser implements FormatGuesser {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CSVFormatGuesser.class);

    /**
     * The maximum size used to guess the format of a CSV input stream.
     *
     */
    private static final long SIZE_LIMIT = 64L * 1024L;

    /**
     * The maximum number of lines to read to guess the format of a CSV stream.
     */
    private static final int LINE_LIMIT = 100;

    /**
     * The maximum number of lines stored from the CSV stream.
     */
    private static final int SMALL_SAMPLE_LIMIT = 10;

    /** The csv format guesser. */
    @Autowired
    private CSVFormatGuess csvFormatGuess;

    /** The fallback guess if the input is not CSV compliant. */
    @Autowired
    private UnsupportedFormatGuess fallbackGuess;

    @Autowired
    private CSVFormatUtils csvFormatUtils;

    /** A list of supported separators for a CSV content */
    private Set<Character> validSeparators = new HashSet<Character>() { // NOSONAR no need for a SerializationUID in the HashSet
        {
            add(' ');
            add('\t');
            add(',');
            add(';');
        }
    };

    /**
     * @see FormatGuesser#guess(SchemaParser.Request, String)
     */
    @Override
    public FormatGuesser.Result guess(SchemaParser.Request request, String encoding) {
        if (request == null || request.getContent() == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        // if the dataset metadata is updated, let's use the separator set as the one to use
        Optional<Character> specifiedSeparator = Optional.empty();
        final String temp = request.getMetadata().getContent().getParameters().get(CSVFormatGuess.SEPARATOR_PARAMETER);
        if (temp != null && StringUtils.isNotEmpty(temp)) {
            specifiedSeparator = Optional.of(temp.charAt(0));
        }

        Separator sep = guessSeparator(request.getContent(), encoding, specifiedSeparator);

        // Fallback
        if (sep == null) {
            return new FormatGuesser.Result(fallbackGuess, "UTF-8", Collections.emptyMap());
        }

        Map<String, String> parameters = csvFormatUtils.compileSeparatorProperties(sep);
        return new FormatGuesser.Result(csvFormatGuess, encoding, parameters);
    }

    /**
     * Try to guess the separator used in the CSV.
     *
     * @param is the input stream to read the CSV from.
     * @param encoding the encoding to use for the reading.
     * @param forcedSeparator if the separator is forced.
     * @return the guessed CSV separator or null if none found.
     */
    private Separator guessSeparator(InputStream is, String encoding, Optional<Character> forcedSeparator) {
        try {
            Reader reader = encoding != null ? new InputStreamReader(is, encoding) : new InputStreamReader(is);
            try (LineNumberReader lineNumberReader = new LineNumberReader(reader)) {
                Map<Character, Separator> separatorMap = new HashMap<>();
                long totalChars = 0;
                int lineCount = 0;
                boolean inQuote = false;
                String s;
                List<String> sampleLines = new ArrayList<>();

                // Detectors used to check the encoding.
                List<WrongEncodingDetector> detectors = Arrays.asList( //
                        new WrongEncodingDetector(65533), //
                        new WrongEncodingDetector(0) //
                );

                while (totalChars < SIZE_LIMIT && lineCount < LINE_LIMIT && (s = lineNumberReader.readLine()) != null) {
                    totalChars += s.length() + 1; // count the new line character
                    if (s.isEmpty()) {
                        continue;
                    }
                    if (!inQuote) {
                        lineCount++;
                        if (lineCount < SMALL_SAMPLE_LIMIT) {
                            sampleLines.add(s);
                        }
                    }
                    for (int i = 0; i < s.length(); i++) {

                        char c = s.charAt(i);

                        // check the encoding
                        try {
                            checkEncoding(c, totalChars, detectors);
                        } catch (IOException e) {
                            LOGGER.debug(encoding + " is assumed wrong" + e);
                            return null;
                        }

                        if ('"' == c) {
                            inQuote = !inQuote;
                        }

                        if (!inQuote && filterSeparator(forcedSeparator, c)) {
                            processCharAsSeparatorCandidate(c, separatorMap, lineCount);
                        }

                    }
                }
                return chooseSeparator(new ArrayList<>(separatorMap.values()), lineCount, sampleLines, forcedSeparator);
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
        } catch (Exception e) {
            LOGGER.debug("Unable to read content from content using encoding '{}'.", encoding, e);
            return null;
        }
    }

    /**
     * Return true if the given char match the optional forced separator.
     *
     * @param forcedSeparator the optional forced separator.
     * @param c the char to analyze.
     * @return true if the given char match the optional forced separator.
     */
    private boolean filterSeparator(Optional<Character> forcedSeparator, char c) {
        // if there's a forced separator and it matches the given char
        return !forcedSeparator.isPresent() || forcedSeparator.get() == c;
    }

    /**
     * Check the encoding with every WrongEncodingDetector.
     *
     * @param c the current char to check.
     * @param totalChars the total number of chars so far.
     * @throws IOException if the encoding is assumed wrong.
     */
    private void checkEncoding(char c, long totalChars, List<WrongEncodingDetector> detectors) throws IOException {
        for (WrongEncodingDetector detector : detectors) {
            detector.checkChar(c, totalChars);
        }
    }

    /**
     * Detects if the given char is a separator candidate. If true, the separator is added within the separators map.
     *
     * @param candidate the candidate to analyse.
     * @param separatorMap the map of current candidates.
     * @param lineNumber the current line number.
     */
    protected void processCharAsSeparatorCandidate(char candidate, Map<Character, Separator> separatorMap, int lineNumber) {
        if (!Character.isLetterOrDigit(candidate)) {
            Separator separator = separatorMap.get(candidate);
            if (separator == null) {
                separator = new Separator(candidate);
                separatorMap.put(candidate, separator);
            }
            separator.incrementCount(lineNumber);
        }
    }

    /**
     * Choose the best separator out of the given ones.
     *
     * @param separators the list of separators found in the CSV (may be empty but not null.
     * @param lineCount number of lines in the CSV.
     * @param forcedSeparator
     * @return the separator to use to read the CSV or null if none found.
     */
    private Separator chooseSeparator(List<Separator> separators, int lineCount, List<String> sampleLines, Optional<Character> forcedSeparator) {

        // filter separators
        final List<Separator> filteredSeparators = separators.stream() //
                .filter(sep ->
                        (forcedSeparator.isPresent() && forcedSeparator.get()== sep.getSeparator()) || validSeparators.contains(sep.getSeparator())
                ) // filter out invalid separators
                .collect(Collectors.toList());

        // easy case where there's no choice
        if (filteredSeparators.isEmpty()) {
            if (lineCount > 0) {
                // There are some lines processed, but no separator (a one-column content?), so pick a default
                // separator.
                Separator result = new Separator(',');
                filteredSeparators.add(result);
            } else {
                return null;
            }
        }

        // compute each separator score
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(lineCount, sampleLines);
        filteredSeparators.forEach(separatorAnalyzer::accept); // analyse separators and set header info and score

        // sort separator and return the first
        return filteredSeparators.stream() //
                .sorted(separatorAnalyzer::compare).findFirst() //
                .get();
    }

    /**
     * Count the number of 'informant char' found in the file. If this number exceeds the threshold (10 %) the encoding
     * is assumed false.
     */
    private class WrongEncodingDetector {

        /** Threshold to detect binary stream in percentage. */
        private static final int WRONG_ENCODING_THRESHOLD = 10;

        private static final int WARM_UP_SAMPLE_SIZE = 50;

        /** Char informing that the encoding is supposed to be wrong. */
        private int informantChar;

        /** How many time was the informant char found. */
        private long count = 0;

        /**
         * Default constructor.
         *
         * @param informantChar the char to use to detect wrong encoding.
         */
        public WrongEncodingDetector(int informantChar) {
            this.informantChar = informantChar;
        }

        /**
         * Check the given char.
         *
         * @param read the char that was read.
         * @param totalChars the total number of chars.
         * @throws IOException if encoding is assumed false.
         */
        public void checkChar(char read, long totalChars) throws IOException {

            if (informantChar != (int) read) {
                return;
            }

            count++;
            long percentage = count * 100 / totalChars;
            if (totalChars > WARM_UP_SAMPLE_SIZE && percentage > WRONG_ENCODING_THRESHOLD) {
                LOGGER.debug("wrong encoding detected, hence cannot be a CSV");
                throw new IOException("'" + (char) informantChar + "' is found more than " + WRONG_ENCODING_THRESHOLD
                        + " % in file after reading a reading " + WARM_UP_SAMPLE_SIZE + " characters.");
            }
        }

    }

}