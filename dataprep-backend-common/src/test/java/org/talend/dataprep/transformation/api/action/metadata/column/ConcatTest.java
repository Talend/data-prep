package org.talend.dataprep.transformation.api.action.metadata.column;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Unit test for the Concat action.
 * 
 * @see Concat
 */
public class ConcatTest {

    /** The action to test. */
    private Concat action;

    /** The action parameters. */
    private Map<String, String> parameters;

    /**
     * Constructor.
     */
    public ConcatTest() throws IOException {
        action = new Concat();
    }

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = ConcatTest.class.getResourceAsStream("concatAction.json");
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
        assertThat(action.getCategory(), is(ActionCategory.COLUMNS.getDisplayName()));
    }

    @Test
    public void should_apply_on_column_with_full_parameter() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "<first-second>");
        assertEquals(expected, row);
    }

    @Test
    public void should_set_new_column_name() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        row.getRowMetadata().getById("0000").setName("source");
        row.getRowMetadata().getById("0001").setName("selected");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("source_selected").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void should_set_new_column_name_without_other_column() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        row.getRowMetadata().getById("0000").setName("source");
        row.getRowMetadata().getById("0001").setName("selected");

        parameters.put(Concat.OTHER_COLUMN_PARAMETER, Concat.CONCAT_WITH_CONSTANT);
        parameters.remove(Concat.SELECTED_COLUMN_PARAMETER);

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");

        // then
        final ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("<source>").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

    @Test
    public void should_apply_without_separator() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.SEPARATOR_PARAMETER);

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "<firstsecond>");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_without_prefix() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.PREFIX_PARAMETER);

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "first-second>");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_without_other_column() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(Concat.OTHER_COLUMN_PARAMETER, Concat.CONCAT_WITH_CONSTANT);

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "<first>");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_without_suffix() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.SUFFIX_PARAMETER);

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "<first-second");
        assertEquals(expected, row);
    }

    @Test
    public void should_apply_without_any_parameters() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.PREFIX_PARAMETER);
        parameters.remove(Concat.SEPARATOR_PARAMETER);
        parameters.remove(Concat.SUFFIX_PARAMETER);

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");

        // then
        DataSetRow expected = getRow("first", "second", "Done !", "firstsecond");
        assertEquals(expected, row);
    }

    @Test(expected = TDPException.class)
    public void should_not_apply_because_missing_selected_parameter() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.remove(Concat.SELECTED_COLUMN_PARAMETER);

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");
    }

    @Test(expected = TDPException.class)
    public void should_not_apply_because_selected_column_not_found() {
        // given
        DataSetRow row = getRow("first", "second", "Done !");
        parameters.put(Concat.SELECTED_COLUMN_PARAMETER, "123548");

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0000");
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptColumn(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptColumn(getColumn(Type.FLOAT)));
        assertTrue(action.acceptColumn(getColumn(Type.INTEGER)));
        assertTrue(action.acceptColumn(getColumn(Type.DATE)));
        assertTrue(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

}