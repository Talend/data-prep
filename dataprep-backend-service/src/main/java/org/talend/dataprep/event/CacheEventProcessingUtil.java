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

package org.talend.dataprep.event;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;

@Component
public class CacheEventProcessingUtil {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = getLogger(CacheEventProcessingUtil.class);

    @Autowired
    private ContentCache cache;

    /**
     * Processing clean cache event
     *
     * @param cacheKey the key to clean on cache
     */
    public void processCleanCacheEvent(ContentCacheKey cacheKey, Boolean isPartialKey) {
        if (cacheKey != null && StringUtils.isNotEmpty(cacheKey.getKey())) {
            if (isPartialKey) {
                cache.evictMatch(cacheKey);
            } else {
                cache.evict(cacheKey);
            }
            LOGGER.info("Deleting content cache key {} because receiving CleanCacheEvent", cacheKey);
        } else {
            LOGGER.debug("Deleting all cache because we don't have key");
            cache.clear();
        }
    }
}
