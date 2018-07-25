package org.talend.dataprep.maintenance.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.talend.tenancy.ForAll;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static java.time.Instant.now;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;
import static org.talend.dataprep.maintenance.executor.Schedule.REPEAT;

/**
 * <p>
 * A wrapper for {@link ForAll} that allow to sequentially execute all {@link Runnable runnables} in the same tenancy
 * context.
 * </p>
 * <p>
 * When multiple {@link ForAll} are being used in code, this implementation put less pressure on configuration service.
 * </p>
 */
public class GroupedForAll implements ForAll {

    private static final Schedule DEFAULT_SCHEDULE = REPEAT;

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupedForAll.class);

    private final ForAll delegate;

    private GroupedExecutor groupedExecutor;

    private Set<Class> knownRunnables = new HashSet<>();

    private Set<Class> executedTasks = new HashSet<>();

    /**
     * Construct a {@link ForAll} implementation that calls {@link GroupedExecutor#register(Supplier, Runnable)} for
     * each {@link #execute(Supplier, Runnable)} called on this implementation.
     * 
     * @param delegate The {@link ForAll} implementation to wrap. Essentially for {@link #condition()} implementation.
     * @param groupedExecutor The executor that allows grouped {@link Runnable} executions.
     */
    public GroupedForAll(ForAll delegate, GroupedExecutor groupedExecutor) {
        this.delegate = delegate;
        this.groupedExecutor = groupedExecutor;
    }

    /**
     * Get the class name out of the runnable. Returns the enclosing class name in case a lambda is used.
     *
     * @param clazz The class of the runnable
     * @return The class of the runnable or the enclosing class name in case supplied class is a lambda.
     */
    private static Class extractClass(Class clazz) {
        String canonical = clazz.getCanonicalName();
        int lambdaOffset = canonical.indexOf("$$Lambda$");
        if (lambdaOffset > 0) {
            final String className = canonical.substring(0, lambdaOffset);
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                LOGGER.debug("Unable to find class '{}'.", className, e);
            }
        }
        return clazz;
    }

    @Override
    public void execute(final Supplier<Boolean> supplier, Runnable runnable) {
        final Class maintenanceClass = extractClass(runnable.getClass());
        final Supplier<Boolean> executionControl = executionControl(supplier, maintenanceClass);
        if (knownRunnables.add(maintenanceClass)) {
            LOGGER.debug("Registered new maintenance task: '{}'.", maintenanceClass);
            groupedExecutor.register(executionControl, runnable);
        } else {
            LOGGER.debug("Skipping execute call to '{}' (already known).", runnable);
        }
    }

    private Supplier<Boolean> executionControl(Supplier<Boolean> supplier, Class maintenanceClass) {
        final MaintenanceTask annotation = AnnotationUtils.findAnnotation(maintenanceClass, MaintenanceTask.class);
        final Schedule value;
        if (annotation == null) {
            LOGGER.warn("Maintenance task '{}' has no schedule indication, default to {}", maintenanceClass.getName(),
                    DEFAULT_SCHEDULE);
            value = DEFAULT_SCHEDULE;
        } else {
            value = annotation.value();
        }
        final Supplier<Boolean> executionControl;
        switch (value) {
        case ONCE:
            LOGGER.debug("Task in '{}' should only be executed once.", maintenanceClass);
            executionControl = () -> executedTasks.add(maintenanceClass) && supplier.get();
            break;
        case NIGHT:
            executionControl = () -> {
                final LocalDateTime localDateTime = ofInstant(now(), systemDefault());
                final Instant lower = localDateTime.withHour(3).atZone(systemDefault()).toInstant();
                final Instant upper = localDateTime.withHour(6).atZone(systemDefault()).toInstant();
                return now().isAfter(lower) && now().isBefore(upper) && supplier.get();
            };
            break;
        case REPEAT:
        default:
            LOGGER.debug("Task in '{}' will be repeatedly executed.", maintenanceClass);
            executionControl = supplier;
            break;
        }
        return executionControl;
    }

    @Override
    public ForAllConditionBuilder condition() {
        return delegate.condition();
    }

    Set<Class> getKnownRunnables() {
        return knownRunnables;
    }
}
