// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.maintenance.preparation;

import static java.util.stream.Collectors.toSet;
import static org.talend.tql.api.TqlBuilder.eq;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.preparation.StepRowMetadata;
import org.talend.dataprep.preparation.store.PersistentStep;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.tenancy.ForAll;

/**
 * Cleans the preparation repository. It removes all the steps that do NOT belong to a preparation any more.
 */
@Component
@ConditionalOnProperty(value = "preparation.store.orphan.cleanup", havingValue = "true", matchIfMissing = true)
public class PreparationCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationCleaner.class);

    @Autowired
    private PreparationRepository repository;

    @Value("${preparation.store.remove.hours:24}")
    private int orphanTime;

    @Autowired
    private SecurityProxy securityProxy;

    @Autowired
    private PreparationUtils preparationUtils;

    @Autowired(required = false)
    private List<OrphanStepsFinder> orphanStepsFinders;

    @Autowired
    private ForAll forAll;

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
        final Predicate<PersistentStep> isNotRootStep = step -> !Step.ROOT_STEP.getId().equals(step.getId());
        final Predicate<PersistentStep> isOrphan = step -> !preparationStepIds.contains(step.getId());
        return repository.list(PersistentStep.class).filter(isNotRootStep).filter(isOrphan);
    }

    /**
     * Remove all orphan steps in preparation repository.
     */
    public void removeCurrentOrphanSteps() {
        securityProxy.asTechnicalUser();
        try {
            getCurrentOrphanSteps().forEach(step -> {
                // Remove step
                final Step stepToRemove = new Step();
                stepToRemove.setId(step.getId());
                repository.remove(stepToRemove);

                // Remove actions linked to step
                // if this step re-use an existing actions we don't delete the actions
                boolean criterion = repository.exist(PersistentStep.class, eq("contentId", step.getContent()));
                if (criterion) {
                    LOGGER.debug("Don't removing step content {} it still used by another step.", step.getContent());
                } else {
                    LOGGER.debug("Removing step content {}.", step.getContent());
                    final PreparationActions preparationActionsToRemove = new PreparationActions();
                    preparationActionsToRemove.setId(step.getContent());
                    repository.remove(preparationActionsToRemove);
                }

                // Remove metadata linked to step
                final StepRowMetadata stepRowMetadataToRemove = new StepRowMetadata();
                stepRowMetadataToRemove.setId(stepToRemove.getRowMetadata());
                repository.remove(stepRowMetadataToRemove);
            });
        } finally {
            securityProxy.releaseIdentity();
        }
    }

    /**
     * Remove the orphan steps (that do NOT belong to any preparation) for all available tenants.
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 60 * 60 * 1000) // Every hour
    public void removeOrphanSteps() {
        forAll.execute(forAll.condition().operational(repository), this::removeCurrentOrphanSteps);
    }
}
