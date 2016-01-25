package org.talend.dataprep.cache;

/**
 * Content cache key used to group all information needed by the cache.
 */
public interface ContentCacheKey {

    /**
     * The key must be unique per content !
     *
     * @return the key for this cache content as a string.
     */
    String getKey();

}
