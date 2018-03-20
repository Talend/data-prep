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

package org.talend.dataprep.cache.noop;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.talend.daikon.content.ContentServiceEnabled;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.cache.ContentCacheKey;

/**
 * An implementation of {@link ContentCache} that holds no content.
 *
 * This is mainly an implementation to replace an actual content cache in case configuration does not include HDFS
 * configuration.
 */
@Component
@ConditionalOnMissingBean(ContentServiceEnabled.class)
public class NoOpContentCache implements ContentCache {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpContentCache.class);

    /**
     * Default empty constructor.
     */
    public NoOpContentCache() {
        LOGGER.info("Using content cache: {}", this.getClass().getName());
    }

    /**
     * @see ContentCache#has(ContentCacheKey)
     */
    @Override
    public boolean has(ContentCacheKey key) {
        return false;
    }

    /**
     * @see ContentCache#get(ContentCacheKey)
     */
    @Override
    public InputStream get(ContentCacheKey key) {
        return new NullInputStream(0);
    }

    /**
     * @see ContentCache#put(ContentCacheKey, TimeToLive)
     */
    @Override
    public OutputStream put(ContentCacheKey key, TimeToLive timeToLive) {
        return new NullOutputStream();
    }

    /**
     * @see ContentCache#evict(ContentCacheKey)
     */
    @Override
    public void evict(ContentCacheKey key) {
        // Nothing to do.
    }

    /**
     * @see ContentCache#evictMatch(ContentCacheKey)
     */
    @Override
    public void evictMatch(ContentCacheKey key) {
        // Nothing to do.
    }

    @Override
    public void move(ContentCacheKey from, ContentCacheKey to, TimeToLive toTimeToLive) {
        // Nothing to do.
    }

    /**
     * @see ContentCache#clear()
     */
    @Override
    public void clear() {
        // Nothing to do.
    }
}
