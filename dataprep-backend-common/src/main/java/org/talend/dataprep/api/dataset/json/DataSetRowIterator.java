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

package org.talend.dataprep.api.dataset.json;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static org.talend.dataprep.api.dataset.row.FlagNames.TDP_ID;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.FlagNames;

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

    /** DataSetRow object used to read rows (cleaned and reused at each iteration). */
    private DataSetRow row;

    /** RowMetadata to link to the row. */
    private final RowMetadata rowMetadata;

    /**
     * Constructor.
     *
     * @param parser the json parser to use.
     * @param rowMetadata the row metadata to add to each row.
     */
    public DataSetRowIterator(JsonParser parser, RowMetadata rowMetadata) {
        this.parser = parser;
        this.rowMetadata = rowMetadata;
        this.row = new DataSetRow(rowMetadata);
    }

    /**
     * Constructor.
     *
     * @param inputStream stream to read json from.
     */
    public DataSetRowIterator(InputStream inputStream) {
        try {
            this.parser = new JsonFactory().createParser(inputStream);
            this.rowMetadata = new RowMetadata();
            this.row = new DataSetRow(rowMetadata);
        } catch (IOException e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * @see Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {

        // parser is closed, it's easy !
        if (parser.isClosed()) {
            return false;
        }

        final String currentName;
        try {
            currentName = parser.getCurrentName();
        } catch (IOException e) {
            LOGGER.debug("Unable to get current JSON field name.", e);
            return false;
        }

        final JsonToken currentToken = parser.getCurrentToken();

        // when dealing with records, there are still records when the token is not null and the end of the array is not reached
        if ("records".equals(currentName)) {
            return currentToken != null && currentToken != END_ARRAY;
        }
        // for all the other cases, let's keep it simple
        else {
            return currentToken != END_ARRAY;
        }
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
                case VALUE_TRUE:
                case VALUE_FALSE:
                    if ("_deleted".equals(currentFieldName)) {
                        row.setDeleted(Boolean.parseBoolean(parser.getText()));
                    }
                    break;
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    if (FlagNames.TDP_ID.equals(currentFieldName)) {
                        row.setTdpId(Long.parseLong(parser.getText()));
                    }
                    break;
                default:
                    // let's skip this unsupported token
                    break;
                }
            }
            parser.nextToken();
            return row;
        } catch (IOException e) {
            throw new TalendRuntimeException(BaseErrorCodes.UNABLE_TO_PARSE_JSON, e);
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
