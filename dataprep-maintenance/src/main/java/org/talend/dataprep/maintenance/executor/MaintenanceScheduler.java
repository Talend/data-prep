package org.talend.dataprep.maintenance.executor;

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
import java.util.stream.Stream;

import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.REPEAT;

@Component
public class MaintenanceScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceScheduler.class);

    private static final ScheduleFrequency DEFAULT_SCHEDULE_FREQUENCY = REPEAT;

    private static final String EVERY_DAY_CRON_EXPRESSION = "0 0 3 * * *";

    @Autowired
    private List<MaintenanceTaskProcess> maintenanceTasks;

    @Autowired
    private ForAll forAll;

    @Autowired
    private Security security;

    private Map<String, Long> runningTask = new HashMap<>();

    @PostConstruct
    public void launchOnceTask() {
        runMaintenanceTask(ScheduleFrequency.ONCE);
    }

    @Scheduled(cron = EVERY_DAY_CRON_EXPRESSION)
    public void launchNightlyTask() {
        runMaintenanceTask(ScheduleFrequency.NIGHT);
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 60 * 60 * 1000) // Every hour
    public void launchRepeatlyTask() {
        runMaintenanceTask(ScheduleFrequency.REPEAT);
    }

    private void runMaintenanceTask(ScheduleFrequency frequency) {
        LOGGER.info("Starting scheduled task with frequency {}", frequency);
        forAll.execute(() -> true, () -> {

            String tenantId = security.getTenantId();
            // filter maintenance task marked as with this frequency and run it
            getTaskForFrequency(frequency)
                    .forEach(
                            task -> {
                                String taskKey = task.getClass() + "_" + tenantId;
                                if (!isAlreadyRunning(taskKey)) {
                                    executeTask(tenantId, task, taskKey);
                                } else {
                                    LOGGER.warn("Scheduled task {} for tenant {} is already running", task.getClass(), tenantId);
                                }
                            }
                    );
        });
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
        return runningTask.containsKey(taskKey);
    }

    private Stream<MaintenanceTaskProcess> getTaskForFrequency(ScheduleFrequency frequency) {
        return maintenanceTasks
                .stream()
                .filter(task -> getFrequency(task) == frequency);
    }

    private ScheduleFrequency getFrequency(MaintenanceTaskProcess task) {
        final MaintenanceTask annotation = AnnotationUtils.findAnnotation(task.getClass(), MaintenanceTask.class);
        final ScheduleFrequency value;
        if (annotation == null) {
            LOGGER.warn("Maintenance task '{}' has no schedule indication, default to {}", task.getClass(),
                    DEFAULT_SCHEDULE_FREQUENCY);
            value = DEFAULT_SCHEDULE_FREQUENCY;
        } else {
            value = annotation.value();
        }
        return value;
    }


}
