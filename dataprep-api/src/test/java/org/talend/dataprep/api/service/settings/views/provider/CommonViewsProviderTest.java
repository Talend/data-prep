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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.talend.dataprep.api.service.settings.views.api.ViewSettings;
import org.talend.dataprep.security.Security;

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
        assertEquals(settings.size(), 7);
        assertTrue(settings.contains(HomeViews.APP_HEADER_BAR));
        assertTrue(settings.contains(HomeViews.SIDE_PANEL));
        assertTrue(settings.contains(HomeViews.BREADCRUMB));
        assertTrue(settings.contains(ListViews.FOLDERS_LIST));
        assertTrue(settings.contains(ListViews.PREPARATIONS_LIST));
        assertTrue(settings.contains(ListViews.DATASETS_LIST));
        assertTrue(settings.contains(PlaygroundViews.PLAYGROUND_APP_HEADER_BAR));
    }

    @Test
    public void shouldGetProviderForNonTDPUsers() {
        // given
        Mockito.when(security.isTDPUser()).thenReturn(false);

        // when
        final List<ViewSettings> settings = provider.getSettings();

        // then
        assertNotNull(settings);
        assertEquals(5, settings.size());
        assertTrue(settings.contains(HomeViewsForNonTDPUsers.APP_HEADER_BAR_FOR_NON_TDP_USERS));
        assertTrue(settings.contains(HomeViewsForNonTDPUsers.SIDE_PANEL));
        assertTrue(settings.contains(ListViews.FOLDERS_LIST));
        assertTrue(settings.contains(ListViews.PREPARATIONS_LIST));
        assertTrue(settings.contains(ListViews.DATASETS_LIST));
    }
}
