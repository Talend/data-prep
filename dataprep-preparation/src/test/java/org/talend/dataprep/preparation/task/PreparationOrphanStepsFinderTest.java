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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.preparation.BasePreparationTest;

/**
 * Unit tests for the PreparationOrphanStepFinder.
 *
 * @see PreparationOrphanStepsFinder
 */
public class PreparationOrphanStepsFinderTest extends BasePreparationTest {

    @Autowired
    private PreparationOrphanStepsFinder finder;

    @Before
    public void localSetup() throws Exception {
        repository.clear();
    }

    @Test
    public void shouldListOrphanSteps() {

        // given
        final Step firstStep = new Step(rootStep.id(), "first", "2.1");
        final Step secondStep = new Step(firstStep.id(), "second", "2.1");
        final Preparation preparation = new Preparation("#123", "1", secondStep.id(), "2.1");

        repository.add(firstStep);
        repository.add(secondStep);
        repository.add(preparation);

        final Step orphanStep = new Step(rootStep.id(), "orphan", "2.1");
        repository.add(orphanStep);

        // when
        final Set<Step> orphanSteps = finder.getOrphanSteps();

        // then
        assertNotNull(orphanSteps);
        assertEquals(1, orphanSteps.size());
        assertEquals(orphanStep, orphanSteps.iterator().next());
    }
}
