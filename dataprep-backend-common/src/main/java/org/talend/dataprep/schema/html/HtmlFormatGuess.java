package org.talend.dataprep.schema.html;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.talend.dataprep.schema.DraftValidator;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.Serializer;

@Component(HtmlFormatGuess.BEAN_ID)
public class HtmlFormatGuess implements FormatGuess {

    public static final String MEDIA_TYPE = "text/html";

    public static final String BEAN_ID = "formatGuess#html";

    @Inject
    private HtmlSchemaParser schemaParser;

    @Inject
    private HtmlSerializer htmlSerializer;

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
