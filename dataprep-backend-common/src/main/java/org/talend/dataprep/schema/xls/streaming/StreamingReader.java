// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.schema.xls.streaming;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.monitorjbl.xlsx.exceptions.OpenException;
import com.monitorjbl.xlsx.exceptions.ReadException;

/**
 * Streaming Excel workbook implementation. Most advanced features of POI are not supported. Use this only if your
 * application can handle iterating through an entire workbook, row by row.
 */
public class StreamingReader implements Iterable<Row>, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger( StreamingReader.class);

    private File tmp;

    private final StreamingWorkbookReader workbook;

    public StreamingReader(StreamingWorkbookReader workbook) {
        this.workbook = workbook;
    }

    /**
     * Returns a new streaming iterator to loop through rows. This iterator is not guaranteed to have all rows in
     * memory, and any particular iteration may trigger a load from disk to read in new data.
     *
     * @return the streaming iterator
     */
    @Override
    public Iterator<Row> iterator() {
        return workbook.first().iterator();
    }

    /**
     * Closes the streaming resource, attempting to clean up any temporary files created.
     *
     * @throws com.monitorjbl.xlsx.exceptions.CloseException if there is an issue closing the stream
     */
    @Override
    public void close() {
        try {
            workbook.close();
        } finally {
            if (tmp != null) {
                LOGGER.debug( "Deleting tmp file [{}]", tmp.getAbsolutePath());
                tmp.delete();
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int rowCacheSize = 10;

        private int bufferSize = 1024;

        private int sheetIndex = 0;

        private String sheetName;

        private String password;

        public int getRowCacheSize() {
            return rowCacheSize;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public String getPassword() {
            return password;
        }

        /**
         * The number of rows to keep in memory at any given point.
         * <p>
         * Defaults to 10
         * </p>
         *
         * @param rowCacheSize number of rows
         * @return reference to current {@code Builder}
         */
        public Builder rowCacheSize(int rowCacheSize) {
            this.rowCacheSize = rowCacheSize;
            return this;
        }

        /**
         * The number of bytes to read into memory from the input resource.
         * <p>
         * Defaults to 1024
         * </p>
         *
         * @param bufferSize buffer size in bytes
         * @return reference to current {@code Builder}
         */
        public Builder bufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        /**
         * For password protected files specify password to open file. If the password is incorrect a
         * {@code ReadException} is thrown on {@code read}.
         * <p>
         * NULL indicates that no password should be used, this is the default value.
         * </p>
         *
         * @param password to use when opening file
         * @return reference to current {@code Builder}
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Reads a given {@code InputStream} and returns a new instance of {@code Workbook}. Due to Apache POI
         * limitations, a temporary file must be written in order to create a streaming iterator. This process will use
         * the same buffer size as specified in {@link #bufferSize(int)}.
         *
         * @param is input stream to read in
         * @return A {@link Workbook} that can be read from
         * @throws ReadException if there is an issue reading the stream
         */
        public Workbook open(InputStream is) {
            StreamingWorkbookReader workbook = new StreamingWorkbookReader(this);
            workbook.init(is);
            return new StreamingWorkbook(workbook);
        }

        /**
         * Reads a given {@code File} and returns a new instance of {@code Workbook}.
         *
         * @param file file to read in
         * @return built streaming reader instance
         * @throws OpenException if there is an issue opening the file
         * @throws ReadException if there is an issue reading the file
         */
        public Workbook open(File file) {
            StreamingWorkbookReader workbook = new StreamingWorkbookReader(this);
            workbook.init(file);
            return new StreamingWorkbook(workbook);
        }
    }

}
