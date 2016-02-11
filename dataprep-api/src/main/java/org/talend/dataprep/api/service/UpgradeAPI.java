package org.talend.dataprep.api.service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.talend.daikon.security.CryptoHelper;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.api.service.upgrade.UpgradeServerVersion;
import org.talend.dataprep.metrics.Timed;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;

@ConditionalOnProperty(name = "upgrade.location")
@RestController
public class UpgradeAPI extends APIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeAPI.class);

    @Autowired
    VersionService service;

    @Autowired
    ObjectMapper mapper;

    @Value("${upgrade.location}")
    String upgradeVersionLocation;

    private String token;

    /**
     * Compute an ID unique for the machine that runs this code. Information includes:
     * <ul>
     * <li>MAC Address of the localhost interface (as returned by {@link InetAddress#getLocalHost()}).</li>
     * <li>OS Name and version</li>
     * <li>Local host name</li>
     * </ul>
     * Should any of the 3 steps above fails, code falls back for a random UUID.
     *
     * @param cryptoHelper A non-null {@link CryptoHelper helper} used to encrypt generated machine ID.
     * @return A non-empty, non-null and encrypted id computed based on information of the running machine.
     * @see InetAddress#getLocalHost()
     * @see UUID#randomUUID()
     */
    public static String generateMachineId(CryptoHelper cryptoHelper) {
        if (cryptoHelper == null) {
            throw new IllegalArgumentException("Crypto helper cannot be null."); //$NON-NLS-1$
        }
        final StringBuilder sb = new StringBuilder();
        try {
            final InetAddress loopBackAddress = InetAddress.getLocalHost();
            // Add all machine's MAC addresses ...
            final Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                final NetworkInterface network = networks.nextElement();
                final byte[] mac = network.getHardwareAddress();
                if (mac != null) { // null mac address can be returned by getHardwareAddress().
                    StringBuilder macAddress = new StringBuilder();
                    for (byte macByte : mac) {
                        macAddress.append(String.format("%02X", macByte)); //$NON-NLS-1$
                    }
                    if (!"0000000000E0".equals(macAddress.toString())) { // Skip empty mac address (if any)
                        sb.append(macAddress.toString());
                    }
                }
            }
            sb.append('-');
            // ... then OS ...
            sb.append(System.getProperty("os.name"));
            sb.append(System.getProperty("os.version"));
            // ... host name ...
            sb.append('-').append(loopBackAddress.getHostName());
        } catch (SocketException | UnknownHostException e) {
            LOGGER.debug("Unable to get local MAC address, fall back to UUID.", e); //$NON-NLS-1$
            sb.append(UUID.randomUUID().toString());
        }

        // ... and encode the result with a static pass phrase.
        final String machineId = cryptoHelper.encrypt(sb.toString());
        LOGGER.debug("Generated machine id: {}", machineId);
        return machineId;
    }

    private static String toString(List<UpgradeServerVersion> versions) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        for (UpgradeServerVersion version : versions) {
            builder.append(version.getVersion()).append(' ');
        }
        builder.append(']');
        return builder.toString();
    }

    // Shared with unit test
    static com.github.zafarkhaja.semver.Version parseVersion(String s) {
        final String versionAsString = StringUtils.substringBefore(s, "-");
        final int[] versionNumbers = new int[3];
        StringTokenizer tokenizer = new StringTokenizer(versionAsString, ".");
        int i = 0;
        while (tokenizer.hasMoreTokens() && i < 3) {
            versionNumbers[i++] = Integer.parseInt(tokenizer.nextToken());
        }
        return com.github.zafarkhaja.semver.Version.forIntegers(versionNumbers[0], versionNumbers[1], versionNumbers[2]);
    }

    @PostConstruct
    public void init() {
        token = generateMachineId(new CryptoHelper("DataPrepIsSoCool"));
        LOGGER.debug("Installation token: {}", token);
    }

    // Here for unit test purposes
    void setUpgradeVersionLocation(String upgradeVersionLocation) {
        this.upgradeVersionLocation = upgradeVersionLocation;
    }

    @RequestMapping(value = "/api/upgrade/check", method = GET)
    @ApiOperation(value = "Checks if a newer versions are available and returns them as JSON.", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<UpgradeServerVersion> check() {
        try {
            // Get current version
            final String versionId = service.version().getVersionId();
            final com.github.zafarkhaja.semver.Version parsedCurrentVersion = parseVersion(versionId);

            // POST to URL that serves a JSON Version object
            LOGGER.debug("Contacting upgrade server @ '{}'", upgradeVersionLocation);
            HttpClient client = new HttpClient();
            final PostMethod post = new PostMethod(upgradeVersionLocation);
            final String response;
            final StringWriter content = new StringWriter();
            try (final JsonGenerator generator = mapper.getFactory().createGenerator(content)) {
                generator.writeStartObject();
                {
                    generator.writeStringField("version", versionId);
                    generator.writeStringField("id", token);
                }
                generator.writeEndObject();
                generator.flush();

                post.setRequestEntity(new StringRequestEntity(content.toString(), MediaType.APPLICATION_JSON.getType(), "UTF-8"));
                client.executeMethod(post);
                response = IOUtils.toString(post.getResponseBodyAsStream());
            } finally {
                post.releaseConnection();
            }

            // Read upgrade server response
            List<UpgradeServerVersion> versions = mapper.readerFor(new TypeReference<List<UpgradeServerVersion>>() {
            }).readValue(response);
            LOGGER.debug("{} available version(s) returned by update server: {}", versions.size(), toString(versions));

            // Compare current version with available and filter new versions
            List<UpgradeServerVersion> filteredVersions = versions.stream().filter(v -> {
                com.github.zafarkhaja.semver.Version parsedRemoteVersion = parseVersion(v.getVersion());
                return parsedRemoteVersion.compareTo(parsedCurrentVersion) > 0;
            }).collect(Collectors.toList());
            LOGGER.debug("{} possible version(s) for upgrade: {} ", filteredVersions.size(), toString(filteredVersions));
            return filteredVersions;
        } catch (Exception e) {
            LOGGER.error("Unable to check for new version (message: {}).", e.getMessage());
            LOGGER.debug("Exception occurred during new version check. ", e);
            return Collections.emptyList();
        }
    }

}
