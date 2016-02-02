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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.schema.DraftValidator;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.Serializer;

@Service(CSVFormatGuess.BEAN_ID)
@SuppressWarnings("InsufficientBranchCoverage")
public class CSVFormatGuess implements FormatGuess {

    /** Name of the separator parameter. */
    public static final String SEPARATOR_PARAMETER = "SEPARATOR"; //$NON-NLS-1$
    /** The parameter used to set and retrieve header information. */
    public static final String HEADER_COLUMNS_PARAMETER = "COLUMN_HEADERS";
    /** The parameter used to set and retrieve the number of lines spanned by the header. */
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
