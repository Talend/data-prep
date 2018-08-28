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

import static org.talend.tql.api.TqlBuilder.neq;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.maintenance.executor.MaintenanceTaskProcess;
import org.talend.dataprep.maintenance.executor.ScheduleFrequency;
import org.talend.dataprep.preparation.store.PreparationRepository;
import org.talend.dataprep.security.SecurityProxy;
import org.talend.tenancy.ForAll;

/**
 * Cleans the preparation repository. It removes all the steps that do NOT belong to a preparation any more.
 */
@ConditionalOnProperty(value = "preparation.store.orphan.cleanup", havingValue = "true", matchIfMissing = true)
@Component
public class PreparationCleaner implements MaintenanceTaskProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparationCleaner.class);

    private static final String MARKER = "marker";

    @Autowired
    private PreparationRepository repository;

    @Autowired
    private SecurityProxy securityProxy;

    @Autowired
    private List<StepMarker> markers = new ArrayList<>();

    @Autowired
    private ForAll forAll;

    @Override
    public void performTask() {
        this.removeCurrentOrphanSteps();
    }

    @Override
    public Supplier<Boolean> condition() {
        return forAll.condition().operational(repository);
    }

    @Override
    public ScheduleFrequency getFrequency() {
        return ScheduleFrequency.NIGHT;
    }

    /**
     * Remove all orphan steps in preparation repository.
     */
    private void removeCurrentOrphanSteps() {
        securityProxy.asTechnicalUser();
        final UUID currentCleanerRun = UUID.randomUUID();
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
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Removing unused steps ({} scheduled for deletion)",
                            repository.count(Step.class, neq(MARKER, currentCleanerRun.toString())));
                }
                repository.remove(Step.class, neq(MARKER, currentCleanerRun.toString()));
            } else {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Discarding {} pending step deletes",
                            repository.count(Step.class, neq(MARKER, currentCleanerRun.toString())));
                }
            }
        } finally {
            securityProxy.releaseIdentity();
            LOGGER.info("Done clean run '{}'", currentCleanerRun);
        }
    }

    public void setMarkers(List<StepMarker> markers) {
        this.markers = markers;
    }
}
