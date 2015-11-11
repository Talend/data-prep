package org.talend.dataprep.api.dataset.json;

import static org.talend.dataprep.api.dataset.DataSetRow.TDP_ID;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Iterator of dataset row used to Stream DatasetRows from json.
 */
public class DataSetRowIterator implements Iterator<DataSetRow> {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetRowIterator.class);

    /** The json parser. */
    private final JsonParser parser;

    /** True if the tdpId is added to the row values. */
    private final boolean addTdpId;

    /** DataSetRow object used to read rows (cleaned and reused at each iteration). */
    private DataSetRow row;

    /** RowMetadata to link to the row. */
    private final RowMetadata rowMetadata;

    /** Counter for the tdp id. */
    private long nextRowId = 1;

    /**
     * Constructor.
     *
     * @param parser the json parser to use.
     * @param rowMetadata the row metadata to add to each row.
     * @param addTdpId true if tdpid is added for each row.
     */
    public DataSetRowIterator(JsonParser parser, RowMetadata rowMetadata, boolean addTdpId) {
        this.addTdpId = addTdpId;
        this.parser = parser;
        this.rowMetadata = rowMetadata;
        this.row = new DataSetRow(rowMetadata);
    }

    /**
     * Constructor.
     *
     * @param inputStream stream to read json from.
     * @param addTdpId true if tdpid is added for each row.
     */
    public DataSetRowIterator(InputStream inputStream, boolean addTdpId) {
        try {
            this.addTdpId = addTdpId;
            this.parser = new JsonFactory().createParser(inputStream);
            this.rowMetadata = new RowMetadata();
            this.row = new DataSetRow(rowMetadata);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * @see Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return !parser.isClosed() && parser.getCurrentToken() != JsonToken.END_ARRAY;
    }

    /**
     * @see Iterator#next()
     */
    @Override
    public DataSetRow next() {
        try {
            String currentFieldName = StringUtils.EMPTY;
            JsonToken nextToken;
            row.clear();
            if (addTdpId) {
                row.setTdpId(nextRowId++);
            }
            while ((nextToken = parser.nextToken()) != JsonToken.END_OBJECT) {
                if (nextToken == null) {
                    // End of input, return the current row.
                    LOGGER.warn("Unexpected end of input for row {}.", row.values());
                    return row;
                }
                switch (nextToken) {
                // DataSetRow fields
                case FIELD_NAME:
                    currentFieldName = parser.getText();
                    break;
                case VALUE_STRING:
                    setStringValue(currentFieldName, parser.getText());
                    break;
                case VALUE_NULL:
                    row.set(currentFieldName, "");
                    break;
                default:
                    // let's skip this unsupported token
                    break;
                }
            }
            parser.nextToken();
            return row;
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * Set the string value and deal with the TDP-ID case.
     *
     * @param fieldName the name of the field.
     * @param value the value.
     */
    private void setStringValue(String fieldName, String value) {
        if (TDP_ID.equals(fieldName)) {
            row.setTdpId(Long.parseLong(value));
        } else {
            row.set(fieldName, value);
        }
    }
}
