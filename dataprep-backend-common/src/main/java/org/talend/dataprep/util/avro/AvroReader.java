package org.talend.dataprep.util.avro;

// ============================================================================
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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static org.slf4j.LoggerFactory.getLogger;


/**
 * TCOMP serializer.
 */
public class AvroReader implements Closeable {

    private static final Logger LOGGER = getLogger(AvroReader.class);

    private final InputStream rawContent;

    private final GenericDatumReader<GenericRecord> reader;

    private final JsonDecoder decoder;

    private boolean closed = false;

    public AvroReader(InputStream rawContent, Schema schema) throws IOException {
        this.rawContent = rawContent;

        // get the avro schema from parameters
        reader = new GenericDatumReader<>(schema);
        decoder = DecoderFactory.get().jsonDecoder(schema, rawContent);
    }

    public GenericRecord read() throws IOException {
        GenericRecord record;
        if (!closed) {
            try {
                GenericRecord currentRecord = readNext(reader, decoder);
                if (currentRecord == null) {
                    // end is reached
                    record = null;
                    close();
                } else {
                    record = currentRecord;
                }
            } catch (IOException e) {
                record = null;
                close();
            }
        } else {
            record = null;
            close();
        }
        return record;
    }

    @Override
    public void close() throws IOException {
        rawContent.close();
        closed = true;
    }

    /**
     * Return the next record from the reader.
     *
     * @param reader the avro reader.
     * @param decoder the avro decoder.
     * @return the next record or null if there's none.
     * @throws IOException that can also happen.
     */
    private static GenericRecord readNext(GenericDatumReader<GenericRecord> reader, JsonDecoder decoder) throws IOException {
        try {
            return reader.read(null, decoder);
        } catch (EOFException eof) {
            // beautiful avro API...
            LOGGER.trace("Reached end of stream.", eof);
            return null;
        }
    }

    public boolean isClosed() {
        return closed;
    }
}
