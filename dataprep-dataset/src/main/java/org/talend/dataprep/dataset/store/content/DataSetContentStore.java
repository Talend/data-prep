package org.talend.dataprep.dataset.store.content;

import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.json.DataSetRowIterator;

public interface DataSetContentStore {

    /**
     * Stores (persists) a data set raw content to a storage. The only expectation is for {@link #get(DataSetMetadata)}
     * to return content after this method ends.
     *
     * @param dataSetMetadata The data set metadata attached to the {@link DataSetMetadata data set}.
     * @param dataSetContent Content of the data set.
     * @see #get(DataSetMetadata)
     * @see #delete(DataSetMetadata)
     */
    void storeAsRaw(DataSetMetadata dataSetMetadata, InputStream dataSetContent);

    /**
     * Returns the {@link DataSetMetadata data set} content as <b>JSON</b> format. Whether data set content was JSON or
     * not, method is expected to provide a JSON output. It's up to the implementation to:
     * <ul>
     * <li>Convert data content to JSON.</li>
     * <li>Throw an exception if data set is not ready for read (content type missing).</li>
     * </ul>
     * Implementations are also encouraged to implement method with no blocking code.
     * 
     * @param dataSetMetadata The {@link DataSetMetadata data set} to read content from.
     * @return A valid <b>JSON</b> stream. It is a JSON array where each element in the array contains a single data set
     * row (it does not mean there's a line in input stream per data set row, a data set row might be split on multiple
     * rows in stream).
     */
    InputStream get(DataSetMetadata dataSetMetadata);

    /**
     * Similarly to {@link #get(DataSetMetadata)} returns the content of the data set but as a {@link Stream stream} of
     * {@link DataSetRow rows} instead of JSON content.
     * 
     * @param dataSetMetadata The {@link DataSetMetadata data set} to read rows from.
     * @return A valid <b>{@link DataSetRow}</b> stream.
     */
    default Stream<DataSetRow> stream(DataSetMetadata dataSetMetadata) {
        final Iterable<DataSetRow> rowIterable = () -> new DataSetRowIterator(get(dataSetMetadata));
        return StreamSupport.stream(rowIterable.spliterator(), false);
    }

    /**
     * Returns the {@link DataSetMetadata data set} content as "raw" (i.e. the content supplied by user upon data set
     * creation).
     * 
     * @param dataSetMetadata The {@link DataSetMetadata data set} to read content from.
     * @return The content associated with <code>dataSetMetadata</code>.
     */
    InputStream getAsRaw(DataSetMetadata dataSetMetadata);

    /**
     * Deletes the {@link DataSetMetadata data set}. No recovery operation is expected.
     * 
     * @param dataSetMetadata The data set to delete.
     */
    void delete(DataSetMetadata dataSetMetadata);

    /**
     * Removes all stored content. No recovery operation is expected.
     */
    void clear();
}
