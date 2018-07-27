package org.talend.dataprep.maintenance.upgrade;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.maintenance.executor.MaintenanceTaskProcess;
import org.talend.dataprep.maintenance.executor.ScheduleFrequency;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.upgrade.UpgradeService;
import org.talend.dataprep.upgrade.repository.UpgradeTaskRepository;
import org.talend.tenancy.ForAll;

import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
@Component
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
    private ForAll forAll;

    @Autowired
    private Security security;

    public void performTask() {
        LOG.info("Performing upgrade for '{}'...", security.getTenantId());
        upgradeService.upgradeVersion();
        LOG.info("Performing upgrade done for '{}'.", security.getTenantId());
    }

    public Supplier<Boolean> condition() {
        final Supplier<Boolean> needUpgradeCondition = () -> upgradeService.needUpgrade();
        final Supplier<Boolean> hasRepositoryConfiguration = forAll.condition().operational(repository);
        return () -> needUpgradeCondition.get() && hasRepositoryConfiguration.get();
    }

    @Override
    public ScheduleFrequency getFrequency() {
        return ScheduleFrequency.ONCE;
    }
}
