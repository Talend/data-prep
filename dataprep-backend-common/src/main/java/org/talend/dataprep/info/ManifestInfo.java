//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.info;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the information info the version of running application
 */
public class ManifestInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestInfo.class);

    /**
     * The version ID
     */
    private String versionId;

    /**
     * The ID (from the SCM) of the source's version of the running application
     */
    private String buildId;
    
    /**
     * The unique instance of this singleton class
     */
    private static ManifestInfo instance = new ManifestInfo();

    private ManifestInfo() {
        Properties properties = new Properties();
        final InputStream gitProperties = ManifestInfo.class.getResourceAsStream("/git.properties");
        if (gitProperties != null) {
            try {
                properties.load(gitProperties);
                versionId = properties.getProperty("git.build.version");
                buildId = properties.getProperty("git.commit.id.abbrev");
            } catch (IOException ie) {
                LOGGER.debug("Unable to read from git.properties.", ie);
                versionId = "????";
                buildId = "????";
            }
        } else {
            LOGGER.debug("No git.properties found, most likely using a locally built service.");
            versionId = "LOCAL";
            buildId = "N/A";
        }
    }

    /**
     * @return Return the unique instance.
     */
    public static ManifestInfo getInstance() {
        return instance;
    }

    /**
     * @return the version of this running application
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * @return the SHA build
     */
    public String getBuildId() {
        return buildId;
    }

}