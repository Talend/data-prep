//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

/**
 * Contains the information info the version of running application
 */
public class ManifestInfo {

    /**
     * The version ID
     */
    private final String versionId;

    /**
     * The ID (from the SCM) of the source's version of the running application
     */
    private final String buildId;

    public ManifestInfo(String versionId, String buildId) {
        this.versionId = versionId;
        this.buildId = buildId;
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
