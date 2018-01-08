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

package org.talend.dataprep.dataset.store.content;

import java.io.IOException;
import java.io.InputStream;

import org.talend.dataprep.exception.TDPException;

/**
 * A bounded input stream that throws a {@link TDPException} when the maximum input stream size is exceeded.
 */
public class StrictlyBoundedInputStream extends InputStream {

    /**
     * The original input stream
     */
    private final InputStream in;

    /**
     * The maximum authorized size
     */
    private final long maxSize;

    /**
     * The number of bytes so far read
     */
    private long total;

    /**
     * Creates a new BoundedInputStream that wraps the given input stream.
     * If the maximum size which is specified is less or equal to zero, the constructed {@link InputStream} is unbounded.
     *
     * @param in the original input stream to be wrapped by this {@link StrictlyBoundedInputStream}
     * @param maxSize the maximum size of this input stream
     */
    public StrictlyBoundedInputStream(InputStream in, long maxSize) {
        this.in = in;
        this.maxSize = maxSize < 0 ? Long.MAX_VALUE : maxSize;
    }

    public long getMaxSize() {
        return maxSize;
    }

    /**
     * @return the total numuber of bytes that have been read so far
     */
    public long getTotal() {
        return total;
    }

    @Override
    public int read() throws IOException {
        int i = in.read();
        if (i >= 0) {
            incrementCounter(1);
        }
        return i;
    }

    /**
     * Increment the number of bytes, so far read, with the specified number.
     *
     * @param size the number of bytes which have been read
     * @throws IOException
     */
    private void incrementCounter(int size) throws IOException {
        total += size;
        if (total > maxSize) {
            throw new InputStreamTooLargeException(maxSize);
        }
    }

    public static class InputStreamTooLargeException extends RuntimeException {

        private long maxSize;

        public InputStreamTooLargeException(long maxSize) {
            super("The input stream exceeds the authorized size: " + maxSize);
            this.maxSize = maxSize;
        }

        /**
         * @return the MaxSize
         */
        public long getMaxSize() {
            return maxSize;
        }

    }

}
