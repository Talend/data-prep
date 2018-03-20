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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.maintenance.BaseMaintenanceTest;
import org.talend.dataprep.preparation.store.PreparationRepository;

/**
 * Unit tests for the PreparationOrphanStepFinder.
 *
 * @see PreparationOrphanStepsFinder
 */
public class PreparationOrphanStepsFinderTest extends BaseMaintenanceTest {

    @InjectMocks
    private PreparationOrphanStepsFinder finder;

    @Mock
    private PreparationRepository repository;

    @Test
    public void shouldListOrphanSteps() {
        // given
        final Step firstStep = new Step(Step.ROOT_STEP.id(), "first", "2.1");
        final Step secondStep = new Step(firstStep.id(), "second", "2.1");
        final Step orphanStep = new Step(Step.ROOT_STEP.id(), "orphan", "2.1");
        when(repository.list(eq(Step.class))).thenReturn(Stream.of(firstStep, secondStep, orphanStep));
        when(repository.get(eq(firstStep.id()), eq(Step.class))).thenReturn(firstStep);
        when(repository.get(eq(secondStep.id()), eq(Step.class))).thenReturn(secondStep);
        when(repository.get(eq(orphanStep.id()), eq(Step.class))).thenReturn(orphanStep);

        final Preparation preparation = new Preparation("#123", "1", secondStep.id(), "2.1");
        when(repository.list(eq(Preparation.class))).thenReturn(Stream.of(preparation));

        // when
        final Set<Step> orphanSteps = finder.getOrphanSteps();

        // then
        assertNotNull(orphanSteps);
        assertEquals(1, orphanSteps.size());
        assertEquals(orphanStep, orphanSteps.iterator().next());
    }
}
