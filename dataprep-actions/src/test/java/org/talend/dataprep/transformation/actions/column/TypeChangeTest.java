// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.column;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.column.TypeChange.NEW_TYPE_PARAMETER_KEY;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class TypeChangeTest extends AbstractMetadataBaseTest<TypeChange> {

    private Map<String, String> parameters;

    private DataSetRow row;

    public TypeChangeTest() {
        super(new TypeChange());
    }

    @Before
    public void init() {
        row = builder() //
                .with(value("David Bowie").type(Type.STRING)) //
                .with(value("").type(Type.INTEGER).domain("FR_BEER")) //
                .with(value("Something").type(Type.STRING)) //
                .build();

        parameters = new HashMap<>();
        parameters.put(NEW_TYPE_PARAMETER_KEY, "string");
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.INVISIBLE_DISABLED;
    }

    @Test
    public void test_apply_in_newcolumn() throws Exception {
        // Nothing to test, this action is always applied in place
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata column = row.getRowMetadata().getById("0002");
        assertThat(column.getType()).isEqualTo("string");
    }

    @Test
    public void should_reset_domain() throws Exception {
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final ColumnMetadata column = row.getRowMetadata().getById("0002");
        assertThat(column.getDomain()).isEqualTo("");
        assertThat(column.getDomainLabel()).isEqualTo("");
        assertThat(column.getDomainFrequency()).isEqualTo(0);
    }

    @Test
    public void should_add_column_to_force_columns() throws Exception {
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
            final boolean accepted = domainChange.acceptField(columnMetadata);

            // then
            assertThat(accepted).isTrue();
        }
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CHANGE_TYPE));
    }

}
