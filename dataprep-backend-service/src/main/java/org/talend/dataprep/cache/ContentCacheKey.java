// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.util.function.Predicate;

/**
 * Content cache key used to group all information needed by the cache.
 */
@FunctionalInterface
public interface ContentCacheKey {

    /**
     * The key must be unique per content !
     *
     * @return the key for this cache content as a string.
     */
    String getKey();

    /**
     * <p>
     * Create a predicate that allows to compare another content cache key (as returned by {@link #getKey()}) with this
     * current cache key.
     * </p>
     * <p>
     * Each implementation may decide to match on partial key or on full key.
     * </p>
     *
     * @return A predicate that match another content cache key
     * @see #getKey()
     * @see ContentCache#evictMatch(ContentCacheKey)
     */
    default Predicate<String> getMatcher() {
        throw new UnsupportedOperationException("Matcher is not implemented");
    }

}
