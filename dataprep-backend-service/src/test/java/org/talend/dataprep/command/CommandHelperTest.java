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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import reactor.core.publisher.Flux;

public class CommandHelperTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCommandToAsync() throws Exception {
        GenericCommand<InputStream> command = new CommandHelperTestCommand();

        final ResponseEntity<?> responseEntity = CommandHelper.async(command);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    @Test
    public void testCommandToStreamingWithHeader() throws Exception {
        GenericCommand<InputStream> command = new CommandHelperTestCommand();

        final ResponseEntity<StreamingResponseBody> responseEntity = CommandHelper.toStreaming(command);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseEntity.getBody().writeTo(outputStream);
        assertEquals("test", new String(outputStream.toByteArray()));
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertEquals("custom value", responseEntity.getHeaders().get("custom").get(0));
    }

    @Test
    public void testCommandToStreamingWithNoHeader() throws Exception {
        HystrixCommand<InputStream> command = new CommandHelperTestCommand();

        final StreamingResponseBody responseBody = CommandHelper.toStreaming(command);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        responseBody.writeTo(outputStream);
        assertEquals("test", new String(outputStream.toByteArray()));
    }

    @Test
    public void testCommandToPublisher() throws Exception {
        // Given
        HystrixCommand<InputStream> command = new InputStreamTestCommand();
        final Publisher<CommandHelperTestData> publisher = CommandHelper.toPublisher(CommandHelperTestData.class, mapper,
                command);
        final List<CommandHelperTestData> receivedData = new ArrayList<>();

        // When
        Flux.from(publisher).subscribe(receivedData::add);

        // Then
        assertEquals(3, receivedData.size());
        assertEquals("1", receivedData.get(0).getValue());
        assertEquals("2", receivedData.get(1).getValue());
        assertEquals("3", receivedData.get(2).getValue());
    }

    @Test
    public void testMalformedToPublisher() throws Exception {
        // Given
        HystrixCommand<InputStream> command = new MalformedInputStreamTestCommand();
        final Publisher<CommandHelperTestData> publisher = CommandHelper.toPublisher(CommandHelperTestData.class, mapper,
                command);
        final List<CommandHelperTestData> receivedData = new ArrayList<>();

        // When
        try {
            Flux.from(publisher).subscribe(receivedData::add);
            fail();
        } catch (Exception e) {
            assertTrue(receivedData.isEmpty());
        }
    }

    private static class CommandHelperTestCommand extends GenericCommand<InputStream> {

        private CommandHelperTestCommand() {
            super(HystrixCommandGroupKey.Factory.asKey("test"));
        }

        @Override
        protected InputStream run() throws Exception {
            return new ByteArrayInputStream("test".getBytes());
        }

        @Override
        public HttpStatus getStatus() {
            return HttpStatus.NO_CONTENT;
        }

        @Override
        public Header[] getCommandResponseHeaders() {
            return new Header[] { new BasicHeader("Custom", "custom value") };
        }
    }

    private class MalformedInputStreamTestCommand extends HystrixCommand<InputStream> {

        private MalformedInputStreamTestCommand() {
            super(() -> "Test");
        }

        @Override
        public InputStream execute() {
            try {
                return run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected InputStream run() throws Exception {
            return new ByteArrayInputStream("[{".getBytes());
        }
    }

    private class InputStreamTestCommand extends HystrixCommand<InputStream> {

        private InputStreamTestCommand() {
            super(() -> "Test");
        }

        @Override
        public InputStream execute() {
            try {
                return run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected InputStream run() throws Exception {
            final List<CommandHelperTestData> tList = Arrays.asList(new CommandHelperTestData("1"),
                    new CommandHelperTestData("2"), new CommandHelperTestData("3"));
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            mapper.writer().writeValue(out, tList);
            out.flush();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
