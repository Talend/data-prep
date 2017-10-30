package org.talend.dataprep.qa;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { OSIntegrationTestUtil.class })
public class OSIntegrationTestUtilTest {

    @Autowired
    OSIntegrationTestUtil util;

    @Test
    public void splitFolderTest_Empty() {
        Set<String> result = util.splitFolder("");
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_Root() {
        Set<String> result = util.splitFolder("/");
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_OneFolder() {
        Set<String> result = util.splitFolder("/folder");
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.contains("folder"));
    }

    @Test
    public void splitFolderTest_OneSubFolder() {
        Set<String> result = util.splitFolder("/folder/subFolder");
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains("folder"));
        Assert.assertTrue(result.contains("folder/subFolder"));
    }

}
