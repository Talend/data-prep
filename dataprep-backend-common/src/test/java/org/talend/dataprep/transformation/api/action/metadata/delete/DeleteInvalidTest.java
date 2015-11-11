package org.talend.dataprep.transformation.api.action.metadata.delete;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

/**
 * Test class for DeleteInvalid action. Creates one consumer, and test it.
 *
 * @see DeleteInvalid
 */
public class DeleteInvalidTest {

    /** The action to test. */
    private DeleteInvalid deleteInvalid;

    private Map<String, String> parameters;

    /**
     * Default constructor.
     */
    public DeleteInvalidTest() throws IOException {
        deleteInvalid = new DeleteInvalid();
        parameters = ActionMetadataTestUtils.parseParameters( //
                DeleteInvalidTest.class.getResourceAsStream("deleteInvalidAction.json"));
    }

    @Test
    public void testActionScope() throws Exception {
        assertThat(deleteInvalid.getActionScope(), hasItem("invalid"));
    }

    @Test
    public void should_delete_because_non_valid() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "N");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.STRING) //
                .computedId("0002") //
                .invalidValues(newHashSet("N")) //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        //when
        deleteInvalid.applyOnColumn( row, new TransformationContext(), parameters, "0002");

        //then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0001"));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-457
     */
    @Test
    public void should_delete_invalid_values_not_in_metadata() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "1");
        values.put("0002", "N"); // invalid value
        values.put("0003", "2");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.INTEGER) //
                .computedId("0002") //
                .invalidValues(new HashSet<>()) // no registered invalid values
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        deleteInvalid.applyOnColumn(row, new TransformationContext(), parameters, "0002");

        // then row is deleted...
        assertTrue(row.isDeleted());

        // ... and column metadata invalid values are also updated
        final Set<String> invalidValues = row.getRowMetadata().getById("0002").getQuality().getInvalidValues();
        assertEquals(1, invalidValues.size());
        assertTrue(invalidValues.contains("N"));
    }

    /**
     * see https://jira.talendforge.org/browse/TDP-457
     */
    @Test
    public void should_delete_invalid_semantic_value_not_in_metadata() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "CA");
        values.put("0002", "ZZ"); // invalid value
        values.put("0003", "NY");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.INTEGER) //
                .domain(SemanticCategoryEnum.US_STATE_CODE.getId())
                .computedId("0002") //
                .invalidValues(new HashSet<>()) // no registered invalid values
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        deleteInvalid.applyOnColumn(row, new TransformationContext(), parameters, "0002");

        // then row is deleted...
        assertTrue(row.isDeleted());

        // ... and column metadata invalid values are also updated
        final Set<String> invalidValues = row.getRowMetadata().getById("0002").getQuality().getInvalidValues();
        assertEquals(1, invalidValues.size());
        assertTrue(invalidValues.contains("ZZ"));
    }


    @Test
    public void should_accept_column() {
        for (Type type : Type.values()) {
            assertTrue(deleteInvalid.acceptColumn(getColumn(type)));
        }
    }

}
