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

package org.talend.dataprep.dataset.event;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.cache.ContentCacheKey;
import org.talend.dataprep.event.CacheEventProcessingUtil;

@Component
public class DatasetEventUtil {

    @Autowired
    private CacheEventProcessingUtil cacheEventProcessingUtil;

    @Autowired
    private AnalysisEventProcessingUtil analysisEventProcessingUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetEventUtil.class);

    public void performUpdateEvent(String datasetId) {
        this.cleanDatasetCache(datasetId);
        this.analyseDataset(datasetId);
    }

    public void performImportEvent(String datasetId) {
        this.analyseDataset(datasetId);
    }

    private void analyseDataset(String datasetId) {
        analysisEventProcessingUtil.processAnalysisEvent(datasetId);
    }

    private void cleanDatasetCache(String datasetId) {
        // when we update a dataset we need to clean cache
        final ContentCacheKey sampleKey = () -> "dataset-sample_" + datasetId;
        LOGGER.debug("Evicting sample cache entry for #{}", datasetId);
        cacheEventProcessingUtil.processCleanCacheEvent(sampleKey, Boolean.FALSE);
        LOGGER.debug("Evicting sample cache entry for #{} done.", datasetId);

        LOGGER.debug("Evicting transformation cache entry for dataset #{}", datasetId);
        final ContentCacheKey matchDatasetKey = new ContentCacheKey() {

            @Override
            public String getKey() {
                return datasetId;
            }

            @Override
            public Predicate<String> getMatcher() {
                String regex = ".*_" + getKey() + "_.*";

                // Build regular expression matcher
                final Pattern pattern = Pattern.compile(regex);
                return str -> pattern.matcher(str).matches();
            }

        };
        cacheEventProcessingUtil.processCleanCacheEvent(matchDatasetKey, Boolean.TRUE);
        LOGGER.debug("Evicting transformation cache entry for dataset  #{} done.", datasetId);
    }
}
