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

import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationActions;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.SecurityProxy;

/**
 * Scheduler that clean the repository.
 * It removes all the steps that do NOT belong to any preparation
 */
@Component
@EnableScheduling
public class PreparationCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationCleaner.class);

    @Autowired
    private PreparationRepository repository;

    private Map<Step, Integer> orphansStepsTags = new HashMap<>();

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
     * @return all orphanSteps.
     */
    private Set<Step> getOrphanSteps() {
        Set<Step> orphanSteps = new HashSet<>();
        for (OrphanStepsFinder finder : orphanStepsFinders) {
            orphanSteps.addAll(finder.getOrphanSteps());
        }
        return orphanSteps;
    }

    /**
     * Tag the orphans steps.
     *
     * @param currentOrphans The current orphans
     */
    private void updateOrphanTags(final Set<Step> currentOrphans) {
        orphansStepsTags = currentOrphans.stream().collect(toMap(Function.identity(), step -> {
            final Integer tag = orphansStepsTags.get(step);
            return tag == null ? 0 : tag + 1;
        }));
    }

    /**
     * Remove all the orphan steps that is orphans since {preparation.store.remove.hours} hours
     */
    private void cleanSteps() {
        orphansStepsTags.entrySet().stream().filter(entry -> entry.getValue() >= orphanTime).forEach(entry -> {
            final Step step = entry.getKey();
            final PreparationActions content = repository.get(step.getContent().id(), PreparationActions.class);
            repository.remove(content);
            repository.remove(step);
        });
    }

    /**
     * Removes all steps & content in the preparation <code>preparationId</code>.
     *
     * @param preparationId A preparation id, if <code>null</code> this a no-op. Preparation must still exist for proper
     * clean up.
     */
    public void removePreparationOrphanSteps(String preparationId) {
        if (preparationId == null) {
            return;
        }
        // Compute usage information (to prevent shared step deletion).
        final Map<Step, Integer> stepUsageCount = new HashMap<>();
        repository.list(Preparation.class) //
                .flatMap(prep -> preparationUtils.listSteps(prep.getHeadId(), repository).stream()).forEach(s -> {
                    if (stepUsageCount.containsKey(s)) {
                        stepUsageCount.put(s, stepUsageCount.get(s) + 1);
                    } else {
                        stepUsageCount.put(s, 1);
                    }
                });
        // Clean up steps only used in deleted preparation
        Stream.of(repository.get(preparationId, Preparation.class)) //
                .filter(p -> { // Exit if preparation no longer exist.
                    if (p != null) {
                        return true;
                    } else {
                        LOGGER.error("Preparation {} is already removed, unable to clean up steps associated with preparation.",
                                preparationId);
                        return false;
                    }
                }) //
                .flatMap(p -> preparationUtils.listSteps(p.getHeadId(), repository).stream()) //
                .filter(s -> !rootStep.getId().equals(s.getId())) // Don't delete root step
                .forEach(s -> stepUsageCount.computeIfPresent(s, (step, usage) -> {
                    if (usage == 1) { // Step only used in to-be-deleted preparation,
                        final PreparationActions content = repository.get(step.getContent().id(), PreparationActions.class);
                        LOGGER.info("Removing step content {}.", content.getId());
                        repository.remove(content);
                        LOGGER.info("Removing step {}.", step.getId());
                        repository.remove(step);
                    } else {
                        LOGGER.info("Not removing step content {} (usage count: {}).", step.getId(), usage);
                    }
                    return usage - 1;
                }));
    }

    /**
     * Remove the orphan steps (that do NOT belong to any preparation).
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000) // Every hour
    public void removeOrphanSteps() {
        securityProxy.asTechnicalUser();
        try {
            final Set<Step> currentOrphans = getOrphanSteps();
            updateOrphanTags(currentOrphans);
            cleanSteps();
        } finally {
            securityProxy.releaseIdentity();
        }
    }
}
