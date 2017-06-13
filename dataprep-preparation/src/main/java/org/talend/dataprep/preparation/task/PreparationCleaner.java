// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.preparation.task;

import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.SecurityProxy;

/**
 * Scheduler that clean the repository. It removes all the steps that do NOT belong to any preparation
 */
@Component
@EnableScheduling
public class PreparationCleaner {

    @Autowired
    private PreparationRepository repository;

    @Value("${preparation.store.remove.hours:24}")
    private int orphanTime;

    /** The root step. */
    @Resource(name = "rootStep")
    private Step rootStep;

    @Autowired
    private SecurityProxy securityProxy;

    @Autowired
    private PreparationUtils preparationUtils;

    @Autowired
    private List<OrphanStepsFinder> orphanStepsFinders;

    /**
     * Get all the step ids that belong to a preparation
     *
     * @return The step ids
     */
    private Set<String> getPreparationStepIds() {
        return repository.list(Preparation.class) //
                .flatMap(p -> p.getSteps().stream().map(Step::getId)) //
                .collect(toSet());
    }

    /**
     * Get current steps that has no preparation
     *
     * @return The orphan steps
     */
    private Stream<PersistentStep> getCurrentOrphanSteps() {
        final Set<String> preparationStepIds = getPreparationStepIds();
        final Predicate<PersistentStep> isNotRootStep = step -> !rootStep.getId().equals(step.getId());
        final Predicate<PersistentStep> isOrphan = step -> !preparationStepIds.contains(step.getId());
        return repository.list(PersistentStep.class).filter(isNotRootStep).filter(isOrphan);
    }

    /**
     * Remove the orphan steps (that do NOT belong to any preparation).
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 60 * 60 * 1000) // Every hour
    public void removeOrphanSteps() {

        securityProxy.asTechnicalUser();
        try {
            getCurrentOrphanSteps().forEach(step -> {
                // Remove step
                final Step stepToRemove = new Step();
                stepToRemove.setId(step.getId());
                repository.remove(stepToRemove);

                // Remove actions linked to step
                final PreparationActions preparationActionsToRemove = new PreparationActions();
                preparationActionsToRemove.setId(step.getContent());
                repository.remove(preparationActionsToRemove);

                // Remove metadata linked to step
                final StepRowMetadata stepRowMetadataToRemove = new StepRowMetadata();
                stepRowMetadataToRemove.setId(stepToRemove.getRowMetadata());
                repository.remove(stepRowMetadataToRemove);
            });
        } finally {
            securityProxy.releaseIdentity();
        }

    }
}
