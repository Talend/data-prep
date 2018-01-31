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

package org.talend.dataprep.upgrade.repository.model;

import java.util.Date;
import java.util.Objects;

import org.talend.dataprep.upgrade.model.UpgradeTaskId;

/**
 * Bean used to store applied upgrade task.
 */
public class AppliedUpgradeTask {

    /** When was the task applied. */
    private Date when;

    /** The task description applied. */
    private String description;

    /**
     * Default empty constructor.
     */
    public AppliedUpgradeTask() {
        // needed for json serialization
        this.when = new Date();
    }

    /**
     * Create an applied upgrade task out of the given parameters.
     *
     * @param task the task applied.
     */
    public AppliedUpgradeTask(UpgradeTaskId task) {
        this();
        this.description = task.getUniqueKey();
    }

    /**
     * @return the When
     */
    public Date getWhen() {
        return when;
    }

    /**
     * @param when the when to set.
     */
    public void setWhen(Date when) {
        this.when = when;
    }

    /**
     * @return the Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "AppliedUpgradeTask{" + "when=" + when + ", description=" + description + '}';
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppliedUpgradeTask that = (AppliedUpgradeTask) o;
        return Objects.equals(when, that.when) && Objects.equals(description, that.description);
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(when, description);
    }
}
