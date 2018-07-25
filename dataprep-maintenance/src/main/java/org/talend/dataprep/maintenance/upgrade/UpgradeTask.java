package org.talend.dataprep.maintenance.upgrade;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.maintenance.MaintenanceTaskProcess;
import org.talend.dataprep.maintenance.executor.MaintenanceTask;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.upgrade.UpgradeService;
import org.talend.dataprep.upgrade.repository.UpgradeTaskRepository;

import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.ONCE;

/**
 *
 */
@MaintenanceTask(ONCE)
@Component
public class UpgradeTask extends MaintenanceTaskProcess {

    /**
     * This class' logger.
     */
    private static final Logger LOG = getLogger(UpgradeTask.class);

    /**
     * Service in charge of upgrading data from older versions.
     */
    @Autowired
    private UpgradeService upgradeService;

    @Autowired
    private UpgradeTaskRepository repository;

    @Autowired
    private Security security;

    protected void performTask() {
        LOG.info("Performing upgrade for '{}'...", security.getTenantId());
        upgradeService.upgradeVersion();
        LOG.info("Performing upgrade done for '{}'.", security.getTenantId());

    }

    protected Supplier<Boolean> condition() {
        return () -> upgradeService.needUpgrade();
    }

}
