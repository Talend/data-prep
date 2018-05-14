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

package org.talend.dataprep.upgrade.model;

/**
 * Interface that defines an upgrade task.
 */
public interface UpgradeTask extends Comparable<UpgradeTask> {

    /**
     * Upgrade task target.
     * 
     * @since 1.3.0
     */
    enum target {
        /** Applied once per version per user. */
        USER,
        /** Applied once per version only. */
        VERSION,
        /** Applied only when application is ready to service requests. */
        POST_STARTUP
    }

    /**
     * @return the upgrade task id.
     */
    UpgradeTaskId getId();

    /**
     * To run the upgrade task.
     */
    void run();

    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    default int compareTo(UpgradeTask other) {
        if (other == null) {
            return 1;
        }
        return this.getId().compareTo(other.getId());
    }

    /**
     * Return the task id that matches the given parameters.
     *
     * @param version the wanted version.
     * @param name the wanted name.
     * @return the task id that matches the given parameters.
     */
    default UpgradeTaskId getTaskId(String version, String name) {
        UpgradeTaskId id = new UpgradeTaskId();
        id.setVersion(version);
        id.setName(name);
        id.setOrder(getOrder());
        return id;
    }

    /**
     * @return the task order for the 1.2.0-PE upgrade.
     */
    int getOrder();

    /**
     * @return this upgrade task target.
     * @since 1.3.0
     */
    target getTarget();

}
