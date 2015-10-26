package org.talend.dataprep.cache;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * An implementation of {@link ContentCache} that holds no content. This is mainly an implementation to replace an
 * actual content cache in case configuration does not include HDFS configuration.
 */
@Component
@ConditionalOnProperty(name = "api.cache", havingValue = "disabled", matchIfMissing = true)
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
        return null;
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
     * @see ContentCache#clear()
     */
    @Override
    public void clear() {
        // Nothing to do.
    }
}
