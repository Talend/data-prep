package org.talend.dataprep.qa.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.talend.dataprep.helper.api.ActionFilterEnum.END;
import static org.talend.dataprep.helper.api.ActionFilterEnum.FIELD;
import static org.talend.dataprep.helper.api.ActionFilterEnum.LABEL;
import static org.talend.dataprep.helper.api.ActionFilterEnum.START;
import static org.talend.dataprep.helper.api.ActionFilterEnum.TYPE;
import static org.talend.dataprep.helper.api.ActionParamEnum.COLUMN_ID;
import static org.talend.dataprep.helper.api.ActionParamEnum.FILTER;
import static org.talend.dataprep.helper.api.ActionParamEnum.ROW_ID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.talend.dataprep.helper.api.Action;
import org.talend.dataprep.helper.api.Filter;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { OSIntegrationTestUtil.class })
public class OSIntegrationTestUtilTest {

    @Autowired
    OSIntegrationTestUtil util;

    @Test
    public void mapParamsToFilterEmpty() {
        Filter result = util.mapParamsToFilter(new HashMap<>());
        Assert.assertNull(result);
    }

    @Test
    public void mapParamsToFilterNoFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNull(result);
    }

    @Test
    public void mapParamsToFilterOneStringFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put(LABEL.getName(), "label");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        assertEquals(result.range.size(), 1);
        assertEquals(result.range.get(LABEL), "label");
    }

    @Test
    public void mapParamsToFilterVariousStringFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put(LABEL.getName(), "label");
        map.put(FIELD.getName(), "field");
        map.put(TYPE.getName(), "type");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        assertEquals(result.range.size(), 3);
        assertEquals(result.range.get(LABEL), "label");
        assertEquals(result.range.get(FIELD), "field");
        assertEquals(result.range.get(TYPE), "type");
    }

    @Test
    public void mapParamsToFilterOneIntegerFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put(START.getName(), "15");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        assertEquals(result.range.size(), 1);
        assertEquals(result.range.get(START), 15);
    }

    @Test
    public void mapParamsToFilterVariousIntegerFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put(START.getName(), "50000");
        map.put(END.getName(), "60000");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        assertEquals(result.range.size(), 2);
        assertEquals(result.range.get(START), 50000);
        assertEquals(result.range.get(END), 60000);
    }

    @Test
    public void mapParamsToFilterVariousMixedFilters() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put(LABEL.getName(), "label");
        map.put(START.getName(), "50000");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        assertEquals(result.range.size(), 2);
        assertEquals(result.range.get(START), 50000);
        assertEquals(result.range.get(LABEL), "label");
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
    public void mapParamsToActionFullParam() {
        Map<String, String> map = new HashMap<>();
        map.put(COLUMN_ID.getName(), "0000");
        map.put("column_name", "id");
        map.put(LABEL.getName(), "label");
        map.put(START.getName(), "50000");
        map.put(TYPE.getName(), "type");

        Map<String, Object> parameters = util.mapParamsToActionParameters(map);

        assertEquals("0000", parameters.get(COLUMN_ID.getName()));
        assertEquals("id", parameters.get("column_name"));
        assertEquals(null, parameters.get(ROW_ID.getName()));

        Filter filter = (Filter) parameters.get(FILTER.getName());
        assertEquals(50000, filter.range.get(START));
        assertEquals("type", filter.range.get(TYPE));
        assertEquals("label", filter.range.get(LABEL));
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
        assertEquals("/simplePath", result);
    }

    @Test
    public void extractPathFromFullNameLongPath() {
        String result = util.extractPathFromFullName("/long/path/name");
        Assert.assertNotNull(result);
        assertEquals("/long/path", result);
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
