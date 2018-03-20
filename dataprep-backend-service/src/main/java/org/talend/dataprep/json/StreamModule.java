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

package org.talend.dataprep.json;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.TalendRuntimeException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Component
public class StreamModule extends SimpleModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamModule.class);

    @Autowired
    @Lazy
    ObjectMapper mapper;

    /**
     * Register the serializer and deserializer.
     */
    @PostConstruct
    private void registerSerializers() {
        addSerializer(Stream.class, new JsonSerializer<Stream>() {
            @Override
            public void serialize(Stream stream, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                // Write values
                ObjectWriter objectWriter = null; // Cache object writer (to prevent additional search for ObjectWriter).
                Object previous = null;
                boolean startedResultArray = false;
                try {
                    // Write results
                    final Iterator iterator = stream.iterator();
                    stream = (Stream) stream.onClose(() -> LOGGER.debug("End of stream."));
                    LOGGER.debug("Iterating over: {}", iterator);
                    while (iterator.hasNext()) {
                        final Object next = iterator.next();
                        if (!startedResultArray) { // Start array after (indirectly) checked there's at least a result available
                            jsonGenerator.writeStartArray();
                            startedResultArray = true;
                        }
                        if (next != null && (objectWriter == null || !previous.getClass().equals(next.getClass()))) {
                            objectWriter = mapper.writerFor(next.getClass());
                        }
                        if (objectWriter != null && next != null) {
                            objectWriter.writeValue(jsonGenerator, next);
                        }
                        previous = next;
                    }
                    // Ends input (and handle empty iterators).
                    if (!startedResultArray) {
                        jsonGenerator.writeStartArray();
                        startedResultArray = true;
                    }
                } catch (TalendRuntimeException e) {
                    throw new IOException(e); // IOException so it doesn't get swallowed by Jackson
                } catch (Exception e) {
                    LOGGER.error("Unable to iterate over values.", e);
                } finally {
                    if (startedResultArray) {
                        jsonGenerator.writeEndArray();
                    }
                    jsonGenerator.flush();
                    try {
                        stream.close();
                    } catch (Exception e) {
                        LOGGER.error("Unable to close stream to serialize.", e);
                    }
                    LOGGER.debug("Iterating done.");
                }
            }
        });
    }
}
