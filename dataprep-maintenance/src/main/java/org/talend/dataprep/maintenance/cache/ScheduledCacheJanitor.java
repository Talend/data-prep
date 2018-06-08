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

package org.talend.dataprep.maintenance.cache;

import static org.talend.dataprep.maintenance.executor.Schedule.NIGHT;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.cache.CacheJanitor;
import org.talend.dataprep.maintenance.executor.MaintenanceTask;
import org.talend.tenancy.ForAll;

@MaintenanceTask(NIGHT)
public class ScheduledCacheJanitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledCacheJanitor.class);

    @Autowired
    private CacheJanitor janitor;

    @Autowired
    private ForAll forAll;

    @PostConstruct
    public void init() {
        LOGGER.info("Starting scheduled cache clean up.");
    }

    /**
     * Cleans the cache every minute.
     */
    public void scheduledJanitor() {
        LOGGER.debug("Janitor process started @ {}.", System.currentTimeMillis());
        forAll.execute(() -> janitor.janitor());
        LOGGER.debug("Janitor process ended @ {}.", System.currentTimeMillis());
    }

}
