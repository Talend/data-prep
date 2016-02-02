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

package org.talend.dataprep.schema.xls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.schema.DraftValidator;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.Serializer;

@Component(XlsFormatGuess.BEAN_ID)
public class XlsFormatGuess implements FormatGuess {

    public static final String MEDIA_TYPE = "application/vnd.ms-excel";

    public static final String BEAN_ID = "formatGuess#xls";

    @Autowired
    private XlsSchemaParser schemaParser;

    @Autowired
    private XlsSerializer serializer;

    @Autowired
    private XlsDraftValidator xlsDraftValidator;

    @Override
    public String getMediaType() {
        return MEDIA_TYPE;
    }

    @Override
    public float getConfidence() {
        return 1;
    }

    @Override
    public SchemaParser getSchemaParser() {
        return schemaParser;
    }

    @Override
    public Serializer getSerializer() {
        return serializer;
    }

    @Override
    public DraftValidator getDraftValidator()
    {
        return xlsDraftValidator;
    }

    @Override
    public String getBeanId() {
        return BEAN_ID;
    }
}
