package org.talend.dataprep.qa.config;

import static org.junit.Assert.*;
import static org.talend.dataprep.qa.config.FeatureContext.*;
import static org.talend.dataprep.qa.config.UnitTestsUtil.*;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.*;

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
