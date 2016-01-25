package org.talend.dataprep.api.service.info;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.info.ManifestInfo;
import org.talend.dataprep.info.Version;
import org.talend.dataprep.metrics.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "version", basePath = "/version", description = "versions of running application")
public class VersionService {

    @RequestMapping(value = "/version", method = GET)
    @ApiOperation(value = "Get the version of the service", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public Version version() {
        ManifestInfo manifestInfo = ManifestInfo.getInstance();
        return new Version(manifestInfo.getVersionId(), manifestInfo.getBuildId());
    }
}