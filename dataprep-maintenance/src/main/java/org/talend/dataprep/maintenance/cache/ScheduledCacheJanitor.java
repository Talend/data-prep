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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.cache.CacheJanitor;
import org.talend.dataprep.maintenance.executor.MaintenanceTaskProcess;
import org.talend.dataprep.maintenance.executor.MaintenanceTask;
import org.talend.dataprep.maintenance.executor.ScheduleFrequency;

import java.util.function.Supplier;

import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.NIGHT;

@Component
public class ScheduledCacheJanitor implements MaintenanceTaskProcess {

    @Autowired
    private CacheJanitor janitor;

    public void performTask() {
        janitor.janitor();
    }

    public Supplier<Boolean> condition() {
        return () -> true;
    }

    @Override
    public ScheduleFrequency getFrequency() {
        return ScheduleFrequency.NIGHT;
    }
}
