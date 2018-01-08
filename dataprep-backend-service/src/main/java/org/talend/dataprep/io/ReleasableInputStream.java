//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.io;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleasableInputStream extends InputStream {

    private static final Logger LOG = LoggerFactory.getLogger(ReleasableInputStream.class);

    private final InputStream delegate;

    private final Runnable onClose;

    private boolean isClosed;

    public ReleasableInputStream(InputStream delegate, Runnable onClose) {
        this.delegate = delegate;
        this.onClose = onClose;
    }

    @Override
    public int read() throws IOException {
        try {
            int read = delegate.read();
            if (read < 0) {
                safeClose();
            }
            return read;
        } catch (IOException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public int read(@Nonnull byte[] bytes) throws IOException {
        try {
            int read = delegate.read(bytes);
            if (read < 0) {
                safeClose();
            }
            return read;
        } catch (IOException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public int read(@Nonnull byte[] bytes, int i, int i1) throws IOException {
        try {
            int read = delegate.read(bytes, i, i1);
            if (read < 0) {
                safeClose();
            }
            return read;
        } catch (IOException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public long skip(long l) throws IOException {
        try {
            return delegate.skip(l);
        } catch (IOException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return delegate.available();
        } catch (IOException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            delegate.close();
        } finally {
            safeClose();
        }
    }

    private synchronized void safeClose() {
        if (isClosed) {
            return;
        }
        try {
            LOG.debug("Safe close on stream using {}", onClose);
            onClose.run();
            isClosed = true;
        } catch (Exception e) {
            LOG.error("Unable to invoke onClose closure.", e);
        }
    }

    @Override
    public void mark(int i) {
        try {
            delegate.mark(i);
        } catch (RuntimeException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public void reset() throws IOException {
        try {
            delegate.reset();
        } catch (IOException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }
}
