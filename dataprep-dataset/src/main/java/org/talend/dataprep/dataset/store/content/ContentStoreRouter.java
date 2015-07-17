package org.talend.dataprep.dataset.store.content;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSetMetadata;

/**
 * Routes the requests to the targeted content store.
 *
 * Routing is achieved from the dataset metadata location and the content store component name.
 *
 * All DataSetContentStore implementation must have a @Component("ContentStore#{locationType}") annotation.
 */
@Service
public class ContentStoreRouter implements DataSetContentStore {

    /** Content store name prefix. */
    private static final String STORE_PREFIX = "ContentStore#";

    /** The application context needed to retrieve content stores. */
    @Autowired
    private ApplicationContext context;

    /**
     * @see DataSetContentStore#storeAsRaw(DataSetMetadata, InputStream)
     */
    @Override
    public void storeAsRaw(DataSetMetadata dataSetMetadata, InputStream dataSetContent) {
        DataSetContentStore target = getContentStore(dataSetMetadata);
        target.storeAsRaw(dataSetMetadata, dataSetContent);
    }

    /**
     * @see DataSetContentStore#get(DataSetMetadata)
     */
    @Override
    public InputStream get(DataSetMetadata dataSetMetadata) {
        DataSetContentStore target = getContentStore(dataSetMetadata);
        return target.get(dataSetMetadata);
    }

    /**
     * @see DataSetContentStore#getAsRaw(DataSetMetadata)
     */
    @Override
    public InputStream getAsRaw(DataSetMetadata dataSetMetadata) {
        DataSetContentStore target = getContentStore(dataSetMetadata);
        return target.getAsRaw(dataSetMetadata);
    }

    /**
     * @see DataSetContentStore#delete(DataSetMetadata)
     */
    @Override
    public void delete(DataSetMetadata dataSetMetadata) {
        DataSetContentStore target = getContentStore(dataSetMetadata);
        target.delete(dataSetMetadata);
    }

    /**
     * @see DataSetContentStore#clear()
     */
    @Override
    public void clear() {
        // find the local store and clear it
        DataSetContentStore target = getLocalContentStore();
        target.clear();
    }

    /**
     * Return the DataSetContentStore that deals with this dataset metadata.
     *
     * @param dataSetMetadata the dataset metadata to store the content.
     * @return the DataSetContentStore that deals with this dataset metadata.
     */
    private DataSetContentStore getContentStore(DataSetMetadata dataSetMetadata) {
        String storeName = STORE_PREFIX + dataSetMetadata.getLocation().getLocationType();
        return context.getBean(storeName, DataSetContentStore.class);
    }

    /**
     * @return the local content store.
     */
    private DataSetContentStore getLocalContentStore() {
        String storeName = STORE_PREFIX + "local";
        return context.getBean(storeName, DataSetContentStore.class);
    }
}
