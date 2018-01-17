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

package org.talend.dataprep.cache.file;

import org.springframework.test.context.TestPropertySource;

/**
 * Implementation of tests for local content cache.
 */
@TestPropertySource(properties = { "content-service.store=local", "content-service.store.local.path=${java.io.tmpdir}/dataprep" })
public class LocalContentCacheTest extends ContentCacheTests {
}
