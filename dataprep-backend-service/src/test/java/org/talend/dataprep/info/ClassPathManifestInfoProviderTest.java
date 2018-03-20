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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ClassPathManifestInfoProviderTest {

    @Test
    public void shouldReadFromFile() throws Exception {
        // given
        final ClassPathManifestInfoProvider provider = new ClassPathManifestInfoProvider("/git.properties", "source1");

        // when
        final ManifestInfo manifestInfo = provider.getManifestInfo();

        // then
        assertEquals("1.0.0", manifestInfo.getVersionId());
        assertEquals("abcd1234", manifestInfo.getBuildId());
    }

    @Test
    public void shouldReadFromMissingFile() throws Exception {
        // given
        final ClassPathManifestInfoProvider provider = new ClassPathManifestInfoProvider("/intentionally_missing_file.properties", "source2");

        // when
        final ManifestInfo manifestInfo = provider.getManifestInfo();

        // then
        assertEquals("N/A", manifestInfo.getVersionId());
        assertEquals("N/A", manifestInfo.getBuildId());
    }

}
