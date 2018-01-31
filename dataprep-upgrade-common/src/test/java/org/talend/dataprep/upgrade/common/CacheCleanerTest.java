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

package org.talend.dataprep.upgrade.common;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.cache.ContentCache;

/**
 * Unit test for the org.talend.dataprep.upgrade.common.CacheCleaner class.
 *
 * @see CacheCleaner
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheCleanerTest {

    @Mock
    private ContentCache contentCache;

    @InjectMocks
    private CacheCleaner cacheCleaner;

    @Test
    public void shouldCleanCache() throws Exception {
        // when
        cacheCleaner.cleanCache();

        // then
        verify(contentCache).clear();
    }
}