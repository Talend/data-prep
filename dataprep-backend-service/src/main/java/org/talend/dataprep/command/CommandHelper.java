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

package org.talend.dataprep.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import rx.Observable;

public class CommandHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHelper.class);

    private CommandHelper() {
    }

    public static StreamingResponseBody toStreaming(final HystrixCommand<InputStream> command) {
        return outputStream -> {
            final Observable<InputStream> stream = command.toObservable();
            stream.toBlocking().subscribe(inputStream -> {
                try {
                    IOUtils.copyLarge(inputStream, outputStream);
                    outputStream.flush();
                } catch (IOException e) {
                    try {
                        inputStream.close();
                    } catch (IOException closingException) {
                        LOGGER.warn("could not close command result, a http connection may be leaked !", closingException);
                    }
                    LOGGER.error("Unable to fully copy command result '{}'.", command.getClass(), e);
                }
            }, TDPException::rethrowOrWrap);
        };
    }

    public static ResponseEntity<Void> async(final GenericCommand<?> command) {
        final Observable<?> stream = command.toObservable();
        return stream.map(is -> {
            // copy all headers from the command response so that the mime-type is correctly forwarded. Command has
            // the correct headers due to call to toBlocking() below.
            final MultiValueMap<String, String> headers = new HttpHeaders();
            HttpStatus status = command.getStatus();
            for (Header header : command.getCommandResponseHeaders()) {
                headers.put(header.getName(), Collections.singletonList(header.getValue()));
            }
            return new ResponseEntity<Void>(null, headers, status);
        }).toBlocking().first();
    }

    public static ResponseEntity<StreamingResponseBody> toStreaming(final GenericCommand<InputStream> command) {
        final Observable<InputStream> stream = command.toObservable();
        return stream.map(is -> {
            // Content for the response entity
            final StreamingResponseBody body = outputStream -> {
                try {
                    IOUtils.copyLarge(is, outputStream);
                    outputStream.flush();
                } catch (IOException e) {
                    try {
                        is.close();
                    } catch (IOException closingException) {
                        LOGGER.warn("could not close command result, a http connection may be leaked !", closingException);
                    }
                    LOGGER.error("Unable to fully copy command result '{}'.", command.getClass(), e);
                }
            };
            // copy all headers from the command response so that the mime-type is correctly forwarded. Command has
            // the correct headers due to call to toBlocking() below.
            final MultiValueMap<String, String> headers = new HttpHeaders();
            final HttpStatus status = command.getStatus();
            for (Header header : command.getCommandResponseHeaders()) {
                headers.put(header.getName(), Collections.singletonList(header.getValue()));
            }
            return new ResponseEntity<>(body, headers, status == null ? HttpStatus.OK : status);
        }).toBlocking().first();
    }

    /**
     * Return a Publisher of type T out of the the hystrix command.
     *
     * @param clazz the wanted stream type.
     * @param mapper the object mapper used to parse objects.
     * @param command the hystrix command to deal with.
     * @param <T> the type of objects to stream.
     * @return a Publisher<T></T> out of the hystrix command response body.
     */
    public static <T> Publisher<T> toPublisher(final Class<T> clazz, final ObjectMapper mapper,
            final HystrixCommand<InputStream> command) {
        AtomicInteger count = new AtomicInteger(0);
        return Flux.create(sink -> {
            final Observable<InputStream> observable = command.toObservable();
            observable.map(i -> {
                try {
                    return mapper.readerFor(clazz).<T> readValues(i);
                } catch (IOException e) {
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                }
            }) //
            .doOnCompleted(() -> LOGGER.debug("Completed command '{}' (emits '{}') with '{}' records.", command.getClass().getName(), clazz.getName(), count.get())) //
            .toBlocking() //
            .forEach(s -> {
                while (s.hasNext()) {
                    sink.next(s.next());
                    count.incrementAndGet();
                }
                sink.complete();
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    public static <T> Stream<T> toStream(Class<T> clazz, ObjectMapper mapper, HystrixCommand<InputStream> command) {
        return Flux.from(toPublisher(clazz, mapper, command)).toStream(1);
    }
}
