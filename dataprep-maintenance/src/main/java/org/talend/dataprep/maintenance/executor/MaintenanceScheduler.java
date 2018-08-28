package org.talend.dataprep.maintenance.executor;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.NIGHT;
import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.ONCE;
import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.REPEAT;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.security.Security;
import org.talend.tenancy.ForAll;

@Component
public class MaintenanceScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceScheduler.class);

    @Autowired
    private List<MaintenanceTaskProcess> maintenanceTasks;

    @Autowired
    private ForAll forAll;

    @Autowired
    private Security security;

    @Autowired
    private TaskExecutor taskExecutor;

    private Map<String, Long> runningTask = new ConcurrentHashMap<>();

    @PostConstruct
    public void launchOnceTask() {
        taskExecutor.execute(() -> runMaintenanceTask(ONCE));
    }

    @Scheduled(cron = "${maintenance.scheduled.cron}")
    public void launchNightlyTask() {
        runMaintenanceTask(NIGHT);
    }

    @Scheduled(fixedDelayString = "${maintenance.scheduled.fixed-delay}",
            initialDelayString = "${maintenance.scheduled.initial-delay}")
    public void launchRepeatlyTask() {
        runMaintenanceTask(REPEAT);
    }

    private void runMaintenanceTask(ScheduleFrequency frequency) {
        forAll.execute(() -> true, () -> {
            final String tenantId = security.getTenantId();
            LOGGER.info("Starting scheduled task with frequency {} for tenant {}", frequency, tenantId);
            maintenanceTasks
                    .stream()
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
            LOGGER.info("Scheduled task with frequency {} for tenant {} is finished", frequency, tenantId);
        });
    }

    private void executeTask(String tenantId, MaintenanceTaskProcess task, String taskKey) {
        Long startedTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Scheduled task {} process for tenant {} started @ {}.", task.getClass(), tenantId,
                    startedTime);
            runningTask.put(taskKey, startedTime);
            task.execute();
            LOGGER.debug("Scheduled task {} process for tenant {} ended @ {}.", task.getClass(), tenantId,
                    System.currentTimeMillis());
        } finally {
            runningTask.remove(taskKey);
        }
    }

    protected boolean isAlreadyRunning(String taskKey) {
        return isNotEmpty(taskKey) && runningTask.containsKey(taskKey);
    }

}
