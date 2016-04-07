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

package org.talend.dataprep.format.export.json;

import java.io.IOException;

import org.talend.dataprep.format.export.ExportFormat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.talend.dataprep.parameters.Parameter;

/**
 * Format JSON parameter.
 */
public class ExportFormatSerializer extends JsonSerializer<ExportFormat> {

    /**
     * @see ExportFormatSerializer#serialize(ExportFormat, JsonGenerator, SerializerProvider)
     */
    @Override
    public void serialize(ExportFormat value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("mimeType", value.getMimeType());
        gen.writeStringField("extension", value.getExtension());
        gen.writeStringField("id", value.getName());
        gen.writeStringField("needParameters", Boolean.toString(value.isNeedParameters()));
        gen.writeStringField("defaultExport", Boolean.toString(value.isDefaultExport()));
        gen.writeBooleanField("enabled", value.isEnabled());
        gen.writeStringField("disableReason", value.getDisableReason());

        if (!value.getParameters().isEmpty()) {
            gen.writeFieldName("parameters");
            gen.writeObject(value.getParameters());
        }

        gen.writeEndObject();
    }

}
