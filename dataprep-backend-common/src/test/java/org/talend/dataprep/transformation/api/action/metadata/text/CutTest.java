//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.metadata.common.ReplaceOnValueHelper;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Unit test for the Cut action.
 *
 * @see Cut
 */
public class CutTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private Cut action;

    /** The dataprep ready jackson builder. */
    @Autowired
    public Jackson2ObjectMapperBuilder builder;
    
    /** The action parameters. */
    private Map<String, String> parameters;

    /**
     * Constructor.
     */
    public CutTest() throws IOException {
        final InputStream parametersSource = SplitTest.class.getResourceAsStream("cutAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS.getDisplayName()));
    }

    @Test
    public void should_apply_on_column(){
        // given
        DataSetRow row = getRow("Wait for it...", "The value that gets cut !", "Done !");
        DataSetRow expected = getRow("Wait for it...", "value that gets cut !", "Done !");

        // when
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_on_column_starts_with() throws IOException {
        // given
        DataSetRow row = getRow("Wait for it...", "The value that gets cut !", "Done !");
        DataSetRow expected = getRow("Wait for it...", " value that gets cut !", "Done !");

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters(
                SplitTest.class.getResourceAsStream("cutAction.json"));
        regexpParameters.put("pattern", generateJson("The", "starts_with"));

        // when
        ActionTestWorkbench.test(row, action.create(regexpParameters).getRowAction());

        // then
        assertEquals(expected, row);
    }


    @Test
    public void should_apply_on_column_ends_with() throws IOException {
        // given
        DataSetRow row = getRow("Wait for it...", "The value that gets cut !", "Done !");
        DataSetRow expected = getRow("Wait for it...", "The value that gets ", "Done !");

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters(
                SplitTest.class.getResourceAsStream("cutAction.json"));
        regexpParameters.put("pattern", generateJson("cut !", "ends_with"));

        // when
        ActionTestWorkbench.test(row, action.create(regexpParameters).getRowAction());

        // then
        assertEquals(expected, row);
    }

    /**
     * Test with an invalid regex pattern as token and mode is not REGEX.
     */
    @Test
    public void should_apply_on_column_contains_and_invalid_regex() throws IOException {
        // given
        DataSetRow row = getRow("Wait for it...", "The value that (gets cut !", "Done !");
        DataSetRow expected = getRow("Wait for it...", "The value that gets cut !", "Done !");

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters(
                SplitTest.class.getResourceAsStream("cutAction.json"));
        regexpParameters.put("pattern", generateJson("(", "contains"));

        // when
        ActionTestWorkbench.test(row, action.create(regexpParameters).getRowAction());

        // then
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_on_column_with_regexp() throws IOException {
        // given
        DataSetRow row = getRow("Wait for it...", "The value that gets cut !", "Done !");
        DataSetRow expected = getRow("Wait for it...", " cut !", "Done !");

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters(
                SplitTest.class.getResourceAsStream("cutAction.json"));
        regexpParameters.put("pattern", generateJson(".*gets", "regex"));

        // when
        ActionTestWorkbench.test(row, action.create(regexpParameters).getRowAction());

        // then
        assertEquals(expected, row);
    }

    @Test
    public void test_TDP_663() throws IOException {
        // given
        DataSetRow row = getRow("Wait for it...", "The value that gets cut !", "Done !");
        DataSetRow expected = getRow("Wait for it...", "The value that gets cut !", "Done !");

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters(
                SplitTest.class.getResourceAsStream("cutAction.json"));
        regexpParameters.put("pattern", generateJson("*", "regex"));

        // when
        ActionTestWorkbench.test(row, action.create(regexpParameters).getRowAction());

        // then
        assertEquals(expected, row);
    }


    @Test
    public void test_TDP_958() throws IOException {
        // given
        DataSetRow row = getRow("Wait for it...", "The value that gets cut !", "Done !");
        DataSetRow expected = getRow("Wait for it...", "The value that gets cut !", "Done !");

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters(
                SplitTest.class.getResourceAsStream("cutAction.json"));
        regexpParameters.put("pattern", generateJson("", "regex"));

        // when
        ActionTestWorkbench.test(row, action.create(regexpParameters).getRowAction());

        // then
        assertEquals(expected, row);
    }

    @Test
    public void should_not_apply_on_column(){
        // given
        DataSetRow row = getRow("Wait for it...", "The value that gets cut !", "Done !");
        DataSetRow expected = getRow("Wait for it...", "The value that gets cut !", "Done !");

        // when (apply on a column that does not exists)
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0010");
        ActionTestWorkbench.test(row, action.create(parameters).getRowAction());

        // then (row should not be changed)
        assertEquals(expected, row);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.DOUBLE)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.INTEGER)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

}
