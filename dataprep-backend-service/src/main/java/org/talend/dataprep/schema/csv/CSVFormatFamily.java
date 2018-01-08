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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.schema.DraftValidator;
import org.talend.dataprep.schema.FormatFamily;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.Serializer;

@Service(CSVFormatFamily.BEAN_ID)
@SuppressWarnings("InsufficientBranchCoverage")
public class CSVFormatFamily implements FormatFamily {

    /** Name of the separator parameter. */
    public static final String SEPARATOR_PARAMETER = "SEPARATOR";

    /** Name of the text enclosure parameter. */
    public static final String TEXT_ENCLOSURE_CHAR = "TEXT_ENCLOSURE_CHAR";

    /** Name of the escape character parameter. */
    public static final String ESCAPE_CHAR = "ESCAPE_CHAR";

    /** The parameter used to set and retrieve header information. */
    public static final String HEADER_COLUMNS_PARAMETER = "COLUMN_HEADERS";

    /** The parameter used to set and retrieve the number of lines spanned by the header. */
    public static final String HEADER_NB_LINES_PARAMETER = "HEADER_NB_LINES";

    public static final String MEDIA_TYPE = "text/csv";

    public static final String BEAN_ID = "formatGuess#csv";

    @Autowired
    private CSVSchemaParser schemaGuesser;

    @Autowired
    private CSVSerializer serializer;

    /**
     * CSV
     */
    public CSVFormatFamily() {
        // empty constructor needed for the json de/serialization
    }

    @Override
    public String getMediaType() {
        return MEDIA_TYPE; // $NON-NLS-1$
    }

    @Override
    public SchemaParser getSchemaGuesser() {
        return this.schemaGuesser;
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
