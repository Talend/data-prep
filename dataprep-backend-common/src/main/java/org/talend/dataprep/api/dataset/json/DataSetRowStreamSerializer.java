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

package org.talend.dataprep.api.dataset.json;

import java.io.IOException;
import java.util.stream.Stream;

import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DataSetRowStreamSerializer extends JsonSerializer<Stream<DataSetRow>> {

    @Override
    public void serialize(Stream<DataSetRow> value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartArray();
        try {
            value.forEach(row -> {
                try {
                    generator.writeObject(row.valuesWithId());
                } catch (IOException e) {
                    throw new TDPException(CommonErrorCodes.UNABLE_TO_WRITE_JSON, e);
                }
            });
        } finally {
            // Not actually needed (stream should be already closing resources at end of rows, but keep it for safety).
            value.close();
        }
        generator.writeEndArray();
    }
}