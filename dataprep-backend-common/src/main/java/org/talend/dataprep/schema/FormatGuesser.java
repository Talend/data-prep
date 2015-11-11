package org.talend.dataprep.schema;

import java.io.InputStream;
import java.util.Map;

/**
 * Represents a class able to create {@link org.talend.dataprep.schema.FormatGuess} from a data set content.
 */
public interface FormatGuesser {

    /**
     * Guess the content type of the provided stream.
     *
     * @param stream The raw data set content. Content cannot be <code>null</code>.
     * @param encoding The encoding to use to read content in <code>stream</code>.
     * @return A {@link org.talend.dataprep.schema.FormatGuess guess} that can never be null (see
     * {@link FormatGuess#getConfidence()}.
     * @throws IllegalArgumentException If stream is <code>null</code>.
     */
    Result guess(InputStream stream, String encoding);

    class Result {

        private FormatGuess formatGuess;

        private Map<String, String> parameters;

        private String encoding;

        public Result(FormatGuess formatGuess, String encoding, Map<String, String> parameters) {
            this.formatGuess = formatGuess;
            this.encoding = encoding;
            this.parameters = parameters;
        }

        public FormatGuess getFormatGuess() {
            return formatGuess;
        }

        public void setFormatGuess(FormatGuess formatGuess) {
            this.formatGuess = formatGuess;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Result)) {
                return false;
            }

            Result result = (Result) o;

            if (!formatGuess.equals(result.formatGuess)) {
                return false;
            }
            return parameters.equals(result.parameters);

        }

        @Override
        public int hashCode() {
            int result = formatGuess.hashCode();
            result = 31 * result + parameters.hashCode();
            return result;
        }

        public String getEncoding() {
            return encoding;
        }
    }
}
