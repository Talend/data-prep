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

package org.talend.dataprep.cache;

/**
 *
 */

public class DataSetSampleCacheKey implements ContentCacheKey {

    public static final String PREFIX = "dataset-sample_";

    /** The dataset id. */
    private String dataSetId;

    /**
     * Default constructor.
     *
     * @param dataSetId the dataset id.
     */
    public DataSetSampleCacheKey(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    /**
     * @see ContentCacheKey#getKey()
     */
    @Override
    public String getKey() {
        return PREFIX + dataSetId;
    }
}
