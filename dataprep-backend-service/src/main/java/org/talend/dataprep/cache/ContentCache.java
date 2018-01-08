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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;

/**
 * A component to hold versions of a preparation at different steps. Each implementation may implement different
 * eviction strategies that do not need to surface here.
 */
public interface ContentCache {

    /**
     * A constant to prevent cache hits and janitor to collide (cache won't serve entries that are going to be cleared
     * in <= EVICTION_PERIOD milliseconds). This constant then assumes it takes less than EVICTION_PERIOD to serve
     * cached content to client.
     */
    int EVICTION_PERIOD = 2000;

    /**
     * Check whether a cached content exists for given <code>preparationId</code> at step <code>stepId</code> for a
     * specific sample size.
     *
     * @param key content cache key.
     * @return <code>true</code> if cache holds content for given parameters, <code>false</code> otherwise.
     */
    @Timed
    boolean has(ContentCacheKey key);

    /**
     * Returns the cached content for given <code>preparationId</code> at step <code>stepId</code>
     *
     * @param key content cache key.
     * @return The cached content for given parameters, or <code>null</code> if not in the cache.
     * @throws IllegalArgumentException If no cache can be found for given parameters.
     * @see #has(ContentCacheKey)
     */
    @VolumeMetered
    InputStream get(ContentCacheKey key);

    /**
     * Allow callers to create an entry in cache for given <code>preparationId</code> at step <code>stepId</code>.
     * Please note content is not passed in parameters but return of this method also callers to write in entry.
     *
     * @param key content cache key.
     * @param timeToLive The {@link TimeToLive TTL} for the new cache entry.
     * @return A {@link OutputStream output stream} to be used to write content in cache entry
     */
    @VolumeMetered
    OutputStream put(ContentCacheKey key, TimeToLive timeToLive);

    /**
     * Mark cache entry as invalid for given key. After this method
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
    @Timed
    void evict(ContentCacheKey key);

    /**
     * Mark cache entry as invalid for the provided partial key. After this method
     * completes, {@link #has(ContentCacheKey)} must immediately return <code>false</code>.
     *
     * The eviction is performed when the entry's key match the partial key.
     *
     * @param key partial content cache key.
     * @see ContentCacheKey#getMatcher()
     */
    @Timed
    void evictMatch(ContentCacheKey key);

    /**
     * <p>
     * Moves a cache entry from the <code>from</code> key to a <code>to</code> key. After method completes,
     * {@link #has(ContentCacheKey)} returns <code>false</code> for <code>from</code> key and <code>true</code> for
     * <code>to</code> key.
     * </p>
     * <p>
     * If <code>to</code> is a cache key with content, it will be overridden with new content.
     * </p>
     *
     * @param from A source content cache key.
     * @param to A destination content cache key.
     * @param toTimeToLive The {@link TimeToLive TTL} for the destination cache key.
     */
    @Timed
    void move(ContentCacheKey from, ContentCacheKey to, TimeToLive toTimeToLive);

    /**
     * Removes all content cached by this {@link ContentCache cache}.
     */
    @Timed
    void clear();

    /**
     * Configure how long a cache entry may exist in cache.
     */
    enum TimeToLive {
        /**
         * A very short expiration time (using ContentCache.EVICTION_PERIOD). Useful for tests on eviction.
         */
        IMMEDIATE(ContentCache.EVICTION_PERIOD + 500L),
        /**
         * A very short expiration time (5 seconds).
         */
        VERY_SHORT(TimeUnit.SECONDS.toMillis(5)),
        /**
         * Short time to live (short period -> 1 minute).
         */
        SHORT(TimeUnit.MINUTES.toMillis(1)),
        /**
         * Normal time to live (1 hour).
         */
        NORMAL(TimeUnit.HOURS.toMillis(1)),
        /**
         * Long time to live (long period -> 1 day).
         */
        LONG(TimeUnit.DAYS.toMillis(1)),
        /**
         * A infinite expiration time (it is up to caller to evict the cache entry).
         */
        PERMANENT(-1),
        /**
         * Default time to live for a content in cache (1 hour).
         */
        DEFAULT(NORMAL.time);

        private final long time;

        TimeToLive(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }
    }
}
