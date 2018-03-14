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

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Spring listener on cache event
 */
@Component
@ConditionalOnProperty(name = "dateprep.event.listener", havingValue = "spring")
public class CleanCacheListener {

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = getLogger(CleanCacheListener.class);

    /**
     * Utility to process event
     */
    @Autowired
    private CacheEventProcessingUtil eventProcessingUtil;

    public CleanCacheListener() {
        super();
    }

    /**
     * Clean the whole dataset cache.
     *
     * @param event the event to respond to.
     */

    @EventListener
    public void onEvent(CleanCacheEvent event) {
        LOGGER.debug("Processing spring clean cache event: {}", event);

        // We delete content cache key
        eventProcessingUtil.processCleanCacheEvent(event.getSource(), event.isPartialKey());
    }
}
