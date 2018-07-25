package org.talend.dataprep.maintenance.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.maintenance.MaintenanceTaskProcess;
import org.talend.tenancy.ForAll;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.talend.dataprep.maintenance.executor.ScheduleFrequency.REPEAT;

@Component
public class TaskScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskScheduler.class);

    private static final ScheduleFrequency DEFAULT_SCHEDULE_FREQUENCY = REPEAT;

    private static final String EVERY_DAY_CRON_EXPRESSION = "0 0 3 * * *";


    @Autowired
    private List<MaintenanceTaskProcess> maintenanceTasks;

    @Autowired
    private ForAll forAll;

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
            // filter maintenance task marked as with this frequency and run it
            getTaskForFrequency(frequency)
                    .forEach(
                            task -> {
                                LOGGER.debug("Scheduler task {} process started @ {}.", task.getClass(), System.currentTimeMillis());
                                task.execute();
                                LOGGER.debug("Scheduler task {} process ended @ {}.", task.getClass(), System.currentTimeMillis());
                            }
                    );
        });
    }

    private Stream<MaintenanceTaskProcess> getTaskForFrequency(ScheduleFrequency frequency) {
        System.out.println("maintenanceTasks = " + maintenanceTasks.size());
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
