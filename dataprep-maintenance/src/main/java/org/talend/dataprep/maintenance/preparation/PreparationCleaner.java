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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.tenancy.ForAll;
import org.talend.tql.parser.Tql;

/**
 * Cleans the preparation repository. It removes all the steps that do NOT belong to a preparation any more.
 */
@Component
@ConditionalOnProperty(value = "preparation.store.orphan.cleanup", havingValue = "true", matchIfMissing = true)
public class PreparationCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationCleaner.class);

    @Autowired
    private PreparationRepository repository;

    @Autowired
    private SecurityProxy securityProxy;

    @Autowired
    private List<StepMarker> markers = new ArrayList<>();

    @Autowired
    private ForAll forAll;

    void setMarkers(List<StepMarker> markers) {
        this.markers = markers;
    }

    /**
     * Remove all orphan steps in preparation repository.
     */
    private void removeCurrentOrphanSteps() {
        securityProxy.asTechnicalUser();
        final String currentCleanerRun = UUID.randomUUID().toString();
        try {
            LOGGER.info("Starting clean run '{}'", currentCleanerRun);
            StepMarker.Result allMarkersResult = StepMarker.Result.COMPLETED;
            for (StepMarker marker : markers) {
                final StepMarker.Result result = marker.mark(repository, currentCleanerRun);
                if (result == StepMarker.Result.INTERRUPTED) {
                    allMarkersResult = StepMarker.Result.INTERRUPTED;
                }
            }

            if (allMarkersResult == StepMarker.Result.COMPLETED) {
                LOGGER.info("Removing unused steps ({} scheduled for deletion)",
                        repository.count(Step.class, Tql.parse("marker = '" + currentCleanerRun + "'")));
                repository.remove(Step.class, Tql.parse("marker != '" + currentCleanerRun + "'"));
            } else {
                LOGGER.info("Discarding {} pending step deletes",
                        repository.count(Step.class, Tql.parse("marker = '" + currentCleanerRun + "'")));
            }
        } finally {
            securityProxy.releaseIdentity();
            LOGGER.info("Done clean run '{}'", currentCleanerRun);
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
