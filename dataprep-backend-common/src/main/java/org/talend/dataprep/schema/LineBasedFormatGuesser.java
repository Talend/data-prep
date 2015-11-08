package org.talend.dataprep.schema;


import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
public class LineBasedFormatGuesser implements FormatGuesser {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LineBasedFormatGuesser.class);

    /** Detectors used to check the encoding. */
    private List<WrongEncodingDetector> detectors = Arrays.asList( //
            new WrongEncodingDetector(65533), //
            new WrongEncodingDetector(0000) //
    );

    /** The csv format guesser. */
    @Autowired
    private CSVFormatGuess csvFormatGuess;

    /** The fallback guess if the input is not CSV compliant. */
    @Autowired
    private UnsupportedFormatGuess fallbackGuess;

    /** A list of supported separators for a CSV content */
    private Set<Character> validSeparators = new HashSet<Character>() {
        {
            add(' ');
            add('\t');
            add(',');
            add(';');
        }
    };

    /**
     * @see FormatGuesser#guess(InputStream, String)
     */
    @Override
    public FormatGuesser.Result guess(InputStream stream, String encoding) {
        Separator sep = guessSeparator(stream, encoding);

        // Fallback
        if (sep == null) {
            return new FormatGuesser.Result(fallbackGuess, "UTF-8", Collections.emptyMap());
        }

        final char separator = sep.getSeparator();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CSVFormatGuess.SEPARATOR_PARAMETER, String.valueOf(separator));
        return new FormatGuesser.Result(csvFormatGuess, encoding, parameters);
    }

    /**
     * Try to guess the separator used in the CSV.
     *
     * @param is the inputstream to read the CSV from.
     * @param encoding the encoding to use for the reading.
     * @return the guessed CSV separator or null if none found.
     */
    private Separator guessSeparator(InputStream is, String encoding) {
        try {
            Reader reader = encoding != null ? new InputStreamReader(is, encoding) : new InputStreamReader(is);
            try (LineNumberReader lineNumberReader = new LineNumberReader(reader)) {
                List<Separator> separators = new ArrayList<>();
                Map<Character, Separator> separatorMap = new HashMap<>();
                long totalChars = 0;
                int lineCount = 0;
                boolean inQuote = false;
                String s;
                while (totalChars < 64 * 1024 && lineCount < 100 && (s = lineNumberReader.readLine()) != null) {
                    totalChars += s.length() + 1; // count the new line character
                    if (s.isEmpty()) {
                        continue;
                    }
                    if (!inQuote) {
                        lineCount++;
                    }
                    for (int i = 0; i < s.length(); i++) {
                        char c = s.charAt(i);
                        // check the encoding
                        for (WrongEncodingDetector detector : detectors) {
                            try {
                                detector.checkChar(c, totalChars);
                            } catch (IOException e) {
                                LOGGER.debug(encoding + " is assumed wrong" + e);
                                return null;
                            }
                        }
                        if ('"' == c) {
                            inQuote = !inQuote;
                        }
                        if (!Character.isLetterOrDigit(c) && !"\"' .-".contains(s.subSequence(i, i + 1)) && (!inQuote)) {
                            Separator separator = separatorMap.get(c);
                            if (separator == null) {
                                separator = new Separator(c);
                                separatorMap.put(c, separator);
                                separators.add(separator);
                            }
                            separator.totalCountPlusOne();
                        }
                    }
                }
                return chooseSeparator(separators, lineCount);
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
        } catch (Exception e) {
            LOGGER.debug("Unable to read content from content using encoding '{}'.", encoding, e);
            return null;
        }
    }

    /**
     * Choose the best separator out of the ones.
     *
     * @param separators the list of separators found in the CSV (may be empty but not null.
     * @param lineCount number of lines in the CSV.
     * @return the separator to use to read the CSV or null if none found.
     */
    private Separator chooseSeparator(List<Separator> separators, int lineCount) {

        // easy case where there's no choice
        if (separators.isEmpty()) {
            if (lineCount > 0) {
                // There are some lines processed, but no separator (a one-column content?), so pick a default separator.
                return new Separator(',');
            }
            return null;
        }

        // if there's only one separator, let's use it
        if (separators.size() == 1) {
            return separators.get(0);
        }

        // compute the average per line for separators
        separators.forEach(s -> {
            double averagePerLine = s.getTotalCount() / lineCount;
            s.setAveragePerLine(averagePerLine);
        });

        // remove irrelevant separators (0 as average per line that can happen when you read binary files)
        return separators.stream()
                .filter(separator -> separator.getAveragePerLine() > 0) //
                .sorted((sep0, sep1) -> Double.compare(sep1.getAveragePerLine(), sep0.getAveragePerLine())) //
                .filter(sep -> validSeparators.contains(sep.getSeparator())) // filter out invalid separators
                .findFirst() //
                .get();

    }

    /**
     * Count the number of 'informant char' found in the file. If this number exceeds the threshold (10 %) the encoding
     * is assumed false.
     */
    private class WrongEncodingDetector {

        /** Threshold to detect binary stream in percentage. */
        private static final int WRONG_ENCODING_THRESHOLD = 10;

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
            if (percentage > WRONG_ENCODING_THRESHOLD) {
                LOGGER.debug("wrong encoding detected, hence cannot be a CSV");
                throw new IOException(
                        "'" + (char) informantChar + "' is found more than " + WRONG_ENCODING_THRESHOLD + " % in file.");
            }
        }

    }

}