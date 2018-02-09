package org.talend.dataprep.maintenance.upgrade;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.talend.dataprep.security.Security;
import org.talend.dataprep.upgrade.UpgradeService;
import org.talend.dataprep.upgrade.repository.UpgradeTaskRepository;
import org.talend.tenancy.ForAll;

/**
 *
 */
@Component
public class UpgradeTask {

    /** This class' logger. */
    private static final Logger LOG = getLogger(UpgradeTask.class);

    /** Service in charge of upgrading data from older versions. */
    @Autowired
    private UpgradeService upgradeService;

    @Autowired
    private UpgradeTaskRepository repository;

    @Autowired
    @Resource(name = "applicationEventMulticaster#executor")
    private TaskExecutor executor;

    @Autowired
    private ForAll forAll;

    @Autowired
    private Security security;

    @PostConstruct
    public void upgradeTask() {
        executor.execute(() -> {
            final Supplier<Boolean> needUpgradeCondition = () -> upgradeService.needUpgrade();
            final Supplier<Boolean> hasRepositoryConfiguration = forAll.condition().operational(repository);
            forAll.execute(() -> hasRepositoryConfiguration.get() && needUpgradeCondition.get(), () -> {
                LOG.info("Performing upgrade for '{}'...", security.getTenantId());
                upgradeService.upgradeVersion();
                LOG.info("Performing upgrade done for '{}'.", security.getTenantId());
            });
        });
    }
}
