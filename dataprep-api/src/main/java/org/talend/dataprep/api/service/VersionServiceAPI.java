package org.talend.dataprep.api.service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.service.command.info.VersionCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.info.ManifestInfo;
import org.talend.dataprep.info.Version;
import org.talend.dataprep.metrics.Timed;

import com.netflix.hystrix.HystrixCommand;
import io.swagger.annotations.ApiOperation;

@RestController
@Scope("request")
public class VersionServiceAPI extends APIService {

    @Autowired
    Jackson2ObjectMapperBuilder builder;

    @Value("${transformation.service.url}")
    protected String transformationServiceUrl;

    @Value("${dataset.service.url}")
    protected String datasetServiceUrl;

    @Value("${preparation.service.url}")
    protected String preparationServiceUrl;

    @RequestMapping(value = "/api/version", method = GET)
    @ApiOperation(value = "Get the version of all services (including underlying low level services)", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    /**
     * Returns all the versions of the different services (api, dataset, preparation and transformation).
     * 
     * @return an array of service versions
     */
    public Version[] allVersions() {
        Version[] versions = new Version[4];
        ManifestInfo manifestInfo = ManifestInfo.getInstance();

        versions[0] = new Version(manifestInfo.getVersionId(), manifestInfo.getBuildId(), "API");
        versions[1] = callVersionService(datasetServiceUrl, "DATASET");
        versions[2] = callVersionService(preparationServiceUrl, "PREPARATION");
        versions[3] = callVersionService(transformationServiceUrl, "TRANSFORMATION");

        return versions;
    }

    /**
     * Call the version service on the given service: dataset, preparation or transformation.
     * 
     * @param serviceName the name of the service
     * @return the version of the called service
     */
    private Version callVersionService(String serviceUrl, String serviceName) {
        HttpClient client = getClient();
        HystrixCommand<InputStream> versionCommand = getCommand(VersionCommand.class, client, serviceUrl);
        try (InputStream content = versionCommand.execute()) {
            final Version version = builder.build().readerFor(Version.class).readValue(content);
            version.setServiceName(serviceName);
            return version;
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_GET_SERVICE_VERSION, e);
        }
    }

}