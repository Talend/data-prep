package org.talend.dataprep.qa.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.qa.config.FeatureContext.getSuffix;
import static org.talend.dataprep.qa.config.FeatureContext.isUseSuffix;
import static org.talend.dataprep.qa.config.FeatureContext.setUseSuffix;
import static org.talend.dataprep.qa.config.FeatureContext.suffixFolderName;
import static org.talend.dataprep.qa.config.FeatureContext.suffixName;
import static org.talend.dataprep.qa.config.UnitTestsUtil.injectFieldInClass;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeatureContextTest {

    private FeatureContext context = new FeatureContext();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        injectFieldInClass(context, "TI_SUFFIX_UID", "_123456789");
    }

    @Test
    public void testGetSuffix() {
        assertEquals(getSuffix(), "_123456789");
    }

    @Test
    public void testIsUseSuffixTrueByDefault() {
        assertTrue(isUseSuffix());
    }

    @Test
    public void testSuffixName() {
        assertEquals(suffixName(""), "_123456789");
        assertEquals(suffixName("toto"), "toto_123456789");
    }

    @Test
    public void testSuffixNameDeactivated() {
        setUseSuffix(false);
        assertEquals(suffixName(""), "");
        assertEquals(suffixName("toto"), "toto");
        setUseSuffix(true);
        assertEquals(suffixName(""), "_123456789");
        assertEquals(suffixName("toto"), "toto_123456789");
    }

    @Test
    public void testSuffixFolderName() {
        assertEquals("", suffixFolderName(""));
        assertEquals("/", suffixFolderName("/"));
        assertEquals("/folderA_123456789", suffixFolderName("/folderA"));
        assertEquals("/folderA_123456789/", suffixFolderName("/folderA/"));
        assertEquals("/folderA_123456789/folderB_123456789", suffixFolderName("/folderA/folderB"));
        assertEquals("/folderA_123456789/folderB_123456789/", suffixFolderName("/folderA/folderB/"));
    }

    @Test
    public void testSuffixFolderNameDeactivated() {
        setUseSuffix(false);
        assertEquals("", suffixFolderName(""));
        assertEquals("/", suffixFolderName("/"));
        assertEquals("/folderA", suffixFolderName("/folderA"));
        assertEquals("/folderA/", suffixFolderName("/folderA/"));
        assertEquals("/folderA/folderB", suffixFolderName("/folderA/folderB"));
        assertEquals("/folderA/folderB/", suffixFolderName("/folderA/folderB/"));
        setUseSuffix(true);
        assertEquals("", suffixFolderName(""));
        assertEquals("/", suffixFolderName("/"));
        assertEquals("/folderA_123456789", suffixFolderName("/folderA"));
        assertEquals("/folderA_123456789/", suffixFolderName("/folderA/"));
        assertEquals("/folderA_123456789/folderB_123456789", suffixFolderName("/folderA/folderB"));
        assertEquals("/folderA_123456789/folderB_123456789/", suffixFolderName("/folderA/folderB/"));
    }

}
