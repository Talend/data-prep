// ============================================================================
//
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

package org.talend.dataprep.upgrade.repository;

import org.talend.dataprep.upgrade.model.UpgradeTaskId;

/**
 * Upgrade task repository to know what task is applied.
 */
public interface UpgradeTaskRepository {

    /**
     * Check if the given upgrade task is already applied for the target id.
     * 
     * @param targetId the current target id (version or user).
     * @param id the upgrade task.
     * @return true if the given upgrade task id is already applied.
     * @see org.talend.dataprep.upgrade.model.UpgradeTask.target
     */
    boolean isAlreadyApplied(String targetId, UpgradeTaskId id);

    /**
     * Register the given upgrade task id as applied for the target.
     *
     * @param targetId the current target id.
     * @param id the upgrade task id.
     */
    void applied(String targetId, UpgradeTaskId id);

    /**
     * Count the number of applied tasks.
     * 
     * @param targetPrefix filter counted applied tasks on their targetId based on this prefix.
     * @return the number of applied tasks.
     */
    int countUpgradeTask(String targetPrefix);

}
