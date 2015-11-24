package org.talend.dataprep.dataset.store.content;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetContent;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.schema.FormatGuess;
import org.talend.dataprep.schema.Serializer;

/**
 * Abstract class for DataSetContentStore implementations.
 */
public abstract class DataSetContentStoreAdapter implements DataSetContentStore {

    /** Format guesser factory. */
    @Autowired
    protected FormatGuess.Factory factory;

    /**
     * @see DataSetContentStore#get(DataSetMetadata)
     */
    @Override
    public InputStream get(DataSetMetadata dataSetMetadata) {
        DataSetContent content = dataSetMetadata.getContent();
        Serializer serializer = factory.getFormatGuess(content.getFormatGuessId()).getSerializer();
        try (InputStream inputStream = getAsRaw(dataSetMetadata)) {
            return serializer.serialize( inputStream, dataSetMetadata );
        } catch ( IOException e ) {
            throw new RuntimeException( e.getMessage(), e);
        }
    }
}
