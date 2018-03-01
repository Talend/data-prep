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

package org.talend.dataprep.transformation.api.transformer;

import java.io.IOException;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * Writer used to write transformed datasets. This interface provides an common abstraction of the output format (JSON,
 * CSV, Excel...).
 */
public interface TransformerWriter extends AutoCloseable {

    /**
     * Write the given RowMetadata.
     *
     * @param columns the row metadata to write.
     * @throws IOException if an unexpected error occurs.
     */
    void write(RowMetadata columns) throws IOException;

    /**
     * Write the given row.
     *
     * @param row the row to write.
     * @throws IOException if an unexpected error occurs.
     */
    void write(DataSetRow row) throws IOException;

    /**
     * Start to write an array.
     *
     * @throws IOException if an unexpected error occurs.
     */
    default void startArray() throws IOException {
        // default implementation to ease implementations development
    }

    /**
     * End to write an array.
     *
     * @throws IOException if an unexpected error occurs.
     */
    default void endArray() throws IOException {
        // default implementation to ease implementations development
    }

    /**
     * Start to write an object.
     *
     * @throws IOException if an unexpected error occurs.
     */
    default void startObject() throws IOException {
        // default implementation to ease implementations development
    }

    /**
     * End to write an object.
     *
     * @throws IOException if an unexpected error occurs.
     */
    default void endObject() throws IOException {
        // default implementation to ease implementations development
    }

    /**
     * Write a field name.
     *
     * @throws IOException if an unexpected error occurs.
     */
    default void fieldName(String columns) throws IOException {
        // default implementation to ease implementations development
    }

    /**
     * Flush the current writing.
     *
     * @throws IOException if an unexpected error occurs.
     */
    default void flush() throws IOException {
        // default implementation to ease implementations development
    }

    /**
     * Close the data stream properly when all data is written or a stop signal is received. Not meant to cloase the OutputStream.
     *
     * @throws IOException
     */
    default void close() throws IOException {
        // default implementation to ease implementations development
    }

}
