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

package org.talend.dataprep.transformation.api.action.metadata.column;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.talend.dataprep.transformation.api.action.metadata.column.DomainChange.NEW_DOMAIN_ID_PARAMETER_KEY;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;

public class DomainChangeTest {

    private DomainChange domainChange;

    private ColumnMetadata columnMetadata;
    private Map<String, String> parameters;

    @Before
    public void init() {
        domainChange = new DomainChange();
        columnMetadata = ColumnMetadata.Builder.column() //
                .type(Type.INTEGER) //
                .computedId("0002") //
                .domain("FR_BEER") //
                .domainFrequency(1) //
                .domainLabel("French Beer") //
                .build();
        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");
        parameters.put(NEW_DOMAIN_ID_PARAMETER_KEY, "AUS_BEER");
    }

    @Test
    public void should_change_domain() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(singletonList(columnMetadata));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        ActionTestWorkbench.test(row, domainChange.create(parameters).getRowAction());

        // then
        final ColumnMetadata column = row.getRowMetadata().getColumns().get(0);
        assertThat(column.getDomain()).isEqualTo("AUS_BEER");
    }

    @Test
    public void should_set_column_in_forced_columns() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(singletonList(columnMetadata));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        // when
        ActionTestWorkbench.test(row, domainChange.create(parameters).getRowAction());

        // then
        assertThat(row.getRowMetadata().getById("0002").isDomainForced()).isTrue();
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
