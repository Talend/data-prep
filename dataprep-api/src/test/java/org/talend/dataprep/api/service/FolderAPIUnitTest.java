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
import static org.mockito.Matchers.anyObject;
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
import org.talend.dataprep.api.service.command.folder.FolderChildrenList;
import org.talend.dataprep.command.preparation.PreparationListByFolder;
import org.talend.dataprep.util.SortAndOrderHelper;
import org.talend.logging.audit.StandardEventAuditLogger;

@RunWith(MockitoJUnitRunner.class)
public class FolderAPIUnitTest {

    @Mock
    private ApplicationContext context;

    @Mock
    private StandardEventAuditLogger auditLogger;

    @InjectMocks
    private FolderAPI folderAPI;

    @Test
    @Ignore("TODO Target another method than loginSuccess(), maybe accessPreparationsList(), to be created")
    public void testListPreparationsShouldLogAuditEventOnSuccess() {
        // given
        FolderChildrenList commandListFolders = mock(FolderChildrenList.class);
        when(context.getBean(eq(FolderChildrenList.class), anyObject(), anyObject(), anyObject()))
                .thenReturn(commandListFolders);
        when(commandListFolders.execute()).thenReturn(mock(InputStream.class));

        PreparationListByFolder commandListPreparations = mock(PreparationListByFolder.class);
        when(context.getBean(eq(PreparationListByFolder.class), anyObject(), anyObject(), anyObject()))
                .thenReturn(commandListPreparations);
        when(commandListPreparations.execute()).thenReturn(mock(InputStream.class));

        // when
        folderAPI.listPreparationsByFolder("Lw==", SortAndOrderHelper.Sort.NAME, SortAndOrderHelper.Order.ASC);

        // then
        verify(auditLogger, times(1)).loginSuccess(anyObject());
    }

    @Test
    // @Ignore("TODO Target another method than loginSuccess(), maybe accessPreparationsList(), to be created")
    public void testListPreparationsShouldNotLogAuditEventOnFailure() {
        // given
        FolderChildrenList commandListFolders = mock(FolderChildrenList.class);
        when(context.getBean(eq(FolderChildrenList.class), anyObject(), anyObject(), anyObject()))
                .thenReturn(commandListFolders);
        when(commandListFolders.execute()).thenReturn(mock(InputStream.class));

        PreparationListByFolder commandListPreparations = mock(PreparationListByFolder.class);
        when(context.getBean(eq(PreparationListByFolder.class), anyObject(), anyObject(), anyObject()))
                .thenReturn(commandListPreparations);
        when(commandListPreparations.execute()).thenThrow(new RuntimeException("on-purpose thrown exception"));

        // when
        folderAPI.listPreparationsByFolder("Lw==", SortAndOrderHelper.Sort.NAME, SortAndOrderHelper.Order.ASC);

        // then
        verify(auditLogger, never()).loginSuccess();
        fail();
    }
}
