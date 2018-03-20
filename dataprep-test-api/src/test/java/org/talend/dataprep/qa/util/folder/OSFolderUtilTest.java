package org.talend.dataprep.qa.util.folder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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

    private Folder emptyPathF = new Folder().setPath("");

    private Folder aPathF = new Folder().setPath("/a");

    private Folder aaPathF = new Folder().setPath("/a/aa");

    private Folder aaaPathF = new Folder().setPath("/a/aa/aaa");

    private Folder abPathF = new Folder().setPath("/a/ab");

    private Folder rootPathF = new Folder().setPath("/");

    private List<Folder> emptyFList = new ArrayList<>();

    private List<Folder> allFList = new ArrayList<>();

    @Autowired
    private OSFolderUtil folderUtil;

    @Before
    public void init(){
        allFList.add(aPathF);
        allFList.add(aaPathF);
        allFList.add(aaaPathF);
        allFList.add(abPathF);
    }

    @Test
    public void splitFolderTestEmptyFEmptyFL() {
        Set<Folder> result = folderUtil.splitFolder(emptyPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTestEmptyFAllFL() {
        Set<Folder> result = folderUtil.splitFolder(emptyPathF, allFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTestRootEmptyFL() {
        Set<Folder> result = folderUtil.splitFolder(rootPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTestRootAllFL() {
        Set<Folder> result = folderUtil.splitFolder(rootPathF, allFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTestAFolderEmptyFL() {
        Set<Folder> result = folderUtil.splitFolder(aPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTestAFolderAllFL() {
        Set<Folder> result = folderUtil.splitFolder(aPathF, allFList);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains(aPathF));
    }

    @Test
    public void splitFolderTestAaFolderEmptyFL() {
        Set<Folder> result = folderUtil.splitFolder(aaPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTestAaFolderAllFL() {
        Set<Folder> result = folderUtil.splitFolder(aaPathF, allFList);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains(aPathF));
        Assert.assertTrue(result.contains(aaPathF));
    }

    @Test
    public void sortFolderEmpty() {
        Set<Folder> folders = new HashSet<>();
        SortedSet<Folder> result = folderUtil.sortFolders(folders);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void sortFolderOneFolder() {
        Set<Folder> folders = new HashSet<>();
        folders.add(aPathF);
        SortedSet<Folder> result = folderUtil.sortFolders(folders);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(aPathF, result.first());
    }

    @Test
    public void sortFolderMultipleFolderNaturalOrderInsert() {
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
    public void sortFolderMultipleFolderInverseOrderInsert() {
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
    public void sortFolderMultipleFolderChaoticOrderInsert() {
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
    public void getEmptyReverseSortedSetEmpty() {
        SortedSet<Folder> sortedFolders = folderUtil.getEmptyReverseSortedSet();
        Assert.assertNotNull(sortedFolders);
        Assert.assertEquals(0, sortedFolders.size());
    }

    @Test
    public void getEmptyReverseSortedSetOneFolder() {
        SortedSet<Folder> sortedFolders = folderUtil.getEmptyReverseSortedSet();
        sortedFolders.add(aPathF);
        Assert.assertNotNull(sortedFolders);
        Assert.assertEquals(1, sortedFolders.size());
        Assert.assertEquals(aPathF, sortedFolders.first());
    }

    @Test
    public void getEmptyReverseSortedSetMultipleFolderNaturalOrderInsert() {
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
    public void getEmptyReverseSortedSetMultipleFolderReverseOrderInsert() {
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
    public void getEmptyReverseSortedSetMultipleFolderChaoticOrderInsert() {
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
