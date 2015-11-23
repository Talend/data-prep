package org.talend.dataprep.schema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.schema.io.CSVSchemaParser;
import org.talend.dataprep.schema.io.CSVSerializer;

@Service(CSVFormatGuess.BEAN_ID)
public class CSVFormatGuess implements FormatGuess {

    public static final String SEPARATOR_PARAMETER = "SEPARATOR"; //$NON-NLS-1$

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
