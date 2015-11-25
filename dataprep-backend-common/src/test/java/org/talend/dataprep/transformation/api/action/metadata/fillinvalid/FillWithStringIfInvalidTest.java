package org.talend.dataprep.transformation.api.action.metadata.fillinvalid;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.fill.FillIfEmpty;
import org.talend.dataprep.transformation.api.action.metadata.fill.FillInvalid;

/**
 * Unit test for the FillWithStringIfInvalid action.
 * 
 * @see FillInvalid
 */
public class FillWithStringIfInvalidTest {

    /** The action to test. */
    private FillInvalid action;

    /**
     * Default empty constructor.
     */
    public FillWithStringIfInvalidTest() {
        action = new FillInvalid();
        action = (FillInvalid) action.adapt(ColumnMetadata.Builder.column().type(Type.STRING).build());
    }

    @Test
    public void should_fill_non_valid_string() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "N");
        values.put("0003", "100");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.STRING) //
                .computedId("0003") //
                .invalidValues(newHashSet("100")) //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidStringAction.json"));

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0003");

        // then
        assertEquals("beer", row.get("0003"));
        assertEquals("David Bowie", row.get("0001"));
    }

    @Test
    public void should_fill_empty_string_other_column() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "DC");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(ColumnMetadata.Builder.column().type(Type.STRING).computedId("0002").invalidValues(newHashSet("DC")).build());
        rowMetadata.addColumn(ColumnMetadata.Builder.column().type(Type.STRING).computedId("0003").build());

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillInvalidStringAction.json"));

        // when
        parameters.put(FillIfEmpty.MODE_PARAMETER, FillIfEmpty.COLUMN_MODE);
        parameters.put(FillIfEmpty.SELECTED_COLUMN_PARAMETER, "0003");
        action.applyOnColumn(row, new TransformationContext(), parameters, "0002");

        // then
        Assert.assertEquals("Something", row.get("0002"));
        Assert.assertEquals("David Bowie", row.get("0001"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_adapt_null() throws Exception {
        assertThat(action.adapt(null), is(action));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.ANY)));
    }
}