package org.talend.dataprep.qa.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.talend.dataprep.qa.dto.Folder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.talend.dataprep.helper.api.ActionFilterEnum.END;
import static org.talend.dataprep.helper.api.ActionFilterEnum.FIELD;
import static org.talend.dataprep.helper.api.ActionFilterEnum.LABEL;
import static org.talend.dataprep.helper.api.ActionFilterEnum.START;
import static org.talend.dataprep.helper.api.ActionFilterEnum.TYPE;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.COLUMN_ID;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.FILTER;
import static org.talend.dataprep.transformation.actions.common.ImplicitParameters.ROW_ID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { OSIntegrationTestUtil.class })
public class OSIntegrationTestUtilTest {

    @Autowired
    OSIntegrationTestUtil util;

    Folder emptyPathF = new Folder().setPath("");

    Folder aPathF = new Folder().setPath("/a");

    Folder aaPathF = new Folder().setPath("/a/aa");

    Folder aaaPathF = new Folder().setPath("/a/aa/aaa");

    Folder abPathF = new Folder().setPath("/a/ab");

    Folder rootPathF = new Folder().setPath("/");

    List<Folder> emptyFList = new ArrayList<>();

    List<Folder> allFList = new ArrayList<>();
    {
        allFList.add(aPathF);
        allFList.add(aaPathF);
        allFList.add(aaaPathF);
        allFList.add(abPathF);
    }

    @Test
    public void splitFolderTest_EmptyF_EmptyFL() {
        Set<Folder> result = util.splitFolder(emptyPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_EmptyF_AllFL() {
        Set<Folder> result = util.splitFolder(emptyPathF, allFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_Root_EmptyFL() {
        Set<Folder> result = util.splitFolder(rootPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_Root_AllFL() {
        Set<Folder> result = util.splitFolder(rootPathF, allFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_AFolder_EmptyFL() {
        Set<Folder> result = util.splitFolder(aPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_AFolder_AllFL() {
        Set<Folder> result = util.splitFolder(aPathF, allFList);
        assertEquals(1, result.size());
        Assert.assertTrue(result.contains(aPathF));
    }

    @Test
    public void splitFolderTest_AaFolder_EmptyFL() {
        Set<Folder> result = util.splitFolder(aaPathF, emptyFList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void splitFolderTest_AaFolder_AllFL() {
        Set<Folder> result = util.splitFolder(aaPathF, allFList);
        assertEquals(2, result.size());
        Assert.assertTrue(result.contains(aPathF));
        Assert.assertTrue(result.contains(aaPathF));
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
        assertEquals(result.range.size(), 1);
        assertEquals(result.range.get(LABEL), "label");
    }

    @Test
    public void mapParamsToFilter_VariousStringFilterParam() {
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
    public void mapParamsToFilter_OneIntegerFilterParam() {
        Map<String, String> map = new HashMap<>();
        map.put(START.getName(), "15");
        Filter result = util.mapParamsToFilter(map);
        Assert.assertNotNull(result);
        assertEquals(result.range.size(), 1);
        assertEquals(result.range.get(START), 15);
    }

    @Test
    public void mapParamsToFilter_VariousIntegerFilterParam() {
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
    public void mapParamsToFilter_VariousMixedFilters() {
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
    public void mapParamsToAction_Empty() {
        Action action = new Action();
        action.parameters = util.mapParamsToActionParameters(new HashMap<>());
        assertEquals(null, action.id);
        assertEquals(null, action.action);

        assertEquals("column", action.parameters.get("scope"));
    }

    @Test
    public void mapParametersToAction_shouldBeSuffixed() {
        Map<String, String> parameters = Collections.singletonMap("new_domain_id", "toto");

        Map<String, Object> actionParameters = util.mapParamsToActionParameters(parameters);

        assertNotEquals("toto", actionParameters.get("new_domain_id"));
    }

    @Test
    public void mapParamsToAction_FullParam() {
        Map<String, String> map = new HashMap<>();
        map.put(COLUMN_ID.getKey(), "0000");
        map.put("column_name", "id");
        map.put(LABEL.getName(), "label");
        map.put(START.getName(), "50000");
        map.put(TYPE.getName(), "type");

        Map<String, Object> parameters = util.mapParamsToActionParameters(map);

        assertEquals("0000", parameters.get(COLUMN_ID.getKey()));
        assertEquals("id", parameters.get("column_name"));
        assertEquals(null, parameters.get(ROW_ID.getKey()));

        Filter filter = (Filter) parameters.get(FILTER.getKey());
        assertEquals(50000, filter.range.get(START));
        assertEquals("type", filter.range.get(TYPE));
        assertEquals("label", filter.range.get(LABEL));
    }

    @Test
    public void getFilenameExtension_Empty() {
        assertEquals(util.getFilenameExtension("myFile"), "myFile");
    }

    @Test
    public void getFilenameExtension_csv1() {
        assertEquals(util.getFilenameExtension("myFile.csv"), "csv");
    }

    @Test
    public void getFilenameExtension_csv2() {
        assertEquals(util.getFilenameExtension("my.file.csv"), "csv");
    }

    @Test
    public void getFilenameExtension_xlsx() {
        assertEquals(util.getFilenameExtension("myFile.csv"), "csv");
    }
}
