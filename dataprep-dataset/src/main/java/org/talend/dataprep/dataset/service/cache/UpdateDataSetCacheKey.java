package org.talend.dataprep.dataset.service.cache;

import org.talend.dataprep.cache.ContentCacheKey;

/**
 * Content cache key used when updating a dataset.
 */
public class UpdateDataSetCacheKey implements ContentCacheKey {

    public static final String PREFIX = "dataset-update_";

    /** The dataset id. */
    private String dataSetId;

    /**
     * Default constructor.
     *
     * @param dataSetId the dataset id.
     */
    public UpdateDataSetCacheKey(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getKey() {
        return PREFIX + dataSetId;
    }
}
