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
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Unit test for the Cut action.
 *
 * @see Cut
 */
public class CutTest {

    /** The action to test. */
    private Cut action;
    /** The action parameters. */
    private Map<String, String> parameters;

    /**
     * Constructor.
     */
    public CutTest() throws IOException {
        action = new Cut();
        final InputStream parametersSource = SplitTest.class.getResourceAsStream("cutAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
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
        action.applyOnColumn(row, new TransformationContext(), parameters, "0001");

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
        regexpParameters.put("pattern", ".*gets");

        // when
        action.applyOnColumn(row, new TransformationContext(), regexpParameters, "0001");

        // then
        assertEquals(expected, row);
    }

    @Test
    public void test_TTP_663() throws IOException {
        // given
        DataSetRow row = getRow("Wait for it...", "TSG-12345", "Done !");
        DataSetRow expected = getRow("Wait for it...", "TSG-12345", "Done !");

        Map<String, String> regexpParameters = ActionMetadataTestUtils.parseParameters(
                SplitTest.class.getResourceAsStream("cutAction.json"));
        regexpParameters.put("pattern", "*");

        // when
        action.applyOnColumn(row, new TransformationContext(), regexpParameters, "0001");

        // then
        assertEquals(expected, row);
    }

    @Test
    public void should_not_apply_on_column(){
        // given
        DataSetRow row = getRow("Wait for it...", "The value that gets cut !", "Done !");
        DataSetRow expected = getRow("Wait for it...", "The value that gets cut !", "Done !");

        // when (apply on a column that does not exists)
        action.applyOnColumn(row, new TransformationContext(), parameters, "0010");

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
