package org.talend.dataprep.api.service.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloneInputStream extends InputStream {

    private static final Logger LOG = LoggerFactory.getLogger(CloneInputStream.class);

    private final InputStream inputStream;

    private final Collection<OutputStream> destinations;

    public CloneInputStream(InputStream inputStream, OutputStream... destinations) {
        this.inputStream = inputStream;
        this.destinations = Arrays.asList(destinations);
    }

    private static void flushDestinations(Collection<OutputStream> destinations) {
        destinations.forEach(out -> {
            try {
                out.flush();
            } catch (IOException e) {
                LOG.error("Unable to write to '" + out + "'.", e);
            }
        });
    }

    private static void closeDestinations(Collection<OutputStream> destinations) {
        destinations.forEach(out -> {
            try {
                out.close();
            } catch (IOException e) {
                LOG.error("Unable to write to '" + out + "'.", e);
            }
        });
    }

    private static void writeDestinations(Collection<OutputStream> destinations, byte[] bytes, int read) {
        byte[] readBytes = Arrays.copyOf(bytes, read);
        destinations.forEach(out -> {
            try {
                out.write(readBytes);
            } catch (IOException e) {
                LOG.error("Unable to write to '" + out + "'.", e);
            }
        });
    }

    @Override
    public int read() throws IOException {
        int read = inputStream.read();
        if (read > 0) {
            writeDestinations(destinations, new byte[] {(byte) read}, 1);
        } else {
            flushDestinations(destinations);
        }
        return read;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        int read = inputStream.read(bytes);
        if (read > 0) {
            writeDestinations(destinations, bytes, read);
        } else {
            flushDestinations(destinations);
        }
        return read;
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        int read = inputStream.read(bytes, i, i1);
        if (read > 0) {
            writeDestinations(destinations, bytes, read);
        } else {
            flushDestinations(destinations);
        }
        return read;
    }

    @Override
    public long skip(long l) throws IOException {
        return inputStream.skip(l);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        flushDestinations(destinations);
        closeDestinations(destinations);
    }

    @Override
    public void mark(int i) {
        inputStream.mark(i);
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}
