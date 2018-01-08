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

import java.util.Set;

import org.talend.dataprep.api.preparation.Step;

/**
 * Interface that defines a way to retrieve orphan steps (steps that are not used anymore).
 */
public interface OrphanStepsFinder {

    /**
     * @return all orphan steps.
     */
    Set<Step> getOrphanSteps();
}
