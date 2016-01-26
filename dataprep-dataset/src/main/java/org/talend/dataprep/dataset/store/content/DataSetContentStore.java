package org.talend.dataprep.dataset.store.content;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.json.DataSetRowIterator;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.schema.Serializer;

/**
 * Base class for DataSet content stores.
 */
public abstract class DataSetContentStore {

    /** Format guesser factory. */
    @Autowired
    protected FormatGuess.Factory factory;

    /** DataPrep ready jackson builder. */
    @Autowired
    protected Jackson2ObjectMapperBuilder builder;

    /**
     * Stores (persists) a data set raw content to a storage. The only expectation is for {@link #get(DataSetMetadata)}
     * to return content after this method ends.
     *
     * @param dataSetMetadata The data set metadata attached to the {@link DataSetMetadata data set}.
     * @param dataSetContent Content of the data set.
     * @see #get(DataSetMetadata)
     * @see #delete(DataSetMetadata)
     */
    public abstract void storeAsRaw(DataSetMetadata dataSetMetadata, InputStream dataSetContent);

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
    protected InputStream get(DataSetMetadata dataSetMetadata) {
        DataSetContent content = dataSetMetadata.getContent();
        Serializer serializer = factory.getFormatGuess(content.getFormatGuessId()).getSerializer();
        return serializer.serialize(getAsRaw(dataSetMetadata), dataSetMetadata);

    }

    /**
     * Similarly to {@link #get(DataSetMetadata)} returns the content of the data set but as a {@link Stream stream} of
     * {@link DataSetRow rows} instead of JSON content.
     * 
     * @param dataSetMetadata The {@link DataSetMetadata data set} to read rows from.
     * @return A valid <b>{@link DataSetRow}</b> stream.
     */
    public final Stream<DataSetRow> stream(DataSetMetadata dataSetMetadata) {
        final InputStream inputStream = get(dataSetMetadata);
        final DataSetRowIterator iterator = new DataSetRowIterator(inputStream, true);
        final Iterable<DataSetRow> rowIterable = () -> iterator;
        Stream<DataSetRow> dataSetRowStream = StreamSupport.stream(rowIterable.spliterator(), false);

        // deal with dataset size limit (ignored if limit is <= 0)
        final Optional<Long> limit = dataSetMetadata.getContent().getLimit();
        if (limit.isPresent() && limit.get() <= 0) {
            dataSetRowStream = dataSetRowStream.limit(limit.get());
        }

        // make sure to close the original input stream when closing this one
        dataSetRowStream = dataSetRowStream.onClose(() -> {
            try {
                inputStream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return dataSetRowStream;
    }

    /**
     * Return a sample of the given size for the wanted dataset.
     *
     * @param dataSetMetadata The {@link DataSetMetadata data set} to read rows from.
     * @param size the wanted sample size.
     * @return A valid <b>{@link DataSetRow}</b> stream sample.
     */
    public Stream<DataSetRow> sample(DataSetMetadata dataSetMetadata, long size) {
        return stream(dataSetMetadata).limit(size);
    }

    /**
     * Returns the {@link DataSetMetadata data set} content as "raw" (i.e. the content supplied by user upon data set
     * creation).
     * 
     * @param dataSetMetadata The {@link DataSetMetadata data set} to read content from.
     * @return The content associated with <code>dataSetMetadata</code>.
     */
    public abstract InputStream getAsRaw(DataSetMetadata dataSetMetadata);

    /**
     * Deletes the {@link DataSetMetadata data set}. No recovery operation is expected.
     * 
     * @param dataSetMetadata The data set to delete.
     */
    public abstract void delete(DataSetMetadata dataSetMetadata);

    /**
     * Removes all stored content. No recovery operation is expected.
     */
    public abstract void clear();
}
