// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
@FunctionalInterface
public interface ManifestInfoProvider {

    /**
     * @return An instance of {@link ManifestInfo} that corresponds to current context.
     */
    ManifestInfo getManifestInfo();
}
