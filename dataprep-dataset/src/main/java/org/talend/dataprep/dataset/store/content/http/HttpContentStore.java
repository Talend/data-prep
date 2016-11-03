//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.dataset.store.content.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.location.HttpLocation;
import org.talend.dataprep.dataset.store.content.DataSetContentStore;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;

/**
 * Remote http dataset content store implementation.
 */
@Component("ContentStore#http")
public class HttpContentStore extends DataSetContentStore {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpContentStore.class);

    /** The http client to use. */
    @Autowired
    private CloseableHttpClient httpClient;

    /**
     * @see DataSetContentStore#getAsRaw(DataSetMetadata)
     */
    @Override
    public InputStream getAsRaw(DataSetMetadata dataSetMetadata, long limit) {
        HttpLocation location = (HttpLocation) dataSetMetadata.getLocation();
        HttpGet get = new HttpGet(location.getUrl());
        get.setHeader("Accept","*/*");
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new IOException("error fetching " + location.getUrl() + " -> " + response.getStatusLine());
            }
            LOGGER.debug("HTTP remote dataset {} fetched from {}", dataSetMetadata, location.getUrl());
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_REMOTE_DATASET_CONTENT, e);
        }
    }

    /**
     * @see DataSetContentStore#storeAsRaw(DataSetMetadata, InputStream)
     */
    @Override
    public void storeAsRaw(DataSetMetadata dataSetMetadata, InputStream dataSetContent) {
        // nothing to do here since the dataset is already stored
    }

    /**
     * @see DataSetContentStore#delete(DataSetMetadata)
     */
    @Override
    public void delete(DataSetMetadata dataSetMetadata) {
        // nothing to do here
        LOGGER.warn("delete called on a remote http content store... (stack trace is informative)", new Exception());
    }

    /**
     * @see DataSetContentStore#clear()
     */
    @Override
    public void clear() {
        // nothing to do here...
        LOGGER.warn("clear called on a remote http content store... (stack trace is informative)", new Exception());
    }
}
