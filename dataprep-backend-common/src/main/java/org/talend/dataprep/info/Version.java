//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.info;

import org.apache.commons.lang.StringUtils;

public class Version {

    private String versionId;

    private String buildId;

    private String serviceName;

    public Version() {
    }

    public Version(String versionId, String buildId) {
        this(versionId, buildId, StringUtils.EMPTY);
    }

    public Version(String versionId, String buildId, String serviceName) {
        this.versionId = versionId;
        this.buildId = buildId;
        this.serviceName = serviceName;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "{" +
                "versionId='" + versionId + '\'' +
                ", buildId='" + buildId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Version version = (Version) o;

        if (versionId != null ? !versionId.equals(version.versionId) : version.versionId != null)
            return false;
        if (buildId != null ? !buildId.equals(version.buildId) : version.buildId != null)
            return false;
        return !(serviceName != null ? !serviceName.equals(version.serviceName) : version.serviceName != null);

    }

    @Override
    public int hashCode() {
        int result = versionId != null ? versionId.hashCode() : 0;
        result = 31 * result + (buildId != null ? buildId.hashCode() : 0);
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        return result;
    }

}
