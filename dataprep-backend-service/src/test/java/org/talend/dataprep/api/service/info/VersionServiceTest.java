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

package org.talend.dataprep.api.service.info;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.info.ManifestInfo;
import org.talend.dataprep.info.ManifestInfoProvider;
import org.talend.dataprep.info.Version;

@RunWith(MockitoJUnitRunner.class)
public class VersionServiceTest {

    @InjectMocks
    private VersionService versionService;

    @Test
    public void shouldAggregateBuildId() throws Exception {
        // given
        final ManifestInfoProvider provider1 = mock(ManifestInfoProvider.class);
        final ManifestInfoProvider provider2 = mock(ManifestInfoProvider.class);
        when(provider1.getManifestInfo()).thenReturn(new ManifestInfo("v1", "1234"));
        when(provider2.getManifestInfo()).thenReturn(new ManifestInfo("v1", "5678"));
        versionService.setManifestInfoProviders(asList(provider1, provider2));

        // when
        final Version version = versionService.version();

        // then
        assertEquals("v1", version.getVersionId());
        assertEquals("1234-5678", version.getBuildId());
    }

    @Test
    public void shouldAggregateSameBuildId() throws Exception {
        // given
        final ManifestInfoProvider provider1 = mock(ManifestInfoProvider.class);
        final ManifestInfoProvider provider2 = mock(ManifestInfoProvider.class);
        when(provider1.getManifestInfo()).thenReturn(new ManifestInfo("v1", "1234"));
        when(provider2.getManifestInfo()).thenReturn(new ManifestInfo("v1", "1234"));
        versionService.setManifestInfoProviders(asList(provider1, provider2));

        // when
        final Version version = versionService.version();

        // then
        assertEquals("v1", version.getVersionId());
        assertEquals("1234-1234", version.getBuildId());
    }

    @Test
    public void shouldAggregateVersionId() throws Exception {
        // given
        final ManifestInfoProvider provider1 = mock(ManifestInfoProvider.class);
        final ManifestInfoProvider provider2 = mock(ManifestInfoProvider.class);
        when(provider1.getManifestInfo()).thenReturn(new ManifestInfo("v1", "1234"));
        when(provider2.getManifestInfo()).thenReturn(new ManifestInfo("v2", "1234"));
        versionService.setManifestInfoProviders(asList(provider1, provider2));

        // when
        final Version version = versionService.version();

        // then
        assertEquals("v1-v2", version.getVersionId());
        assertEquals("1234-1234", version.getBuildId());
    }

    @Test
    public void shouldShouldSkipMissingVersionId() throws Exception {
        // given
        final ManifestInfoProvider provider1 = mock(ManifestInfoProvider.class);
        final ManifestInfoProvider provider2 = mock(ManifestInfoProvider.class);
        when(provider1.getManifestInfo()).thenReturn(new ManifestInfo("v1", "1234"));
        when(provider2.getManifestInfo()).thenReturn(new ManifestInfo("N/A", "1234"));
        versionService.setManifestInfoProviders(asList(provider1, provider2));

        // when
        final Version version = versionService.version();

        // then
        assertEquals("v1", version.getVersionId());
        assertEquals("1234-1234", version.getBuildId());
    }


}
