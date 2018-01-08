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

package org.talend.dataprep.maintenance.cache;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.talend.dataprep.maintenance.BaseMaintenanceTest;

public class ScheduledCacheJanitorTest extends BaseMaintenanceTest {

    @InjectMocks
    private ScheduledCacheJanitor scheduledCacheJanitor;

    @Test
    public void shouldInvokeJanitor() throws Exception {
        // given
        when(forAll.condition()).thenReturn(bean -> () -> true);

        // when
        scheduledCacheJanitor.scheduledJanitor();

        // then
        verify(forAll, times(1)).execute(any());
    }
}
