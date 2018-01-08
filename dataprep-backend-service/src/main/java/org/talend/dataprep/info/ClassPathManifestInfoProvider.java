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

package org.talend.dataprep.info;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassPathManifestInfoProvider implements ManifestInfoProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathManifestInfoProvider.class);

    private final String resource;

    private final String name;

    public ClassPathManifestInfoProvider(String resource, String name) {
        this.resource = resource;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ManifestInfo getManifestInfo() {
        final Properties properties = new Properties();
        final InputStream gitProperties = ClassPathManifestInfoProvider.class.getResourceAsStream(resource);
        if (gitProperties != null) {
            try {
                properties.load(gitProperties);
            } catch (IOException e) {
                LOGGER.debug("Unable to read from resource '{}'.", resource, e);
            }
        } else {
            LOGGER.debug("Resource '{}' does not exist.", resource);
        }
        final String versionId = Optional.ofNullable(properties.getProperty("git.build.version")).orElse("N/A");
        final String buildId = Optional.ofNullable(properties.getProperty("git.commit.id.abbrev")).orElse("N/A");
        return new ManifestInfo(versionId, buildId);
    }
}
