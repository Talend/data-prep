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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.schema.FormatGuesser;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaUpdater;

/**
 *
 */
@Component
public class CSVSchemaUpdater implements SchemaUpdater {

    /** CSV format utils. */
    @Autowired
    CSVFormatUtils formatUtils;

    /** The csv format guesser. */
    @Autowired
    private CSVFormatGuess csvFormatGuess;

    /** The CSV format guesser. */
    @Autowired
    private CSVFormatGuesser csvFormatGuesser;

    /**
     * @see SchemaUpdater#accept(DataSetMetadata)
     */
    @Override
    public boolean accept(DataSetMetadata metadata) {
        return StringUtils.equals(metadata.getContent().getFormatGuessId(), CSVFormatGuess.BEAN_ID);
    }

    /**
     * @see SchemaUpdater#updateSchema(SchemaParser.Request)
     */
    @Override
    public FormatGuesser.Result updateSchema(SchemaParser.Request request) {
        final DataSetMetadata metadata = request.getMetadata();
        formatUtils.useNewSeparator(metadata);
        return csvFormatGuesser.guess(request, metadata.getEncoding());
    }

    /**
     * @return the format guess.
     */
    @Override
    public FormatGuess getFormatGuess() {
        return csvFormatGuess;
    }

}
