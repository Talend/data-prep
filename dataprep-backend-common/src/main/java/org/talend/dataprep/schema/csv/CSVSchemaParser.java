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

import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaParserResult;

import au.com.bytecode.opencsv.CSVReader;

@Service("parser#csv")
public class CSVSchemaParser implements SchemaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVSchemaParser.class);

    private static final String META_KEY = "key";

    @Autowired
    protected CSVFormatUtils csvFormatUtils;

    /**
     *
     * @param request container with information needed to parse the raw data.
     * @return
     */
    @Override
    public SchemaParserResult parse(Request request) {
        List<SchemaParserResult.SheetContent> sheetContents = new ArrayList<>();
        sheetContents.add(new SchemaParserResult.SheetContent(META_KEY, new ArrayList<>()));
        try {
            final DataSetMetadata metadata = request.getMetadata();
            final Map<String, String> parameters = metadata.getContent().getParameters();
            final char separator = parameters.get(CSVFormatGuess.SEPARATOR_PARAMETER).charAt(0);
            List<String> header = csvFormatUtils.retrieveHeader(parameters);

            if (header == null || header.isEmpty()) {
                try (CSVReader reader = new CSVReader(new InputStreamReader(request.getContent(), metadata.getEncoding()), separator)) {
                    String[] columns = reader.readNext();
                    if (columns == null) { // Empty content?
                        return SchemaParserResult.Builder.parserResult() //
                                .sheetContents(sheetContents).build();
                    } else {
                        throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT);
                    }
                }
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
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
        }
        return SchemaParserResult.Builder.parserResult() //
                .sheetContents(sheetContents) //
                .draft(false).build();
    }

}