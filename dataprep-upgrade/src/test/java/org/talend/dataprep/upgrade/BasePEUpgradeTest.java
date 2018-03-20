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

package org.talend.dataprep.upgrade;

import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.upgrade.model.UpgradeTaskId;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all PE upgrade tasks.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BasePEUpgradeTest.class)
@ComponentScan("org.talend.dataprep")
@Configuration
public abstract class BasePEUpgradeTest {

    /** This class' logger. */
    private static final Logger LOG = getLogger(BasePEUpgradeTest.class);

    @Autowired
    private ConfigurableEnvironment environment;

    /**
     * Set the store up
     *
     * @param version the store version.
     * @throws IOException when an error occured.
     */
    protected static void setupStore(String version) throws IOException, URISyntaxException {
        Path source = Paths.get(BasePEUpgradeTest.class.getResource("/snapshots/" + version).toURI());
        Path dest = Paths.get("target/test", version);
        if (Files.exists(dest)) {
            FileUtils.deleteDirectory(dest.toFile());
        }
        Files.createDirectories(dest);
        FileUtils.copyDirectory(source.toFile(), dest.toFile());

        LOG.info("{} store copied to {}", version, dest);
    }

    @Test
    public void shouldCheckId() throws Exception {

        // when
        UpgradeTaskId id = getTaskId();

        // then
        assertEquals(getExpectedVersion(), id.getVersion());
        assertEquals(getExpectedTaskOrder(), id.getOrder());

    }

    @Bean
    public ObjectMapper jacksonBuilder() {
        return json().featuresToDisable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES).indentOutput(false).build();
    }

    /**
     * @return the task id.
     */
    protected abstract UpgradeTaskId getTaskId();

    /**
     * @return the expected task order.
     */
    protected abstract int getExpectedTaskOrder();

    /**
     * @return the expected version.
     */
    protected abstract String getExpectedVersion();

}
