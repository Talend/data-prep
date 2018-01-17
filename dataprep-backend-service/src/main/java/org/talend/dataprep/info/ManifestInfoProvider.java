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

/**
 * A provider of {@link ManifestInfo} that may read from any source (a file, for example).
 */
public interface ManifestInfoProvider {

    /**
     * @return A non-null name for the source that can be used later on for identifying manifest info source (e.g. for
     * ordering).
     */
    String getName();

    /**
     * @return An instance of {@link ManifestInfo} that corresponds to current context.
     */
    ManifestInfo getManifestInfo();
}
