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

package org.talend.dataprep.api.service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.service.version.VersionsSupplier;
import org.talend.dataprep.info.BuildDetails;
import org.talend.dataprep.info.Version;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;

import io.swagger.annotations.ApiOperation;

@RestController
public class VersionServiceAPI extends APIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionServiceAPI.class);

    @Value("${dataprep.display.version}")
    private String applicationVersion;

    @Autowired
    private List<VersionsSupplier> versionsSuppliers;

    /**
     * Returns all the versions of the different services (api, dataset, preparation and transformation) and the global
     * application version.
     *
     * @return an array of service versions
     */
    @RequestMapping(value = "/api/version", method = GET)
    @ApiOperation(value = "Get the version of all services (including underlying low level services)",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PublicAPI
    public BuildDetails allVersions() {
        final List<Version> versions = new ArrayList<>();

        for (VersionsSupplier versionsSupplier : versionsSuppliers) {
            versions.addAll(versionsSupplier.getVersions());
        }
        CollectionUtils.filter(versions, PredicateUtils.notNullPredicate());

        return new BuildDetails(applicationVersion, versions.toArray(new Version[0]));
    }
}
