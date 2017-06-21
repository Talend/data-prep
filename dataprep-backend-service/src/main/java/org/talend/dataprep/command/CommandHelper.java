package org.talend.dataprep.command;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.http.HttpResponseContext;

import com.netflix.hystrix.HystrixCommand;

public class CommandHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHelper.class);

    private CommandHelper() {
    }

    public static StreamingResponseBody toStreaming(final HystrixCommand<InputStream> command) {
        return outputStream -> {
            final InputStream commandResult = command.execute();
            try {
                IOUtils.copyLarge(commandResult, outputStream);
                outputStream.flush();
            } catch (IOException ioe) {
                try {
                    commandResult.close();
                } catch (IOException closingException) {
                    LOGGER.warn("could not close command result, a http connection may be leaked !", closingException);
                }
                LOGGER.error("Unable to fully copy command result '{}'.", command.getClass(), ioe);
            }
        };
    }

    public static StreamingResponseBody toStreaming(final GenericCommand<InputStream> command) {
        final InputStream stream = command.execute();
        // copy all headers from the command response so that the mime-type is correctly forwarded for instance
        for (Header header : command.getCommandResponseHeaders()) {
            HttpResponseContext.header(header.getName(), header.getValue());
        }
        return outputStream -> {
            try {
                IOUtils.copyLarge(stream, outputStream);
                outputStream.flush();
            } catch (IOException ioe) {
                try {
                    stream.close();
                } catch (IOException closingException) {
                    LOGGER.warn("could not close command result, a http connection may be leaked !", closingException);
                }
                LOGGER.error("Unable to fully copy command result '{}'.", command.getClass(), ioe);
            }
        };
    }
}
