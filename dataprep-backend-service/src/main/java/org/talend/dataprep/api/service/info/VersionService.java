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

import static java.util.Arrays.asList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
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

    @Autowired(required = false)
    List<ManifestInfoProvider> manifestInfoProviders;

    /**
     * @param manifestInfoProviders The new {@link ManifestInfoProvider providers} to use to extract build id and version id
     * from.
     */
    public void setManifestInfoProviders(List<ManifestInfoProvider> manifestInfoProviders) {
        this.manifestInfoProviders = manifestInfoProviders;
    }

    /**
     * @return A {@link Version} built following these conventions:
     * <ul>
     * <li>Concatenation of all {@link ManifestInfo} build ids (as returned by {@link ManifestInfo#getBuildId()}), separated
     * by "-". Value to be returned by {@link Version#getBuildId()}.</li>
     * <li>Concatenation of all <b>unique</b>{@link ManifestInfo} versions ids (as returned by
     * {@link ManifestInfo#getVersionId()} ()}), separated by "-" (when more than one version found). Value to be returned by {@link Version#getVersionId()}</li>
     * </ul>
     */
    @RequestMapping(value = "/version", method = GET)
    @ApiOperation(value = "Get the version of the service", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PublicAPI
    public Version version() {
        List<String> preferredOrder = asList("OS", "EE", "OPS");
        String buildId = manifestInfoProviders.stream() //
                .sorted(Comparator.comparingInt(provider -> {
                    if (provider.getName() != null) {
                        return preferredOrder.indexOf(provider.getName().toUpperCase());
                    }
                    return 0;
                })) //
                .map(ManifestInfoProvider::getManifestInfo) //
                .map(ManifestInfo::getBuildId) //
                .collect(Collectors.joining("-"));
        final Optional<String> uniqueVersions = manifestInfoProviders.stream() //
                .map(ManifestInfoProvider::getManifestInfo) //
                .map(ManifestInfo::getVersionId) //
                .filter(versionId -> !StringUtils.equals("N/A", versionId)) //
                .reduce((s, s2) -> {
                    if (StringUtils.equals(s, s2)) {
                        return s;
                    }
                    return s + '-' + s2;
                });
        String versionId = uniqueVersions.orElse("N/A");

        return new Version(versionId, buildId);
    }
}
