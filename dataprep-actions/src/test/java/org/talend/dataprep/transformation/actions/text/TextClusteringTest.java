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

package org.talend.dataprep.transformation.actions.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.util.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class TextClusteringTest extends AbstractMetadataBaseTest {

    private TextClustering textClustering = new TextClustering();

    @Test
    public void create_should_build_textclustering_consumer() {
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
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(textClustering, parameters));

        // then
        rows.stream().map(row -> row.get(columnId)).forEach(uglyState -> Assertions.assertThat(uglyState).isEqualTo("Tata"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(textClustering.getCategory(), is(ActionCategory.STRINGS_ADVANCED.getDisplayName()));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(textClustering.adapt((ColumnMetadata) null), is(textClustering));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(textClustering.adapt(column), is(textClustering));
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
        ActionTestWorkbench.test(rows, actionRegistry, factory.create(textClustering, parameters));

        // then
        rows.stream().map((row) -> row.get(columnId)).forEach(uglyState -> Assertions.assertThat(uglyState).isNotEqualTo("Tata"));
    }

    private DataSetRow createRow(final String key, final String value) {
        Map<String, String> values = Collections.singletonMap(key, value);
        return new DataSetRow(values);
    }

    @Test
    public void should_accept_column() {
        assertTrue(textClustering.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(textClustering.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(textClustering.acceptField(getColumn(Type.DOUBLE)));
        assertFalse(textClustering.acceptField(getColumn(Type.FLOAT)));
        assertFalse(textClustering.acceptField(getColumn(Type.INTEGER)));
        assertFalse(textClustering.acceptField(getColumn(Type.DATE)));
        assertFalse(textClustering.acceptField(getColumn(Type.BOOLEAN)));
    }
}
