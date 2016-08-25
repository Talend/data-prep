package org.talend.dataprep.command;

import com.netflix.hystrix.HystrixCommand;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.http.HttpResponseContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CommandHelper {

    private CommandHelper() {
    }

    public static StreamingResponseBody toStreaming(final HystrixCommand<InputStream> command) {
        return outputStream -> {
            IOUtils.copyLarge(command.execute(), outputStream);
            outputStream.flush();
        };
    }

    public static StreamingResponseBody toStreaming(final GenericCommand<InputStream> command) {
        final InputStream stream = command.execute();
        // copy all headers from the command response so that the mime-type is correctly forwarded for instance
        for (Header header : command.getCommandResponseHeaders()) {
            HttpResponseContext.header(header.getName(), header.getValue());
        }
        return outputStream -> {
            IOUtils.copyLarge(stream, outputStream);
            outputStream.flush();
        };
    }
}
