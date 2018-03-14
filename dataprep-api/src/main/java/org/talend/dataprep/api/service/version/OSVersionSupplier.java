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

package org.talend.dataprep.api.service.version;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.info.Version;

@Component
@Order(value = 1)
public class OSVersionSupplier extends AbstractVersionSupplier {

    @Value("${transformation.service.url}")
    protected String transformationServiceUrl;

    @Value("${dataset.service.url}")
    protected String datasetServiceUrl;

    @Value("${preparation.service.url}")
    protected String preparationServiceUrl;

    @Autowired
    private VersionService versionService;

    @Override
    public List<Version> getVersions() {
        final List<Version> versions = new ArrayList<>(4);

        final Version apiVersion = versionService.version();
        apiVersion.setServiceName("API");
        versions.add(apiVersion);
        versions.add(callVersionService(datasetServiceUrl, "Dataset", VERSION_ENTRY_POINT));
        versions.add(callVersionService(preparationServiceUrl, "Preparation", VERSION_ENTRY_POINT));
        versions.add(callVersionService(transformationServiceUrl, "Transformation", VERSION_ENTRY_POINT));

        return versions;
    }

}
