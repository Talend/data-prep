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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.talend.dataprep.info.Version.fromInternal;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.daikon.security.CryptoHelper;
import org.talend.daikon.token.TokenGenerator;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.api.service.upgrade.UpgradeServerVersion;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.security.PublicAPI;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.zafarkhaja.semver.Version;

import io.swagger.annotations.ApiOperation;

@RestController
public class UpgradeAPI extends APIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeAPI.class);

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private VersionService service;

    @Value("${upgrade.location:}")
    private String upgradeVersionLocation;

    private String token;

    private static String toString(List<UpgradeServerVersion> versions) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        for (UpgradeServerVersion version : versions) {
            builder.append(version.getVersion()).append(' ');
        }
        builder.append(']');
        return builder.toString();
    }

    @PostConstruct
    public void init() {
        token = TokenGenerator.generateMachineToken(new CryptoHelper("DataPrepIsSoCool"));
        LOGGER.debug("Installation token: {}", token);
    }

    // Here for unit test purposes
    void setUpgradeVersionLocation(String upgradeVersionLocation) {
        this.upgradeVersionLocation = upgradeVersionLocation;
    }

    @RequestMapping(value = "/api/upgrade/check", method = GET)
    @ApiOperation(value = "Checks if a newer versions are available and returns them as JSON.", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PublicAPI
    public Stream<UpgradeServerVersion> check() {

        // defensive programming
        if (StringUtils.isBlank(upgradeVersionLocation)) {
            return Stream.empty();
        }

        try {
            // Get current version
            final Version parsedCurrentVersion = fromInternal(service.version());

            // POST to URL that serves a JSON Version object
            LOGGER.debug("Contacting upgrade server @ '{}'", upgradeVersionLocation);
            List<UpgradeServerVersion> versions = fetchServerUpgradeVersions(service.version());
            LOGGER.debug("{} available version(s) returned by update server: {}", versions.size(), toString(versions));

            // Compare current version with available and filter new versions
            return versions.stream().filter(v -> Version.valueOf(v.getVersion()).greaterThan(parsedCurrentVersion));
        } catch (Exception e) {
            LOGGER.error("Unable to check for new version (message: {}).", e.getMessage());
            LOGGER.debug("Exception occurred during new version check. ", e);
            return Stream.empty();
        }
    }

    private List<UpgradeServerVersion> fetchServerUpgradeVersions(org.talend.dataprep.info.Version version) throws IOException {
        final HttpPost post = new HttpPost(upgradeVersionLocation);
        final String response;
        final StringWriter content = new StringWriter();
        try (final JsonGenerator generator = mapper.getFactory().createGenerator(content)) {
            generator.writeStartObject();
            generator.writeStringField("version", version.getVersionId());
            generator.writeStringField("id", token);
            generator.writeEndObject();
            generator.flush();

            post.setEntity(new StringEntity(content.toString(), ContentType.APPLICATION_JSON.withCharset(UTF_8)));
            response = IOUtils.toString(httpClient.execute(post).getEntity().getContent(), UTF_8);
        } finally {
            post.releaseConnection();
        }

        // Read upgrade server response
        return mapper.readerFor(new TypeReference<List<UpgradeServerVersion>>() {
        }).readValue(response);
    }

}
