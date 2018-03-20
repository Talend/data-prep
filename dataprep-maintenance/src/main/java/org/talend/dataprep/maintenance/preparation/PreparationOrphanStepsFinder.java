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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.PreparationUtils;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.store.PreparationRepository;

/**
 * Preparation based implementation of the OrphanStepsFinder.
 */
@Component
public class PreparationOrphanStepsFinder implements OrphanStepsFinder {

    private final PreparationUtils preparationUtils = new PreparationUtils();

    @Autowired
    private PreparationRepository repository;

    @Override
    public Set<Step> getOrphanSteps() {
        final Collection<Step> steps = repository.list(Step.class).collect(toList());
        final Set<String> preparationStepIds = getUsedSteps();

        final Predicate<Step> isNotRootStep = step -> !Step.ROOT_STEP.getId().equals(step.getId());
        final Predicate<Step> isOrphan = step -> !preparationStepIds.contains(step.getId());

        return steps.stream().filter(isNotRootStep).filter(isOrphan).collect(toSet());
    }

    private Set<String> getUsedSteps() {
        return repository.list(Preparation.class) //
                .flatMap(prep -> preparationUtils.listStepsIds(prep.getHeadId(), repository).stream()) //
                .collect(toSet());
    }

}
