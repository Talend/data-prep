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

package org.talend.dataprep.schema.csv;

import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.io.*;
import java.util.*;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.schema.Schema;
import org.talend.dataprep.schema.SchemaParser;

@Service("parser#csv")
public class CSVSchemaParser implements SchemaParser {

    /** A list of supported separators for a CSV content */
    public static final List<Character> DEFAULT_VALID_SEPARATORS = Collections
            .unmodifiableList(Arrays.asList(' ', '\t', ',', ';', '|'));

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVSchemaParser.class);

    private static final String META_KEY = "key";

    /**
     * The maximum size used to guess the schema of a CSV input stream.
     *
     */
    private static final long SIZE_LIMIT = 64L * 1024L;

    /**
     * The maximum number of lines to read to guess the schema of a CSV stream.
     */
    private static final int LINE_LIMIT = 100;

    /**
     * The maximum number of lines stored from the CSV stream.
     */
    private static final int SMALL_SAMPLE_LIMIT = 10;

    @Autowired
    protected CSVFormatUtils csvFormatUtils;

    @Override
    public boolean accept(DataSetMetadata metadata) {
        if (metadata == null || metadata.getContent() == null) {
            return false;
        }
        return StringUtils.equals(metadata.getContent().getFormatFamilyId(), CSVFormatFamily.BEAN_ID);
    }

    /**
     *
     * @param request container with information needed to parse the raw data.
     * @return
     */
    @Override
    public Schema parse(Request request) {
        List<Schema.SheetContent> sheetContents = new ArrayList<>();
        sheetContents.add(new Schema.SheetContent(META_KEY, new ArrayList<>()));
        try {
            final DataSetMetadata metadata = request.getMetadata();
            final Map<String, String> parameters = guess(request, metadata.getEncoding());
            metadata.getContent().setParameters(parameters);
            List<String> header = csvFormatUtils.retrieveHeader(parameters);

            if (header == null || header.isEmpty()) {
                throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT);
            }
            LOGGER.debug("Columns found: {}", header);
            // By default, consider all columns as Strings (to be refined by deeper analysis).
            LOGGER.debug("Setting default type for columns...");
            int i = 0;
            for (String column : header) {
                sheetContents.stream().filter(sheetContent -> META_KEY.equals(sheetContent.getName())).findFirst() //
                        .get().getColumnMetadatas() //
                        .add(column().id(i++).name(column).type(Type.STRING).build());
            }
        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
        }
        return Schema.Builder.parserResult() //
                .sheetContents(sheetContents) //
                .draft(false).build();
    }

    /**
     * Guesses the schema of a dataset.
     *
     * @param request container with information needed to parse the raw data
     * @param encoding the encoding of the content (raw data) of the data set
     * @return a Map containing parameters needed to create a schema
     */
    public Map<String, String> guess(SchemaParser.Request request, String encoding) {
        if (request == null || request.getContent() == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        // if the dataset metadata is updated, let's use the separator set as the one to use
        Optional<Character> forcedSeparator = Optional.empty();
        final String temp = request.getMetadata().getContent().getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER);
        if (temp != null && StringUtils.isNotEmpty(temp)) {
            forcedSeparator = Optional.of(temp.charAt(0));
        }

        Separator sep = guessSeparator(request.getContent(), encoding, forcedSeparator);

        return csvFormatUtils.compileParameterProperties(sep, request.getMetadata().getContent().getParameters());
    }

    /**
     * Try to guess the separator of a CSV formatted input stream or use the specified forced separator.
     *
     * @param is the input stream to read the CSV from
     * @param encoding the encoding to use for the reading
     * @param forcedSeparator if the separator is forced
     * @return the guessed CSV separator or null if none found
     */
    private Separator guessSeparator(InputStream is, String encoding, Optional<Character> forcedSeparator) {
        try (CSVStreamReader csvStreamReader = new CSVStreamReader(is, encoding, SIZE_LIMIT, LINE_LIMIT)) {
            Map<Character, Separator> separatorMap = new HashMap<>();
            String line;
            List<String> sampleLines = new ArrayList<>();
            final List<Character> validSepartors;

            validSepartors = forcedSeparator.map(Collections::singletonList).orElse(DEFAULT_VALID_SEPARATORS);

            while ((line = csvStreamReader.readLine()) != null) {
                if (!line.isEmpty() && sampleLines.size() < SMALL_SAMPLE_LIMIT) {
                    sampleLines.add(line);
                }
                processLine(line, separatorMap, validSepartors, csvStreamReader.getLineCount());
            }
            return chooseSeparator(new ArrayList<>(separatorMap.values()), csvStreamReader.getLineCount(), sampleLines,
                    forcedSeparator);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
        } catch (Exception e) {
            LOGGER.debug("Unable to read content from content using encoding '{}'.", encoding, e);
            return null;
        }
    }

    /**
     * Process a line to update the separators with the current line
     * 
     * @param line the current line
     * @param separatorMap the map of current candidates
     * @param validSeparators the list of valid separators
     * @param lineCount the current line number
     */
    private void processLine(String line, Map<Character, Separator> separatorMap, List<Character> validSeparators,
            int lineCount) {
        // do not process the line if it is empty
        if (line.isEmpty())
            return;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            processCharAsSeparatorCandidate(c, separatorMap, validSeparators, lineCount);
        }
    }

    /**
     * Detects if the given char is a separator candidate. If true, the separator is added within the separators map.
     *
     * @param candidate the candidate to analyse
     * @param separatorMap the map of current candidates
     * @param lineNumber the current line number
     */
    protected void processCharAsSeparatorCandidate(char candidate, Map<Character, Separator> separatorMap,
            List<Character> validSeparators, int lineNumber) {
        if (validSeparators.contains(candidate)) {
            updateSeparatorMap(candidate, separatorMap, lineNumber);
        }
    }

    private void updateSeparatorMap(char candidate, Map<Character, Separator> separatorMap, int lineNumber) {
        Separator separator = separatorMap.get(candidate);
        if (separator == null) {
            separator = new Separator(candidate);
            separatorMap.put(candidate, separator);
        }
        separator.incrementCount(lineNumber);
    }

    /**
     * Chooses the best separator out of the given ones.
     *
     * @param separators the list of separators found in the CSV (may be empty but not null
     * @param lineCount number of lines in the CSV
     * @param forcedSeparator
     * @return the separator to use to read the CSV or null if none found
     */
    private Separator chooseSeparator(List<Separator> separators, int lineCount, List<String> sampleLines,
            Optional<Character> forcedSeparator) {

        // easy case where there's no choice
        if (separators.isEmpty()) {
            if (lineCount > 0) {
                // There are some lines processed, but no separator (a one-column content?), so pick a default
                // separator.
                if (forcedSeparator.isPresent()) {
                    Separator result = new Separator(forcedSeparator.get());
                    separators.add(result);
                } else {
                    Separator result = new Separator(',');
                    separators.add(result);
                }
            } else {
                return null;
            }
        }

        // compute each separator score
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(lineCount, sampleLines);
        separators.forEach(separatorAnalyzer::accept); // analyse separators and set header info and score

        // sort separator and return the first
        return separators.stream() //
                .sorted(separatorAnalyzer::compare).findFirst() //
                .get();
    }

    /**
     * A helper class to parse CSV file during separator guessing or updating
     */
    class CSVStreamReader implements AutoCloseable {

        /**
         * The number of so far read characters
         */
        private long totalChars = 0;

        /**
         * The number of line read so far
         */
        private int lineCount = 0;

        /**
         * Is a quote currently opened
         */
        private boolean inQuote = false;

        /**
         * The helper {@see LineNumberReader}
         */
        private final LineNumberReader reader;

        /**
         * The maximum size that can be read from the file
         */
        private final long sizeLimit;

        /**
         * The maximum number of lines that can be read from the file
         */
        private final int lineLimit;

        /**
         * Constructs an CSVReaderStream object.
         * 
         * @param inputStream the specified base input stream
         * @param encoding the encoding of the file
         * @param sizeLimit maximum size that can be read from the file
         * @param lineLimit the maximum number of lines that can be read from the file
         * @throws UnsupportedEncodingException
         */
        public CSVStreamReader(InputStream inputStream, String encoding, long sizeLimit, int lineLimit)
                throws UnsupportedEncodingException {
            this.sizeLimit = sizeLimit;
            this.lineLimit = lineLimit;
            this.reader = new LineNumberReader(
                    encoding != null ? new InputStreamReader(inputStream, encoding) : new InputStreamReader(inputStream));
        }

        /**
         * Returns the portion of a line that is not in quote as a string.
         * 
         * @return he portion of a line that is not in quote as a string
         * @throws IOException
         */
        public String readLine() throws IOException {
            final String currentLine;
            if (totalChars < sizeLimit && lineCount < lineLimit && (currentLine = reader.readLine()) != null) {
                totalChars += currentLine.length() + 1; // count the new line character
                if (BooleanUtils.isFalse(inQuote)) {
                    lineCount++;
                }
                return processLine(currentLine);
            } else {
                reader.close();
                return null;
            }
        }

        /**
         * Processes a line and only returns the portion of a line that is not in quote as a string.
         * 
         * @param line the line as read from the input stream
         * @return the portion of a line that is not in quote as a string
         */
        private String processLine(String line) {

            StringBuilder sb = new StringBuilder();
            // do not process the line if it is empty
            if (line.isEmpty())
                return sb.toString();

            for (int i = 0; i < line.length(); i++) {

                char c = line.charAt(i);

                if ('"' == c) {
                    inQuote = BooleanUtils.negate(inQuote);
                }

                sb.append(c);
            }
            return sb.toString();
        }

        @Override
        public void close() throws Exception {
            reader.close();
        }

        public long getTotalChars() {
            return totalChars;
        }

        public int getLineCount() {
            return lineCount;
        }
    }

}