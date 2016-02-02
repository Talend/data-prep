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

package org.talend.dataprep.transformation.api.action.metadata.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.column.TypeChange;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;

public class ChangeTypeTest {

    @Test
    public void should_change_type() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.INTEGER) //
                .computedId("0002") //
                .domain("FR_BEER") //
                .domainFrequency(1) //
                .domainLabel("French Beer") //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        TypeChange typeChange = new TypeChange();

        Map<String, String> parameters = new HashMap<>();
        parameters.put(TypeChange.NEW_TYPE_PARAMETER_KEY, "STRING");
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");

        // when
        ActionTestWorkbench.test(row, typeChange.create(parameters).getRowAction());

        // then
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getDomain()).isEmpty();
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getDomainLabel()).isEmpty();
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).getDomainFrequency()).isEqualTo(0);

        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).isTypeForced()).isTrue();

        // Check for TDP-838:
        Assertions.assertThat(row.getRowMetadata().getColumns().get(0).isDomainForced()).isTrue();
    }

    @Test
    public void should_not_apply() {
        TypeChange typeChange = new TypeChange();

        for (Type type : Type.values()) {

            ColumnMetadata columnMetadata = ColumnMetadata.Builder.column() //
                    .type(type) //
                    .computedId("0002") //
                    .domain("FR_BEER") //
                    .domainFrequency(1) //
                    .domainLabel("French Beer") //
                    .build();

            Assertions.assertThat(typeChange.acceptColumn(columnMetadata)).isTrue();
        }
    }
}
