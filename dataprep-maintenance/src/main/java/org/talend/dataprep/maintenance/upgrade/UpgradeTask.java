package org.talend.dataprep.maintenance.upgrade;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.maintenance.MaintenanceTaskProcess;
import org.talend.dataprep.maintenance.executor.MaintenanceTask;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.upgrade.UpgradeService;
import org.talend.dataprep.upgrade.repository.UpgradeTaskRepository;

import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.dataprep.maintenance.executor.Schedule.ONCE;

/**
 *
 */
@MaintenanceTask(ONCE)
public class UpgradeTask implements MaintenanceTaskProcess {

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

    @Override
    public void executeTask() {
        LOG.info("Performing upgrade for '{}'...", security.getTenantId());
        upgradeService.upgradeVersion();
        LOG.info("Performing upgrade done for '{}'.", security.getTenantId());

    }

    @Override
    public Supplier<Boolean> conditionTask() {
        return () -> upgradeService.needUpgrade();
    }

}
