//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.actions.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.util.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class TextClusteringTest extends AbstractMetadataBaseTest<TextClustering> {

    public TextClusteringTest() {
        super(new TextClustering());
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.INVISIBLE_DISABLED;
    }

    @Test
    public void test_apply_in_newcolumn() throws Exception {
        // Nothing to test, this action is never applied in place
    }

    @Test
    public void test_apply_inplace() {
        // given
        final String columnId = "0000";

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("column_id", columnId);
        parameters.put("T@T@", "Tata");
        parameters.put("TaaTa", "Tata");
        parameters.put("Toto", "Tata");

        final List<DataSetRow> rows = new ArrayList<>();
        rows.add(createRow(columnId, "T@T@"));
        rows.add(createRow(columnId, "TaaTa"));
        rows.add(createRow(columnId, "Toto"));
        rows.add(createRow(columnId, "Tata"));

        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        rows.stream().map(row -> row.get(columnId)).forEach(uglyState -> Assertions.assertThat(uglyState).isEqualTo("Tata"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.STRINGS_ADVANCED.getDisplayName(Locale.US)));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void create_result_should_not_change_unmatched_value() {
        // given
        final String columnId = "0001";

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("column_id", columnId);
        parameters.put("T@T@", "Tata");
        parameters.put("TaaTa", "Tata");
        parameters.put("Toto", "Tata");

        final List<DataSetRow> rows = new ArrayList<>();
        rows.add(createRow(columnId, "T@T@1"));
        rows.add(createRow(columnId, "TaaTa1"));
        rows.add(createRow(columnId, "Toto1"));
        rows.add(createRow(columnId, "Tata1"));

        // when
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(action, parameters));

        // then
        rows.stream().map((row) -> row.get(columnId)).forEach(uglyState -> Assertions.assertThat(uglyState).isNotEqualTo("Tata"));
    }

    private DataSetRow createRow(final String key, final String value) {
        Map<String, String> values = Collections.singletonMap(key, value);
        return new DataSetRow(values);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.DOUBLE)));
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.INTEGER)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

}
