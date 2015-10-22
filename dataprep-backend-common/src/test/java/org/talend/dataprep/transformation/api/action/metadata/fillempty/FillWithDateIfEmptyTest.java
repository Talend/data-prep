package org.talend.dataprep.transformation.api.action.metadata.fillempty;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.setStatistics;

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
import org.talend.dataprep.transformation.api.action.metadata.date.ChangeDatePatternTest;

/**
 * Unit test for the FillWithStringIfEmpty action.
 *
 * @see FillIfEmpty
 */
public class FillWithDateIfEmptyTest {

    /** The action to test. */
    private FillIfEmpty action;

    /**
     * Default empty constructor.
     */
    public FillWithDateIfEmptyTest() {
        action = new FillIfEmpty();
        action = (FillIfEmpty) action.adapt(ColumnMetadata.Builder.column().type(Type.DATE).build());
    }

    @Test
    public void should_fill_empty_date() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "100");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.DATE) //
                .computedId("0002") //
                .build()));

        final DataSetRow row = new DataSetRow(values);
        row.setRowMetadata(rowMetadata);

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters(action, //
                this.getClass().getResourceAsStream("fillEmptyDateAction.json"));

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0002");

        // then
        Assert.assertEquals("1/1/1970 10:0:0", row.get("0002"));
        Assert.assertEquals("David Bowie", row.get("0001"));
    }

    @Test
    public void test_TDP_591() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "100");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.DATE) //
                .computedId("0002") //
                .build()));

        final DataSetRow row = new DataSetRow(values);
        row.setRowMetadata(rowMetadata);
        setStatistics(row, "0002", ChangeDatePatternTest.class.getResourceAsStream("statistics_yyyy-MM-dd.json"));

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters(action, //
                this.getClass().getResourceAsStream("fillEmptyDateAction.json"));

        // when
        action.applyOnColumn(row, new TransformationContext(), parameters, "0002");

        // then
        Assert.assertEquals("1970-01-01", row.get("0002"));
        Assert.assertEquals("David Bowie", row.get("0001"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.DOUBLE)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.ANY)));
    }

}