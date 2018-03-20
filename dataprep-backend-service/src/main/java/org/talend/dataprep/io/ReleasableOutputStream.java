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

package org.talend.dataprep.io;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleasableOutputStream extends OutputStream {

    private static final Logger LOG = LoggerFactory.getLogger(ReleasableOutputStream.class);

    private final OutputStream delegate;

    private final Runnable onClose;

    private boolean isClosed;

    public ReleasableOutputStream(OutputStream delegate, Runnable onClose) {
        this.delegate = delegate;
        this.onClose = onClose;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            delegate.write(b);
        } catch (IOException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            delegate.write(b);
        } catch (IOException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            delegate.write(b, off, len);
        } catch (IOException e) {
            safeClose();
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            delegate.flush();
        } catch (IOException e) {
            safeClose();
            isClosed = true;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            delegate.close();
        } finally {
            safeClose();
            isClosed = true;
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
}
