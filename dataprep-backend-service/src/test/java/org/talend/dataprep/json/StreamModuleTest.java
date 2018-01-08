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

import static java.util.Spliterators.spliterator;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StreamModuleTest extends ServiceBaseTest {

    @Autowired
    ObjectMapper mapper;

    @Test
    public void shouldStreamWithNullValues() throws Exception {
        // Given
        final Stream<String> stringStream = Stream.of(null, null);

        // When
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, stringStream);

        // Then
        assertThat(writer.toString(), sameJSONAs("[]"));
    }

    @Test
    public void shouldHandleEmptyStream() throws Exception {
        // Given
        final Stream<String> stringStream = Stream.empty();

        // When
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, stringStream);

        // Then
        assertThat(writer.toString(), sameJSONAs("[]"));
    }

    @Test
    public void shouldHandleCloseStreamException() throws Exception {
        // Given
        Stream<String> stringStream = Stream.of();
        stringStream = stringStream.onClose(() -> {
            throw new RuntimeException("On purpose thrown close exception.");
        });

        // When
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, stringStream);

        // Then
        assertThat(writer.toString(), sameJSONAs("[]"));
    }

    @Test
    public void shouldIteratorOverValues() throws Exception {
        // Given
        final Stream<String> stringStream = Stream.of("string1", "string2");

        // When
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, stringStream);

        // Then
        assertThat(writer.toString(), sameJSONAs("[\"string1\",\"string2\"]"));
    }

    @Test
    public void null_value_works() throws Exception {
        // Given
        final Stream<String> stringStream = Stream.of(null, "string1", "string2");

        // When
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, stringStream);

        // Then
        assertThat(writer.toString(), sameJSONAs("[\"string1\",\"string2\"]"));
    }

    @Test
    public void shouldHaveCorrectJSONWhenFailure() throws Exception {
        // Given
        Iterator<String> failedIterator = new Iterator<String>() {

            int i = 0;

            @Override
            public boolean hasNext() {
                return i < 2;
            }

            @Override
            public String next() {
                if (i <= 0) {
                    i++;
                    return "string1";
                } else {
                    throw new RuntimeException("On purpose unchecked exception.");
                }
            }
        };

        final Stream<String> stringStream = stream(spliterator(failedIterator, 2, 0), false);

        // When
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, stringStream);

        // Then
        assertThat(writer.toString(), sameJSONAs("[\"string1\"]"));
    }

    @Test
    public void shouldHaveCorrectJSONWithTDPFailure() throws Exception {
        // Given
        Iterator<String> failedIterator = new Iterator<String>() {

            int i = 0;

            @Override
            public boolean hasNext() {
                return i < 2;
            }

            @Override
            public String next() {
                if (i <= 0) {
                    i++;
                    return "string1";
                } else {
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION);
                }
            }
        };

        final Stream<String> stringStream = stream(spliterator(failedIterator, 2, 0), false);

        // When
        final StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, stringStream);
        } catch (IOException e) {
            // Ignored
        }

        // Then
        assertThat(writer.toString(), sameJSONAs("[\"string1\"]"));
    }

}
