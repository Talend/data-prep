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

package org.talend.dataprep.api.service.version;

import java.util.List;

import org.talend.dataprep.info.Version;

/**
 * Supplier of services or component versions, like TDP API, or TDQ Semantic Types Consumer.
 */
public interface VersionsSupplier {

    List<Version> getVersions();

}
