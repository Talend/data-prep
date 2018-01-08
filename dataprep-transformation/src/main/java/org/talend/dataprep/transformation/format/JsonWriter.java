//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.format;

import static org.talend.dataprep.transformation.format.JsonFormat.JSON;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

@Scope("prototype")
@Component("writer#" + JSON)
public class JsonWriter implements TransformerWriter {

    /** The data-prep ready jackson module. */
    @Autowired
    private ObjectMapper mapper;

    /** Where this writer should write. */
    private final OutputStream output;

    /** Jackson generator. */
    private JsonGenerator generator;

    /**
     * <b>Needed</b> private constructor for the WriterRegistrationService.
     *
     * @param output where to write the transformation.
     * @param params ignored parameters.
     */
    private JsonWriter(final OutputStream output, final Map<String, String> params) {
        this(output);
    }

    /**
     * Default constructor.
     *
     * @param output Where this writer should write.
     */
    public JsonWriter(final OutputStream output) {
        this.output = output;
    }

    /**
     * Init the writer.
     * 
     * @throws IOException if an error occurs.
     */
    @PostConstruct
    private void init() throws IOException {
        this.generator = mapper.getFactory().createGenerator(output);
    }

    @Override
    public void write(final RowMetadata rowMetadata) throws IOException {
        startArray();
        rowMetadata.getColumns().forEach(col -> {
            try {
                generator.writeObject(col);
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNABLE_TO_WRITE_JSON, e);
            }
        });
        endArray();
    }

    @Override
    public void write(final DataSetRow row) throws IOException {
        generator.writeObject(row.valuesWithId());
    }

    @Override
    public void startArray() throws IOException {
        generator.writeStartArray();
    }

    @Override
    public void endArray() throws IOException {
        generator.writeEndArray();
    }

    @Override
    public void startObject() throws IOException {
        generator.writeStartObject();
    }

    @Override
    public void endObject() throws IOException {
        generator.writeEndObject();
    }

    @Override
    public void fieldName(String name) throws IOException {
        generator.writeFieldName(name);
    }

    @Override
    public void flush() throws IOException {
        generator.flush();
    }

    @Override
    public String toString() {
        return "JsonWriter";
    }
}
