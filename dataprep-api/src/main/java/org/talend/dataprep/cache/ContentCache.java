package org.talend.dataprep.cache;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * A component to hold versions of a preparation at different steps. Each implementation may implement different
 * eviction strategies that do not need to surface here.
 */
public interface ContentCache {

    /**
     * Check whether a cached content exists for given <code>preparationId</code> at step <code>stepId</code> for a
     * specific sample size.
     * 
     * @param key content cache key.
     * @return <code>true</code> if cache holds content for given parameters, <code>false</code> otherwise.
     */
    boolean has(ContentCacheKey key);

    /**
     * Returns the cached content for given <code>preparationId</code> at step <code>stepId</code>
     *
     * @param key content cache key.
     * @return The cached content for given parameters, or <code>null</code> if not in the cache.
     * @throws IllegalArgumentException If no cache can be found for given parameters.
     * @see #has(ContentCacheKey)
     */
    InputStream get(ContentCacheKey key);

    /**
     * Allow callers to create an entry in cache for given <code>preparationId</code> at step <code>stepId</code>.
     * Please note content is not passed in parameters but return of this method also callers to write in entry.
     *
     * @param key content cache key.
     * @param timeToLive The {@link TimeToLive TTL} for the new cache entry.
     * @return A {@link OutputStream output stream} to be used to write content in cache entry
     */
    OutputStream put(ContentCacheKey key, TimeToLive timeToLive);

    /**
     * Mark cache entry as invalid for given <code>preparationId</code> at step <code>stepId</code>. After this method
     * completes, {@link #has(ContentCacheKey)} must immediately return <code>false</code>.
     *
     * The eviction is performed gradually according to the given key : the more precise the key is, the finer the
     * eviction.
     *
     * Here is the order : dataset.id / preparation.id / step.id / sample size
     *
     * For instance, if the step id is null in the key, all cache entries down to the preparation are evicted.
     *
     * @param key content cache key.
     */
    void evict(ContentCacheKey key);

    /**
     * Removes all content cached by this {@link ContentCache cache}.
     */
    void clear();

    /**
     * Configure how long a cache entry may exist in cache.
     */
    enum TimeToLive {
        /**
         * Default time to live for a content in cache (1 hour).
         */
        DEFAULT(TimeUnit.HOURS.toMillis(1)),
        /**
         * Short time to live (short period -> 1 minute).
         */
        SHORT(TimeUnit.MINUTES.toMillis(1)),
        /**
         * Long time to live (long period -> 1 day).
         */
        LONG(TimeUnit.DAYS.toMillis(1));

        private final long time;

        TimeToLive(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }
    }
}
