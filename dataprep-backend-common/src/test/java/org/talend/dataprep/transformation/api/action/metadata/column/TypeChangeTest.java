package org.talend.dataprep.transformation.api.action.metadata.column;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.talend.dataprep.transformation.api.action.metadata.column.TypeChange.NEW_TYPE_PARAMETER_KEY;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;

public class TypeChangeTest {

    private TypeChange typeChange;

    private TransformationContext transformationContext;

    private ColumnMetadata columnMetadata;

    @Before
    public void init() {
        typeChange = new TypeChange();
        transformationContext = new TransformationContext();
        columnMetadata = ColumnMetadata.Builder.column() //
                .type(Type.INTEGER) //
                .computedId("0002") //
                .domain("FR_BEER") //
                .domainFrequency(1) //
                .domainLabel("French Beer") //
                .build();
    }

    @Test
    public void should_change_type() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(singletonList(columnMetadata));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(NEW_TYPE_PARAMETER_KEY, "string");

        // when
        typeChange.applyOnColumn(row, transformationContext, parameters, "0002");

        // then
        final ColumnMetadata column = row.getRowMetadata().getColumns().get(0);
        assertThat(column.getType()).isEqualTo("string");
    }

    @Test
    public void should_reset_domain() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(singletonList(columnMetadata));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(NEW_TYPE_PARAMETER_KEY, "string");

        // when
        typeChange.applyOnColumn(row, transformationContext, parameters, "0002");

        // then
        final ColumnMetadata column = row.getRowMetadata().getColumns().get(0);
        assertThat(column.getDomain()).isEqualTo("");
        assertThat(column.getDomainLabel()).isEqualTo("");
        assertThat(column.getDomainFrequency()).isEqualTo(0);
    }

    @Test
    public void should_add_column_to_force_columns() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(singletonList(columnMetadata));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(NEW_TYPE_PARAMETER_KEY, "string");

        // when
        typeChange.applyOnColumn(row, transformationContext, parameters, "0002");

        // then
        assertThat(row.getRowMetadata().getById("0002").isTypeForced()).isTrue();
    }

    @Test
    public void should_not_accept_any_type_to_avoid_transformation_to_be_in_transfo_list() {
        // given
        final DomainChange domainChange = new DomainChange();
        for (final Type type : Type.values()) {

            final ColumnMetadata columnMetadata = ColumnMetadata.Builder.column() //
                    .type(type) //
                    .computedId("0002") //
                    .domain("FR_BEER") //
                    .domainFrequency(1) //
                    .domainLabel("French Beer") //
                    .build();

            // when
            final boolean accepted = domainChange.acceptColumn(columnMetadata);

            // then
            assertThat(accepted).isTrue();
        }
    }
}
