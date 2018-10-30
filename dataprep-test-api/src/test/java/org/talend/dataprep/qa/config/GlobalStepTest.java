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
package org.talend.dataprep.qa.config;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.talend.dataprep.helper.OSDataPrepAPIHelper;
import org.talend.dataprep.qa.dto.Folder;
import org.talend.dataprep.qa.util.folder.FolderUtil;

import com.jayway.restassured.response.Response;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class GlobalStepTest {

    @InjectMocks
    private GlobalStep gobalstep;

    @Mock
    private FeatureContext context;

    @Mock
    private OSDataPrepAPIHelper api;

    @Mock
    private FolderUtil folderUtil;

    @Mock
    private Response respOK;

    @Mock
    private Response respOKEmpty;

    @Mock
    private Response respNotFound;

    private Folder folderOK1 = new Folder().setId("A_OK");

    private Folder folderOK2 = new Folder().setId("B_OK");

    private Folder folderOK3 = new Folder().setId("C_OK");

    private Folder folderNotFound = new Folder().setId("B_NotFound");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        when(respOK.getStatusCode()).thenReturn(200);
        when(respOKEmpty.getStatusCode()).thenReturn(204);
        when(respNotFound.getStatusCode()).thenReturn(404);

        when(api.deletePreparation(endsWith("_OK"))).thenReturn(respOK);
        when(api.deletePreparation(endsWith("_NotFound"))).thenReturn(respNotFound);

        when(api.deleteDataset(endsWith("_OK"))).thenReturn(respOK);
        when(api.deleteDataset(endsWith("_NotFound"))).thenReturn(respNotFound);

        when(folderUtil.deleteFolder(folderOK1)).thenReturn(respOKEmpty);
        when(folderUtil.deleteFolder(folderOK2)).thenReturn(respOKEmpty);
        when(folderUtil.deleteFolder(folderOK3)).thenReturn(respOKEmpty);
        when(folderUtil.deleteFolder(folderNotFound)).thenReturn(respNotFound);
    }

    // should clean context OK : no exception is thrown
    @Test
    public void shouldCleanContextIsOK() {

        // when
        when(context.getPreparationIdsToDelete()).thenReturn(getOKList());
        when(context.getDatasetIdsToDelete()).thenReturn(getOKList());
        when(context.getFolders()).thenReturn(getOKFolderSet());

        gobalstep.context = this.context;
        boolean isExceptionThrown = true;

        // then
        try {
            gobalstep.cleanAfter();
            isExceptionThrown = false;
        } finally {
            Assert.assertFalse(isExceptionThrown);
        }
    }

    @Test
    public void shouldCleanContextWithDatasetError() throws Exception {

        // when
        when(context.getPreparationIdsToDelete()).thenReturn(getOKList());
        when(context.getDatasetIdsToDelete()).thenReturn(getNotFoundList());
        when(context.getFolders()).thenReturn(getOKFolderSet());

        gobalstep.context = this.context;

        // then
        thrown.expect(GlobalStep.CleanAfterException.class);
        gobalstep.cleanAfter();
    }

    @Test
    public void shouldCleanContextWithPreparationError() throws Exception {

        // when
        when(context.getPreparationIdsToDelete()).thenReturn(getNotFoundList());
        when(context.getDatasetIdsToDelete()).thenReturn(getOKList());
        when(context.getFolders()).thenReturn(getOKFolderSet());

        gobalstep.context = this.context;

        // then
        thrown.expect(GlobalStep.CleanAfterException.class);
        gobalstep.cleanAfter();
    }

    @Test
    public void shouldCleanContextWithFolderError() throws Exception {

        // when
        when(context.getPreparationIdsToDelete()).thenReturn(getOKList());
        when(context.getDatasetIdsToDelete()).thenReturn(getOKList());
        when(context.getFolders()).thenReturn(getNotFoundFolderSet());

        gobalstep.context = this.context;

        // then
        thrown.expect(GlobalStep.CleanAfterException.class);
        gobalstep.cleanAfter();
    }

    public List<String> getOKList() {
        List<String> listOK = new ArrayList<>();
        listOK.add("A_OK");
        listOK.add("B_OK");
        listOK.add("C_OK");
        return listOK;
    }

    public List<String> getNotFoundList() {
        List<String> listNotFound = new ArrayList<>();
        listNotFound.add("A_OK");
        listNotFound.add("B_NotFound");
        listNotFound.add("C_OK");
        return listNotFound;
    }

    public Set<Folder> getOKFolderSet() {
        Set<Folder> folderSetOK = new HashSet<>();
        folderSetOK.add(folderOK1);
        folderSetOK.add(folderOK2);
        folderSetOK.add(folderOK3);
        return folderSetOK;
    }

    public Set<Folder> getNotFoundFolderSet() {
        Set<Folder> folderSetNotFound = new HashSet<>();
        folderSetNotFound.add(folderOK1);
        folderSetNotFound.add(folderNotFound);
        folderSetNotFound.add(folderOK2);
        return folderSetNotFound;
    }
}
