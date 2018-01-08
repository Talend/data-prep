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

package org.talend.dataprep.schema.html;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.schema.DraftValidator;
import org.talend.dataprep.schema.FormatFamily;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.Serializer;

@Component(HtmlFormatFamily.BEAN_ID)
public class HtmlFormatFamily implements FormatFamily {

    // Html content is not Excel, but currently only HTML content wrapped in Excel is supported, thus this MIME.
    public static final String MEDIA_TYPE = "application/vnd.ms-excel";

    public static final String BEAN_ID = "formatGuess#html";

    @Autowired
    private HtmlSchemaParser schemaParser;

    @Autowired
    private HtmlSerializer htmlSerializer;

    @Override
    public String getMediaType() {
        return MEDIA_TYPE;
    }

    @Override
    public SchemaParser getSchemaGuesser() {
        return schemaParser;
    }

    @Override
    public Serializer getSerializer() {
        return htmlSerializer;
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
