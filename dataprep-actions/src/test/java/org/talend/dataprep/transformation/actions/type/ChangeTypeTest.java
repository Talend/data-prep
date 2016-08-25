// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.column.TypeChange;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class ChangeTypeTest extends AbstractMetadataBaseTest {

    private TypeChange typeChange = new TypeChange();

    @Test
    public void should_change_type() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("David Bowie").type(Type.STRING)) //
                .with(value("").type(Type.INTEGER).domain("FR_BEER")) //
                .with(value("Something").type(Type.STRING)) //
                .build();

        Map<String, String> parameters = new HashMap<>();
        parameters.put(TypeChange.NEW_TYPE_PARAMETER_KEY, "STRING");
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(typeChange, parameters));

        // then
        final ColumnMetadata columnMetadata = row.getRowMetadata().getById("0001");
        assertThat(columnMetadata.getDomain()).isEmpty();
        assertThat(columnMetadata.getDomainLabel()).isEmpty();
        assertThat(columnMetadata.getDomainFrequency()).isEqualTo(0);

        assertThat(columnMetadata.isTypeForced()).isTrue();

        // Check for TDP-838:
        assertThat(columnMetadata.isDomainForced()).isTrue();
    }

    @Test
    public void should_not_apply() {

        for (Type type : Type.values()) {

            ColumnMetadata columnMetadata = ColumnMetadata.Builder.column() //
                    .type(type) //
                    .computedId("0002") //
                    .domain("FR_BEER") //
                    .domainFrequency(1) //
                    .domainLabel("French Beer") //
                    .build();

            assertThat(typeChange.acceptField(columnMetadata)).isTrue();
        }
    }
}
