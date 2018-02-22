// ============================================================================
//
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

package org.talend.dataprep.async.progress;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * First In First Out map used to limit the map size.
 */
public class FifoMap<K, V> extends LinkedHashMap<K, V> {

    /** For the Serializable interface. */
    private static final long serialVersionUID = 1L;

    /** Maximum number of entries. */
    private int maxEntries;

    /**
     * Default constructor.
     * 
     * @param maxEntries maximum number of entries.
     */
    public FifoMap(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * @see LinkedHashMap#removeEldestEntry(Map.Entry)
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
        return size() > maxEntries;
    }
}
