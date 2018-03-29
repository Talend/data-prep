package org.talend.dataprep.maintenance.preparation;

import static java.time.Instant.now;
import static org.talend.tql.api.TqlBuilder.gt;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Identifiable;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.tql.model.Expression;

/**
 * A {@link StepMarker} implementation that marks all {@link Step steps} in the current state of a {@link Preparation}.
 * If a recently modified preparation is detected (a preparation has been modified within last hour), the marker process
 * is interrupted.
 */
@Component
public class PreparationStepMarker implements StepMarker {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationStepMarker.class);

    @Value("${cleaner.recently.modified:3600}")
    private int recentlyModified;

    // Utility method to log recently modified preparations (log in DEBUG level).
    private void logRecentlyModified(PreparationRepository repository) {
        if (LOGGER.isDebugEnabled()) {
            repository.list(Preparation.class, recentlyModified()).forEach(
                    rm -> LOGGER.debug("Preparation '{}' was recently modified.", rm));
        }
    }

    // Utility method to get TQL expression for recently modified preparations.
    private Expression recentlyModified() {
        return gt("lastModificationDate", now().minus(recentlyModified, ChronoUnit.SECONDS).toEpochMilli());
    }

    @Override
    public Result mark(PreparationRepository repository, UUID marker) {
        if (repository.exist(Preparation.class, recentlyModified())) {
            LOGGER.info("Not running clean up (at least a preparation modified within last hour).");
            logRecentlyModified(repository);
            return Result.INTERRUPTED;
        }

        final AtomicBoolean interrupted = new AtomicBoolean(false);
        repository
                .list(Preparation.class) //
                .filter(p -> !interrupted.get()) //
                .forEach(p -> {
                    if (repository.exist(Preparation.class, recentlyModified())) {
                        LOGGER.info("Interrupting clean up (preparation modified within last hour).");
                        logRecentlyModified(repository);
                        interrupted.set(true);
                        return;
                    }
                    final Collection<Identifiable> markedSteps = p
                            .getSteps() //
                            .stream() //
                            .filter(s -> !Objects.equals(s, Step.ROOT_STEP))
                            .peek(s -> s.setMarker(marker)) //
                            .collect(Collectors.toList());
                    repository.add(markedSteps);
                });
        return interrupted.get() ? Result.INTERRUPTED : Result.COMPLETED;
    }
}
