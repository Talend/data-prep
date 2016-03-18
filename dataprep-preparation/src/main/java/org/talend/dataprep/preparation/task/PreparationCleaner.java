//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.preparation.task;

import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PreparationRepository;

/**
 * Scheduler that clean the repository.
 * It removes all the steps that do NOT belong to any preparation
 */
@Component
@EnableScheduling
public class PreparationCleaner {

    @Autowired
    private PreparationRepository repository;

    private Map<Step, Integer> orphansStepsTags = new HashMap<>();

    @Value("${preparation.store.remove.hours}")
    private int orphanTime;

    /** The root step. */
    @Resource(name = "rootStep")
    private Step rootStep;

    @Autowired
    private PreparationUtils preparationUtils;

    /**
     * Get all the step ids that belong to a preparation
     * @return The step ids
     */
    private Set<String> getPreparationStepIds() {
        return repository.listAll(Preparation.class)
                .stream()
                .flatMap(prep -> preparationUtils.listStepsIds(prep.getHeadId(), repository).stream())
                .collect(toSet());
    }

    /**
     * Get current steps that has no preparation
     * @return The orphan steps
     */
    private List<Step> getCurrentOrphanSteps() {
        final Collection<Step> steps = repository.listAll(Step.class);
        final Set<String> preparationStepIds = getPreparationStepIds();

        final Predicate<Step> isNotRootStep = step -> !rootStep.getId().equals(step.getId());
        final Predicate<Step> isOrphan = step -> !preparationStepIds.contains(step.getId());

        return steps.stream()
                .filter(isNotRootStep)
                .filter(isOrphan)
                .collect(toList());
    }

    /**
     * Tag the orphans steps.
     * @param currentOrphans The current orphans
     */
    private void updateOrphanTags(final List<Step> currentOrphans) {
        orphansStepsTags = currentOrphans.stream()
                .collect(toMap(Function.identity(), step -> {
                    final Integer tag = orphansStepsTags.get(step);
                    return tag == null ? 0 : tag + 1;
                }));
    }

    /**
     * Remove all the orphan steps that is orphans since {preparation.store.remove.hours} hours
     */
    private void cleanSteps() {
        orphansStepsTags.entrySet().stream()
                .filter(entry -> entry.getValue() >= orphanTime)
                .forEach(entry -> repository.remove(entry.getKey()));
    }

    /**
     * Remove the orphan steps (that do NOT belong to any preparation).
     */
    @Scheduled(fixedDelay = 60000)
    public void removeOrphanSteps() {
        final List<Step> currentOrphans = getCurrentOrphanSteps();
        updateOrphanTags(currentOrphans);
        cleanSteps();
    }
}
