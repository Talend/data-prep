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
import static org.talend.dataprep.transformation.actions.column.DomainChange.NEW_DOMAIN_ID_PARAMETER_KEY;
import static org.talend.dataprep.transformation.actions.column.DomainChange.NEW_DOMAIN_LABEL_PARAMETER_KEY;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class DomainChangeTest extends AbstractMetadataBaseTest<DomainChange> {

    private Map<String, String> parameters;

    private DataSetRow row;

    public DomainChangeTest() {
        super(new DomainChange());
    }

    @Before
    public void init() {
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "");
        values.put("0002", "Something");

        row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata columnMetadata = rowMetadata.getById("0002");
        columnMetadata.setType(Type.INTEGER.getName());
        columnMetadata.setDomain("FR_BEER");
        columnMetadata.setDomainFrequency(1);
        columnMetadata.setDomainLabel("French Beer");

        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");
        parameters.put(NEW_DOMAIN_ID_PARAMETER_KEY, "AUS_BEER");
        parameters.put(NEW_DOMAIN_LABEL_PARAMETER_KEY, "Australian Beer");
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
        assertThat(column.getDomain()).isEqualTo("AUS_BEER");
        assertThat(column.getDomainLabel()).isEqualTo("Australian Beer");
    }

    @Test
    public void should_set_column_in_forced_columns() throws Exception {
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

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
