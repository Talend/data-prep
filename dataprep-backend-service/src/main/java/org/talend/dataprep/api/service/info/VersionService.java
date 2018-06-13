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

package org.talend.dataprep.api.service.info;

import static java.util.stream.Collectors.joining;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.info.ManifestInfo;
import org.talend.dataprep.info.ManifestInfoProvider;
import org.talend.dataprep.info.Version;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Rest controller that returns current version of the Data Prep service.
 *
 * @see ManifestInfoProvider
 * @see Version
 */
@RestController
@Api(value = "version", basePath = "/version", description = "versions of running application")
public class VersionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionService.class);

    private static final String UNDEFINED_VERSION_ID = "N/A";

    @Autowired(required = false)
    private List<ManifestInfoProvider> manifestInfoProviders;

    /**
     * @return A {@link Version} built following these conventions:
     * <ul>
     * <li>Concatenation of all {@link ManifestInfo} build ids (as returned by {@link ManifestInfo#getBuildId()}),
     * separated
     * by "-". Value to be returned by {@link Version#getBuildId()}.</li>
     * <li>Concatenation of all <b>unique</b>{@link ManifestInfo} versions ids (as returned by
     * {@link ManifestInfo#getVersionId()} ()}), separated by "-" (when more than one version found). Value to be
     * returned by {@link Version#getVersionId()}</li>
     * </ul>
     */
    @RequestMapping(value = "/version", method = GET)
    @ApiOperation(value = "Get the version of the service", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PublicAPI
    public Version version() {
        String buildId = manifestInfoProviders
                .stream() //
                .map(ManifestInfoProvider::getManifestInfo) //
                .map(ManifestInfo::getBuildId) //
                .collect(joining("-"));
        final Optional<String> uniqueVersion = manifestInfoProviders
                .stream() //
                .map(ManifestInfoProvider::getManifestInfo) //
                .map(ManifestInfo::getVersionId) //
                .filter(versionId -> !StringUtils.equals(UNDEFINED_VERSION_ID, versionId)) //
                .reduce((s, s2) -> {
                    if (StringUtils.equals(s, s2)) {
                        return s;
                    }
                    return s + '-' + s2;
                });
        String serviceUnifiedVersionId = uniqueVersion.orElse(UNDEFINED_VERSION_ID);

        return new Version(serviceUnifiedVersionId, buildId);
    }
}
