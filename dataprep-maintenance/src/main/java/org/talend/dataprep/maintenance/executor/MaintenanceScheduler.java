package org.talend.dataprep.maintenance.executor;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.security.Security;
import org.talend.tenancy.ForAll;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.NIGHT;
import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.ONCE;
import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.REPEAT;

@Component
public class MaintenanceScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceScheduler.class);

    private static final String EVERY_DAY_CRON_EXPRESSION = "0 0 3 * * *";

    @Autowired
    private List<MaintenanceTaskProcess> maintenanceTasks;

    @Autowired
    private ForAll forAll;

    @Autowired
    private Security security;

    private Map<String, Long> runningTask = new ConcurrentHashMap<>();

    @PostConstruct
    public void launchOnceTask() {
        runMaintenanceTask(ONCE);
    }

    @Scheduled(cron = EVERY_DAY_CRON_EXPRESSION)
    public void launchNightlyTask() {
        runMaintenanceTask(NIGHT);
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 60 * 60 * 1000) // Every hour
    public void launchRepeatlyTask() {
        runMaintenanceTask(REPEAT);
    }

    private void runMaintenanceTask(ScheduleFrequency frequency) {
        LOGGER.info("Starting scheduled task with frequency {}", frequency);
        forAll.execute(() -> {
            final String tenantId = security.getTenantId();
            // filter maintenance task marked as with this frequency and run it
            maintenanceTasks.stream()
                    .filter(task -> task.getFrequency() == frequency) //
                    .forEach(task -> {
                        String taskKey = task.getClass() + "_" + tenantId;
                        if (isAlreadyRunning(taskKey)) {
                            LOGGER.warn("Scheduled task {} for tenant {} is already running", task.getClass(),
                                    tenantId);
                        } else {
                            executeTask(tenantId, task, taskKey);
                        }
                    });
        });
        LOGGER.info("Scheduled task with frequency {} is finished", frequency);
    }

    private void executeTask(String tenantId, MaintenanceTaskProcess task, String taskKey) {
        Long startedTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Scheduled task {} process for tenant {} started @ {}.", task.getClass(), tenantId, startedTime);
            runningTask.put(taskKey, startedTime);
            task.execute();
            LOGGER.debug("Scheduled task {} process for tenant {} ended @ {}.", task.getClass(), tenantId, System.currentTimeMillis());
        } finally {
            runningTask.remove(taskKey);
        }
    }

    protected boolean isAlreadyRunning(String taskKey) {
        return isNotEmpty(taskKey) && runningTask.containsKey(taskKey);
    }

}
