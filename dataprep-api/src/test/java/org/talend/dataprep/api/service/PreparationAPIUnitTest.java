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

package org.talend.dataprep.api.service;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.talend.dataprep.api.service.command.preparation.PreparationList;
import org.talend.logging.audit.StandardEventAuditLogger;

@RunWith(MockitoJUnitRunner.class)
public class PreparationAPIUnitTest {

    @Mock
    private ApplicationContext context;

    @Mock
    private StandardEventAuditLogger auditLogger;

    @InjectMocks
    private PreparationAPI preparationAPI;

    @Test
    @Ignore("TODO Target another method than loginSuccess(), maybe accessPreparationsList(), to be created")
    public void testListPreparationsShouldLogAuditEventOnSuccess() {
        // given
        PreparationList command = mock(PreparationList.class);
        when(context.getBean(eq(PreparationList.class))).thenReturn(command);
        when(command.execute()).thenReturn(mock(InputStream.class));

        // when
        preparationAPI.listPreparations(null, null, null, "prep name", null, null);

        // then
        verify(auditLogger, times(1)).loginSuccess();
    }

    @Test
    @Ignore("TODO Target another method than loginSuccess(), maybe accessPreparationsList(), to be created")
    public void testListPreparationsShouldNotLogAuditEventOnFailure() {
        // given
        PreparationList command = mock(PreparationList.class);
        when(context.getBean(eq(PreparationList.class))).thenReturn(command);
        when(command.execute()).thenThrow(new RuntimeException("on-purpose thrown exception"));

        // when
        preparationAPI.listPreparations(null, null, null, "prep name", null, null);

        // then
        verify(auditLogger, never()).loginSuccess();
        fail();
    }

}
