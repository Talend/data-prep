/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.transformation.format;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;

import org.talend.dataprep.util.FilesHelper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Buffer for Jackson-JSON serializable objects on a file.
 *
 * This class creates a temporary file to buffer objects serialized as JSON using an {@link ObjectMapper}. They can then
 * all be retrieved.
 *
 * This is not intended to be thread-safe. Read is intended to be done after all writes are done.
 *
 * @param <T> the type to store.
 */
public class ObjectBuffer<T> implements AutoCloseable {

    private final JsonGenerator generator;

    private final ObjectMapper mapper = new ObjectMapper();

    private Path tempFile;

    private Class<T> bufferedClass;

    private boolean closed = false;

    public ObjectBuffer(Class<T> bufferedClass) throws IOException {
        this.bufferedClass = bufferedClass;
        tempFile = Files.createTempFile("buffered-object", ".json");
        FileWriter fileWriter = new FileWriter(tempFile.toFile());
        generator = mapper.getFactory().createGenerator(fileWriter);
    }

    /** Append an object to the buffer. */
    public void appendRow(T entity) throws IOException {
        generator.writeObject(entity);
    }

    /**
     * Read all data of the ObjectBuffer, prevent any further writings.
     */
    public Stream<T> readAll() throws IOException {
        if (closed) {
            throw new IOException("The ObjectBuffer is closed");
        }
        generator.close();
        FileReader reader = new FileReader(tempFile.toFile());
        JsonParser parser = mapper.getFactory().createParser(reader);
        Iterator<T> objectIterator = mapper.readValues(parser, bufferedClass);
        return stream(spliteratorUnknownSize(objectIterator, Spliterator.SIZED), false);
    }

    /**
     * Close the ObjectBuffer and delete underlying resources.
     */
    public void close() throws IOException {
        generator.close();
        FilesHelper.deleteQuietly(tempFile.toFile());
        closed = true;
    }

}
