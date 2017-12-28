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

public class GlobalVersion {

    private String application;

    private Version[] services;

    public GlobalVersion() {
        // needed for the json de/serialization
    }

    public GlobalVersion(String application, Version[] services) {
        this.application = application;
        this.services = services;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Version[] getServices() {
        return services;
    }

    public void setServices(Version[] services) {
        this.services = services;
    }
}
