package org.talend.dataprep.schema.csv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.schema.DraftValidator;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.Serializer;

@Service(CSVFormatGuess.BEAN_ID)
public class CSVFormatGuess implements FormatGuess {

    public static final String SEPARATOR_PARAMETER = "SEPARATOR"; //$NON-NLS-1$

    /**
     * The parameter used to set and retrieve header information
     */
    public static final String HEADER_COLUMNS_PARAMETER = "COLUMN_HEADERS";

    /**
     * The parameter used to set and retrieve the number of lines spanned by the header
     */
    public static final String HEADER_NB_LINES_PARAMETER = "HEADER_NB_LINES";

    public static final String BEAN_ID = "formatGuess#csv";

    @Autowired
    private CSVSchemaParser schemaParser;

    @Autowired
    private CSVSerializer serializer;

    public CSVFormatGuess() {
        //
    }

    @Override
    public String getMediaType() {
        return "text/csv"; //$NON-NLS-1$
    }

    @Override
    public float getConfidence() {
        return 0.9f;
    }

    @Override
    public SchemaParser getSchemaParser() {
        return this.schemaParser;
    }

    @Override
    public Serializer getSerializer() {
        return this.serializer;
    }

    @Override
    public DraftValidator getDraftValidator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBeanId() {
        return BEAN_ID;
    }

}
