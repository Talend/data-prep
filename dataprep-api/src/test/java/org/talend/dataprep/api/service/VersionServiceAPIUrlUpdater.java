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

package org.talend.dataprep.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.talend.dataprep.url.UrlRuntimeUpdater;

/**
 * Update the VersionServiceAPI urls
 */
@Component
@Lazy
public class VersionServiceAPIUrlUpdater extends UrlRuntimeUpdater {

    @Value("${local.server.port}")
    protected int port;

    @Autowired
    private VersionServiceAPI versionServiceAPI;

    public void setUp() {
        setField(versionServiceAPI, "transformationServiceUrl", "http://localhost:" + port);
        setField(versionServiceAPI, "datasetServiceUrl", "http://localhost:" + port);
        setField(versionServiceAPI, "preparationServiceUrl", "http://localhost:" + port);
    }
}
