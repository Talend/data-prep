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

package org.talend.dataprep.upgrade.common;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.cache.ContentCache;

/**
 * Clean cache.
 */
@Component
public class CacheCleaner {

    /** This class' logger. */
    private static final Logger LOG = getLogger(CacheCleaner.class);

    @Autowired
    private ContentCache contentCache;

    /**
     * Clean the cache.
     */
    public void cleanCache() {
        contentCache.clear();
        LOG.debug("content cache has been cleared");
    }

}
