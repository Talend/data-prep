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

package org.talend.dataprep.info;

public class BuildDetails {

    private String displayVersion;

    private Version[] services;

    public BuildDetails() {
        // needed for the json de/serialization
    }

    public BuildDetails(String displayVersion, Version[] services) {
        this.displayVersion = displayVersion;
        this.services = services;
    }

    public String getDisplayVersion() {
        return displayVersion;
    }

    public void setDisplayVersion(String displayVersion) {
        this.displayVersion = displayVersion;
    }

    public Version[] getServices() {
        return services;
    }

    public void setServices(Version[] services) {
        this.services = services;
    }
}
