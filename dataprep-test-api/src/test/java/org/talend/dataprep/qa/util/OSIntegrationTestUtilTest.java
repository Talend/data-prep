package org.talend.dataprep.qa.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.qa.config.FeatureContext;
import org.talend.dataprep.qa.config.UnitTestsUtil;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = { OSIntegrationTestUtil.class })
public class OSIntegrationTestUtilTest {

    @Mock
    private FeatureContext featureContext;

    @InjectMocks
    private OSIntegrationTestUtil util;

    @Before
    public void setUp() throws Exception {
        UnitTestsUtil.injectFieldInClass(featureContext, "TI_SUFFIX_UID", "_TI_SUFFIX_UID");
    }

    @Test
    public void mapParamsToActionEmpty() {
        Action action = new Action();
        action.parameters = util.mapParamsToActionParameters(new HashMap<>());
        assertEquals(null, action.id);
        assertEquals(null, action.action);

        assertEquals("column", action.parameters.get("scope"));
    }

    @Test
    public void mapParametersToActionShouldBeSuffixed() {
        Map<String, String> parameters = Collections.singletonMap("new_domain_id", "toto");

        Map<String, Object> actionParameters = util.mapParamsToActionParameters(parameters);

        assertNotEquals("toto", actionParameters.get("new_domain_id"));
    }

    @Test
    public void getFilenameExtensionEmpty() {
        assertEquals(util.getFilenameExtension("myFile"), "myFile");
    }

    @Test
    public void getFilenameExtensionCsv1() {
        assertEquals(util.getFilenameExtension("myFile.csv"), "csv");
    }

    @Test
    public void getFilenameExtensionCsv2() {
        assertEquals(util.getFilenameExtension("my.file.csv"), "csv");
    }

    @Test
    public void getFilenameExtensionXlsx() {
        assertEquals(util.getFilenameExtension("myFile.csv"), "csv");
    }

    @Test
    public void extractPathFromFullNameEmpty() {
        String result = util.extractPathFromFullName("");
        Assert.assertNotNull(result);
        assertEquals("/", result);
    }

    @Test
    public void extractPathFromFullNameSimpleName() {
        String result = util.extractPathFromFullName("simpleName");
        Assert.assertNotNull(result);
        assertEquals("/", result);
    }

    @Test
    public void extractPathFromFullNameRootPath() {
        String result = util.extractPathFromFullName("/simpleName");
        Assert.assertNotNull(result);
        assertEquals("/", result);
    }

    @Test
    public void extractPathFromFullNameSimplePath() {
        String result = util.extractPathFromFullName("/simplePath/name");
        Assert.assertNotNull(result);
        assertEquals("/simplePath_TI_SUFFIX_UID", result);
    }

    @Test
    public void extractPathFromFullNameLongPath() {
        String result = util.extractPathFromFullName("/long/path/name");
        Assert.assertNotNull(result);
        assertEquals("/long_TI_SUFFIX_UID/path_TI_SUFFIX_UID", result);
    }

    @Test
    public void extractNameFromFullNameEmpty() {
        String result = util.extractNameFromFullName("");
        Assert.assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    public void extractNameFromFullNameSimpleName() {
        String result = util.extractNameFromFullName("simpleName");
        Assert.assertNotNull(result);
        assertEquals("simpleName", result);
    }

    // Should never append
    @Test
    public void extractNameFromFullNameRootPath() {
        String result = util.extractNameFromFullName("/");
        Assert.assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    public void extractNameFromFullNameSimplePath() {
        String result = util.extractNameFromFullName("/simplePath/name");
        Assert.assertNotNull(result);
        assertEquals("name", result);
    }

    @Test
    public void extractNameFromFullNameLongPath() {
        String result = util.extractNameFromFullName("/long/path/name");
        Assert.assertNotNull(result);
        assertEquals("name", result);
    }
}
