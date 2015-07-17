package org.talend.dataprep.preparation.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationRepository;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

/**
 * Scheduler that clean the repository.
 * It removes all the steps that do NOT belong to any preparation
 */
@Component
@EnableScheduling
public class PreparationCleaner {

    @Autowired
    private PreparationRepository repository;

    /**
     * Get all the step ids that belong to a preparation
     *
     * @return The step ids
     */
    private Set<String> getPreparationStepIds() {
        return repository.listAll(Preparation.class)
                .stream()
                .flatMap(prep -> PreparationUtils.listSteps(prep.getStep(), repository).stream())
                .collect(toSet());
    }

    /**
     * Remove all the steps which id is not in the preparations step ids
     *
     * @param steps              The steps list to process
     * @param preparationStepIds The step ids that belongs to at least 1 preparation
     */
    private void cleanSteps(final Collection<Step> steps, final Set<String> preparationStepIds) {
        final Predicate<Step> isNotRootStep = step -> !Step.ROOT_STEP.getId().equals(step.getId());
        final Predicate<Step> isOrphan = step -> !preparationStepIds.contains(step.getId());

        steps.stream()
                .filter(isNotRootStep)
                .filter(isOrphan)
                .forEach(repository::remove);
    }

    /**
     * Remove the orphan steps (that do NOT belong to any preparation).
     */
    @Scheduled(fixedDelay = 60000)
    public void removeOrphanSteps() {
        final Collection<Step> steps = repository.listAll(Step.class);
        final Set<String> preparationStepIds = getPreparationStepIds();

        cleanSteps(steps, preparationStepIds);
    }
}
