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

package org.talend.dataprep.api.service.settings.views.provider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.security.Security;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for CommonViewsProvider class.
 *
 * @see CommonViewsProvider
 */
@RunWith(MockitoJUnitRunner.class)
public class CommonViewsProviderTest {

    @Mock
    private Security security;

    @InjectMocks
    private CommonViewsProvider provider;

    @Test
    public void shouldGetProviderForTDPUsers() {
        // given
        Mockito.when(security.isTDPUser()).thenReturn(true);

        // when
        final List<ViewSettings> settings = provider.getSettings();

        // then
        assertNotNull(settings);
        assertEquals(settings.size(), 6);
        assertTrue(settings.contains(HomeViews.appHeaderBar()));
        assertTrue(settings.contains(HomeViews.sidePanel()));
        assertTrue(settings.contains(HomeViews.breadcrumb()));
        assertTrue(settings.contains(ListViews.folderList()));
        assertTrue(settings.contains(ListViews.preparationList()));
        assertTrue(settings.contains(PlaygroundViews.playgroundAppHeaderBar()));
    }

    @Test
    public void shouldGetProviderForNonTDPUsers() {
        // given
        Mockito.when(security.isTDPUser()).thenReturn(false);

        // when
        final List<ViewSettings> settings = provider.getSettings();

        // then
        assertNotNull(settings);
        assertEquals(4, settings.size());
        assertTrue(settings.contains(HomeViewsForNonTDPUsers.appHeaderBarForNonTdpUsers()));
        assertTrue(settings.contains(HomeViewsForNonTDPUsers.sidePanel()));
        assertTrue(settings.contains(ListViews.folderList()));
        assertTrue(settings.contains(ListViews.preparationList()));
    }
}
