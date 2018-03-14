package org.talend.dataprep.qa.util.folder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.helper.OSDataPrepAPIHelper;
import org.talend.dataprep.qa.dto.Folder;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { OSFolderUtil.class, OSDataPrepAPIHelper.class })
public class OSFolderUtilTest {

    Folder emptyPathF = new Folder().setPath("");

    Folder aPathF = new Folder().setPath("/a");

    Folder aaPathF = new Folder().setPath("/a/aa");

    Folder aaaPathF = new Folder().setPath("/a/aa/aaa");

    Folder abPathF = new Folder().setPath("/a/ab");

    Folder rootPathF = new Folder().setPath("/");

    List<Folder> emptyFList = new ArrayList<>();

    List<Folder> allFList = new ArrayList<>();

    @Autowired
    private OSFolderUtil folderUtil;

    {
        allFList.add(aPathF);
        allFList.add(aaPathF);
        allFList.add(aaaPathF);
        allFList.add(abPathF);
    }

    @Test
    public void splitFolderTest_EmptyF_EmptyFL() {
        Set<Folder> result = folderUtil.splitFolder(emptyPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_EmptyF_AllFL() {
        Set<Folder> result = folderUtil.splitFolder(emptyPathF, allFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_Root_EmptyFL() {
        Set<Folder> result = folderUtil.splitFolder(rootPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_Root_AllFL() {
        Set<Folder> result = folderUtil.splitFolder(rootPathF, allFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_AFolder_EmptyFL() {
        Set<Folder> result = folderUtil.splitFolder(aPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_AFolder_AllFL() {
        Set<Folder> result = folderUtil.splitFolder(aPathF, allFList);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains(aPathF));
    }

    @Test
    public void splitFolderTest_AaFolder_EmptyFL() {
        Set<Folder> result = folderUtil.splitFolder(aaPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_AaFolder_AllFL() {
        Set<Folder> result = folderUtil.splitFolder(aaPathF, allFList);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains(aPathF));
        Assert.assertTrue(result.contains(aaPathF));
    }

    @Test
    public void sortFolder_Empty() {
        Set<Folder> folders = new HashSet<>();
        SortedSet<Folder> result = folderUtil.sortFolders(folders);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void sortFolder_OneFolder() {
        Set<Folder> folders = new HashSet<>();
        folders.add(aPathF);
        SortedSet<Folder> result = folderUtil.sortFolders(folders);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(aPathF, result.first());
    }

    @Test
    public void sortFolder_MultipleFolderNaturalOrderInsert() {
        Set<Folder> folders = new HashSet<>();
        folders.add(emptyPathF);
        folders.add(rootPathF);
        folders.add(aPathF);
        folders.add(aaPathF);
        SortedSet<Folder> result = folderUtil.sortFolders(folders);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(aaPathF, result.last());
        Assert.assertEquals(emptyPathF, result.first());
    }

    @Test
    public void sortFolder_MultipleFolderInverseOrderInsert() {
        Set<Folder> folders = new HashSet<>();
        folders.add(aaPathF);
        folders.add(aPathF);
        folders.add(rootPathF);
        folders.add(emptyPathF);
        SortedSet<Folder> result = folderUtil.sortFolders(folders);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(aaPathF, result.last());
        Assert.assertEquals(emptyPathF, result.first());
    }

    @Test
    public void sortFolder_MultipleFolderChaoticOrderInsert() {
        Set<Folder> folders = new HashSet<>();
        folders.add(emptyPathF);
        folders.add(aPathF);
        folders.add(rootPathF);
        folders.add(aaPathF);
        SortedSet<Folder> result = folderUtil.sortFolders(folders);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(aaPathF, result.last());
        Assert.assertEquals(emptyPathF, result.first());
    }

    @Test
    public void getEmptyReverseSortedSet_Empty() {
        SortedSet<Folder> sortedFolders = folderUtil.getEmptyReverseSortedSet();
        Assert.assertNotNull(sortedFolders);
        Assert.assertEquals(0, sortedFolders.size());
    }

    @Test
    public void getEmptyReverseSortedSet_OneFolder() {
        SortedSet<Folder> sortedFolders = folderUtil.getEmptyReverseSortedSet();
        sortedFolders.add(aPathF);
        Assert.assertNotNull(sortedFolders);
        Assert.assertEquals(1, sortedFolders.size());
        Assert.assertEquals(aPathF, sortedFolders.first());
    }

    @Test
    public void getEmptyReverseSortedSet_MultipleFolderNaturalOrderInsert() {
        SortedSet<Folder> sortedFolders = folderUtil.getEmptyReverseSortedSet();
        sortedFolders.add(emptyPathF);
        sortedFolders.add(rootPathF);
        sortedFolders.add(aPathF);
        sortedFolders.add(aaPathF);
        Assert.assertNotNull(sortedFolders);
        Assert.assertEquals(4, sortedFolders.size());
        Assert.assertEquals(aaPathF, sortedFolders.first());
        Assert.assertEquals(emptyPathF, sortedFolders.last());
    }

    @Test
    public void getEmptyReverseSortedSet_MultipleFolderReverseOrderInsert() {
        SortedSet<Folder> sortedFolders = folderUtil.getEmptyReverseSortedSet();
        sortedFolders.add(aaPathF);
        sortedFolders.add(aPathF);
        sortedFolders.add(rootPathF);
        sortedFolders.add(emptyPathF);
        Assert.assertNotNull(sortedFolders);
        Assert.assertEquals(4, sortedFolders.size());
        Assert.assertEquals(aaPathF, sortedFolders.first());
        Assert.assertEquals(emptyPathF, sortedFolders.last());
    }

    @Test
    public void getEmptyReverseSortedSet_MultipleFolderChaoticOrderInsert() {
        SortedSet<Folder> sortedFolders = folderUtil.getEmptyReverseSortedSet();
        sortedFolders.add(rootPathF);
        sortedFolders.add(aaPathF);
        sortedFolders.add(aPathF);
        sortedFolders.add(emptyPathF);
        Assert.assertNotNull(sortedFolders);
        Assert.assertEquals(4, sortedFolders.size());
        Assert.assertEquals(aaPathF, sortedFolders.first());
        Assert.assertEquals(emptyPathF, sortedFolders.last());
    }
}
