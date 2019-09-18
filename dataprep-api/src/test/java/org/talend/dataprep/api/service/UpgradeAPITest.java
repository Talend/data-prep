package org.talend.dataprep.api.service;

import static org.junit.Assert.assertEquals;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.daikon.security.CryptoHelper;
import org.talend.daikon.token.TokenGenerator;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.api.service.upgrade.UpgradeServerVersion;
import org.talend.dataprep.http.HttpResponseContext;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;
import com.jayway.restassured.RestAssured;

public class UpgradeAPITest extends ApiServiceTestBase {

    @Value("${local.server.port}")
    public int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UpgradeAPI upgradeAPI;

    @Autowired
    VersionService versionService;

    private static String computeNextVersion(VersionService versionService) {
        final Version parsedVersion = UpgradeAPI.parseVersion(versionService.version().getVersionId());
        return parsedVersion.incrementMajorVersion().toString();
    }

    @Test
    public void checkWithoutUpgradeServer() throws Exception {
        // When
        upgradeAPI.setUpgradeVersionLocation(""); // Wrong update URL

        // Then
        String actual = RestAssured.when().get("/api/upgrade/check").asString();
        assertEquals("[]", actual, true);
    }

    @Test
    public void checkUpgradeServerFailure() throws Exception {
        // When
        upgradeAPI.setUpgradeVersionLocation("http://localhost:" + port + "/upgrade/server/failure"); // Update URL that
                                                                                                      // fails

        // Then
        String actual = RestAssured.when().get("/api/upgrade/check").asString();
        assertEquals("[]", actual, true);
    }

    @Test
    @Ignore("No explicit fix - see TDP-7705")
    // two version returned, instead of one...
    public void checkStaticUpgradeServer() throws Exception {
        // When
        // Server deliver always same response based (does *not* depend on sent content)
        upgradeAPI.setUpgradeVersionLocation("http://localhost:" + port + "/upgrade/server/static");

        // Then
        String actual = RestAssured.when().get("/api/upgrade/check").asString();
        final String nextVersion = computeNextVersion(versionService);
        assertEquals(
                "[{\"version\":\"" + nextVersion
                        + "\",\"title\":\"My Title 2\",\"download_url\":\"http://www.amazingdownload.com/2\",\"release_note_url\":\"http://www.amazingrelease.com/2\"}]",
                actual, true);
    }

    @Test
    public void checkDynamicUpgradeServer() throws Exception {
        // When
        // Server deliver response based on sent content
        upgradeAPI.setUpgradeVersionLocation("http://localhost:" + port + "/upgrade/server/dynamic");

        // Then
        String response = RestAssured.when().get("/api/upgrade/check").asString();
        final List<UpgradeServerVersion> versions = mapper.readerFor(new TypeReference<List<UpgradeServerVersion>>() {
        }).readValue(response);
        assertEquals(1, versions.size());
        final UpgradeServerVersion actual = versions.get(0);

        final String expectedVersion = versionService.version().getVersionId();
        final String expectedToken = TokenGenerator.generateMachineToken(new CryptoHelper("DataPrepIsSoCool"));
        assertEquals(expectedToken, actual.getTitle());
        assertEquals(expectedVersion, actual.getDownloadUrl());
    }

    @RestController
    public static class UpgradeServer {

        @Autowired
        ObjectMapper mapper;

        @Autowired
        VersionService versionService;

        @RequestMapping(path = "/upgrade/server/failure", method = RequestMethod.POST)
        public List<UpgradeServerVersion> failure() {
            HttpResponseContext.status(HttpStatus.NOT_FOUND);
            return null;
        }

        @RequestMapping(path = "/upgrade/server/static", method = RequestMethod.POST)
        public List<UpgradeServerVersion> getAvailableVersions() {
            UpgradeServerVersion version1 = new UpgradeServerVersion();
            version1.version = "0.1";
            version1.title = "My Title 1";
            version1.downloadUrl = "http://www.amazingdownload.com/1";
            version1.releaseNoteUrl = "http://www.amazingrelease.com/1";

            UpgradeServerVersion version2 = new UpgradeServerVersion();
            version2.version = computeNextVersion(versionService);
            version2.title = "My Title 2";
            version2.downloadUrl = "http://www.amazingdownload.com/2";
            version2.releaseNoteUrl = "http://www.amazingrelease.com/2";

            return Arrays.asList(version1, version2);
        }

        @RequestMapping(path = "/upgrade/server/dynamic", method = RequestMethod.POST)
        public List<UpgradeServerVersion> getAvailableVersionsWithTokenAndVersion(final InputStream inputStream) {
            String inputId = "", inputVersion = "";
            try (final JsonParser parser = mapper.getFactory().createParser(inputStream)) {
                JsonToken jsonToken = parser.nextToken();
                while (jsonToken != JsonToken.END_OBJECT) {
                    if (jsonToken == JsonToken.FIELD_NAME) {
                        final String fieldName = parser.getValueAsString();
                        if ("id".equalsIgnoreCase(fieldName)) {
                            inputId = parser.nextTextValue();
                        } else if ("version".equalsIgnoreCase(fieldName)) {
                            inputVersion = parser.nextTextValue();
                        }
                    }
                    jsonToken = parser.nextToken();
                }
            } catch (IOException e) {
                LOGGER.error("Unexpected exception.", e);
            }
            UpgradeServerVersion version1 = new UpgradeServerVersion();
            version1.version = computeNextVersion(versionService);
            version1.title = inputId;
            version1.downloadUrl = inputVersion;

            return Collections.singletonList(version1);
        }
    }

}
