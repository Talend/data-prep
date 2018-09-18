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

import static org.apache.commons.lang3.StringUtils.leftPad;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines an upgrade task getId.
 */
public class UpgradeTaskId implements Comparable<UpgradeTaskId> {

    /** The target application getVersion. */
    private String version;

    /** The task 'human readable' name. */
    private String name;

    /** The order of the task. */
    private int order;

    /**
     * Default empty constructor.
     */
    public UpgradeTaskId() {
        // needed for json serialization
    }

    /**
     * Full constructor.
     *
     * @param version the task target version.
     * @param name the task 'human readable' name.
     * @param order the order of this task to perform within this version upgrade.
     */
    public UpgradeTaskId(String version, String name, int order) {
        this.version = version;
        this.name = name;
        this.order = order;
    }

    /**
     * @return the Version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the getVersion to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the Name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the Order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order the order to set.
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "UpgradeTaskId{" + "version='" + version + '\'' + ", name='" + name + '\'' + ", order=" + order + '}';
    }

    /**
     *
     * @return the key used to id this task.
     */
    public String getUniqueKey() {
        return version + '_' + order + '_' + name;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(UpgradeTaskId other) {

        if (other == null) {
            return 1;
        }

        // compare version only if needed
        if (!StringUtils.equals(version, other.getVersion())) {

            // split with by '.'
            final String[] versionParts = version.split("\\.");
            final String[] otherVersionParts = other.getVersion().split("\\.");

            // compare each part with a left pad so that '1-PE' < '2-EE' < '100-PE'
            for (int i = 0; i < versionParts.length; i++) {
                String currentPart = leftPad(versionParts[i], 6, '0');

                // if there's no match for the other version, let's consider 0
                String otherPart = "000000";
                if (i < otherVersionParts.length) {
                    otherPart = leftPad(otherVersionParts[i], 6, '0');
                }

                final int result = currentPart.compareTo(otherPart);
                if (result != 0) {
                    return result;
                }
            }
        }

        // then order
        return Integer.compare(order, other.getOrder());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UpgradeTaskId that = (UpgradeTaskId) o;

        if (order != that.order)
            return false;
        if (version != null ? !version.equals(that.version) : that.version != null)
            return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + order;
        return result;
    }
}
