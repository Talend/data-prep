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

package org.talend.dataprep.dataset.store.content;

import java.io.InputStream;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;

/**
 * Routes the requests to the targeted content store.
 *
 * Routing is achieved from the dataset metadata location and the content store component name.
 *
 * All DataSetContentStore implementation must have a @Component("ContentStore#{locationType}") annotation.
 */
@Service
public class ContentStoreRouter extends DataSetContentStore {

    /** Content store name prefix. */
    private static final String STORE_PREFIX = "ContentStore#";

    /** A component to limit data set content limit (or not, depending on edition). */
    @Autowired
    DataSetContentLimit limit;

    /** The application context needed to retrieve content stores. */
    @Autowired
    private ApplicationContext context;

    /**
     * @see DataSetContentStore#storeAsRaw(DataSetMetadata, InputStream)
     */
    @Override
    public void storeAsRaw(DataSetMetadata dataSetMetadata, InputStream dataSetContent) {
        DataSetContentStore target = wrapStore(dataSetMetadata);
        target.storeAsRaw(dataSetMetadata, dataSetContent);
    }

    /**
     * @see DataSetContentStore#get(DataSetMetadata)
     */
    @Override
    public InputStream get(DataSetMetadata dataSetMetadata) {
        DataSetContentStore target = wrapStore(dataSetMetadata);
        return target.get(dataSetMetadata);
    }

    /**
     * @see DataSetContentStore#getAsRaw(DataSetMetadata)
     */
    @Override
    public InputStream getAsRaw(DataSetMetadata dataSetMetadata) {
        DataSetContentStore target = wrapStore(dataSetMetadata);
        return target.getAsRaw(dataSetMetadata);
    }

    /**
     * @see DataSetContentStore#delete(DataSetMetadata)
     */
    @Override
    public void delete(DataSetMetadata dataSetMetadata) {
        DataSetContentStore target = wrapStore(dataSetMetadata);
        target.delete(dataSetMetadata);
    }

    @Override
    public Stream<DataSetRow> stream( DataSetMetadata dataSetMetadata )
    {
        DataSetContentStore target = wrapStore(dataSetMetadata);
        return target.stream(dataSetMetadata);
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
    private DataSetContentStore wrapStore(DataSetMetadata dataSetMetadata) {
        String storeName = STORE_PREFIX + dataSetMetadata.getLocation().getLocationType();
        final DataSetContentStore contentStore = context.getBean(storeName, DataSetContentStore.class);
        return limit.get(contentStore);
    }

    /**
     * @return the local content store.
     */
    private DataSetContentStore getLocalContentStore() {
        final DataSetContentStore contentStore = context.getBean(STORE_PREFIX + "local", DataSetContentStore.class);
        return limit.get(contentStore);
    }
}
