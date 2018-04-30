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

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.slf4j.Logger;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Reader in the style of java.io stream readers. Read an avro stream of data using apache reference library.
 */
public class AvroReader implements Closeable, Iterator<GenericRecord> {

    private static final Logger LOGGER = getLogger(AvroReader.class);

    private final InputStream rawContent;

    private final GenericDatumReader<GenericRecord> reader;

    private final Decoder decoder;

    private boolean closed = false;

    private GenericRecord buffer;

    public AvroReader(InputStream rawContent, Schema schema) throws IOException {
        this(rawContent, schema, false);
    }

    public AvroReader(InputStream rawContent, Schema schema, boolean binary) throws IOException {
        this.rawContent = rawContent;

        // get the avro schema from parameters
        this.reader = new GenericDatumReader<>(schema);

        DecoderFactory decoderFactory = DecoderFactory.get();
        this.decoder = binary ? decoderFactory.binaryDecoder(rawContent, null) //
                : decoderFactory.jsonDecoder(schema, rawContent);
    }

    @Override
    public boolean hasNext() {
        if (buffer == null) {
            buffer = readNext();
        }
        return buffer != null;
    }

    @Override
    public GenericRecord next() {
        GenericRecord genericRecord;
        if (buffer == null) {
            genericRecord = readNext();
        } else {
            genericRecord = buffer;
            buffer = null;
        }
        return genericRecord;
    }

    private GenericRecord readNext() {
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
    public void close() {
        try {
            rawContent.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    private static GenericRecord readNext(GenericDatumReader<GenericRecord> reader, Decoder decoder) throws IOException {
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

    public Stream<GenericRecord> asStream() {
        return stream(spliteratorUnknownSize(this, Spliterator.IMMUTABLE), false).onClose(this::close);
    }
}
