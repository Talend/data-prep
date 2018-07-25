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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.cache.CacheJanitor;
import org.talend.dataprep.maintenance.MaintenanceTaskProcess;
import org.talend.dataprep.maintenance.executor.MaintenanceTask;

import java.util.function.Supplier;

import static org.talend.dataprep.maintenance.executor.Schedule.NIGHT;

@MaintenanceTask(NIGHT)
public class ScheduledCacheJanitor implements MaintenanceTaskProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledCacheJanitor.class);

    @Autowired
    private CacheJanitor janitor;

    @Override
    public void executeTask() {
        LOGGER.debug("Janitor process started @ {}.", System.currentTimeMillis());
        janitor.janitor();
        LOGGER.debug("Janitor process ended @ {}.", System.currentTimeMillis());
    }

    @Override
    public Supplier<Boolean> conditionTask() {
        return () -> true;
    }

}
