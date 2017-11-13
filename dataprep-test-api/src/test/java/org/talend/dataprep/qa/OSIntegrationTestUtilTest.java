package org.talend.dataprep.qa;

import static org.talend.dataprep.helper.api.ActionFilterEnum.END;
import static org.talend.dataprep.helper.api.ActionFilterEnum.FIELD;
import static org.talend.dataprep.helper.api.ActionFilterEnum.LABEL;
import static org.talend.dataprep.helper.api.ActionFilterEnum.START;
import static org.talend.dataprep.helper.api.ActionFilterEnum.TYPE;
import static org.talend.dataprep.helper.api.ActionParamEnum.COLUMN_ID;
import static org.talend.dataprep.helper.api.ActionParamEnum.COLUMN_NAME;
import static org.talend.dataprep.helper.api.ActionParamEnum.FILTER;
import static org.talend.dataprep.helper.api.ActionParamEnum.ROW_ID;
import static org.talend.dataprep.qa.OSIntegrationTestUtil.ACTION_NAME;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    @Test
    public void mapParamsToFilter_Empty() {
        Filter result = util.mapParamsToFilter(new HashMap<String, String>());
        Assert.assertNull(result);
    }

    @Test
    public void mapParamsToFilter_NoFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNull(result);
    }

    @Test
    public void mapParamsToFilter_OneStringFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put(LABEL.getName(), "label");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.range.size(), 1);
        Assert.assertEquals(result.range.get(LABEL), "label");
    }

    @Test
    public void mapParamsToFilter_VariousStringFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put(LABEL.getName(), "label");
        map.put(FIELD.getName(), "field");
        map.put(TYPE.getName(), "type");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.range.size(), 3);
        Assert.assertEquals(result.range.get(LABEL), "label");
        Assert.assertEquals(result.range.get(FIELD), "field");
        Assert.assertEquals(result.range.get(TYPE), "type");
    }

    @Test
    public void mapParamsToFilter_OneIntegerFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put(START.getName(), "15");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.range.size(), 1);
        Assert.assertEquals(result.range.get(START), 15);
    }

    @Test
    public void mapParamsToFilter_VariousIntegerFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put(START.getName(), "50000");
        map.put(END.getName(), "60000");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.range.size(), 2);
        Assert.assertEquals(result.range.get(START), 50000);
        Assert.assertEquals(result.range.get(END), 60000);
    }

    @Test
    public void mapParamsToFilter_VariousMixedFilters() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put(LABEL.getName(), "label");
        map.put(START.getName(), "50000");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.range.size(), 2);
        Assert.assertEquals(result.range.get(START), 50000);
        Assert.assertEquals(result.range.get(LABEL), "label");
    }

    @Test
    public void mapParamsToAction_Empty() {
        Action action = new Action();
        Action result = util.mapParamsToAction(new HashMap<String, String>(), action);
        Assert.assertEquals(action, result);
    }

    @Test
    public void mapParamsToAction_NoKnownParam() {
        Action action = new Action();
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        Action result = util.mapParamsToAction(map, action);
        Assert.assertEquals(action, result);
    }

    @Test
    public void mapParamsToAction_NoActionNameParam() {
        Action action = new Action();
        action.action = "testName";
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        Action result = util.mapParamsToAction(map, action);
        Assert.assertEquals(action, result);
        Assert.assertEquals(result.action, "testName");
    }

    @Test
    public void mapParamsToAction_WithActionNameParam() {
        Action action = new Action();
        action.action = "testName";
        Map<String, String> map = new HashMap<>();
        map.put(ACTION_NAME, "newName");
        Action result = util.mapParamsToAction(map, action);
        Assert.assertEquals(action, result);
        Assert.assertEquals(result.action, "newName");
    }

    @Test
    public void mapParamsToAction_FullParam() {
        Action action = new Action();
        action.action = "testName";
        Map<String, String> map = new HashMap<>();
        map.put(ACTION_NAME, "newName");
        map.put(COLUMN_ID.getName(), "0000");
        map.put(COLUMN_NAME.getName(), "id");
        map.put(LABEL.getName(), "label");
        map.put(START.getName(), "50000");
        map.put(TYPE.getName(), "type");
        map.put(ROW_ID.getName(), "");
        Action result = util.mapParamsToAction(map, action);
        Assert.assertNotNull(result);
        Assert.assertEquals(action.action, "newName");
        Assert.assertEquals(action.parameters.get(COLUMN_ID), "0000");
        Assert.assertEquals(action.parameters.get(COLUMN_NAME), "id");
        Assert.assertEquals(action.parameters.get(ROW_ID), null);
        Filter filter = (Filter) action.parameters.get(FILTER);
        Assert.assertEquals(filter.range.get(START), 50000);
        Assert.assertEquals(filter.range.get(TYPE), "type");
        Assert.assertEquals(filter.range.get(LABEL), "label");
    }

}
