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

package org.talend.dataprep.transformation.api.action.metadata.line;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.metadata.text.UpperCase;

public class MakeLineHeaderTest extends AbstractMetadataBaseTest {

    @Autowired
    private MakeLineHeader action;

    @Test
    public void should_be_in_data_cleansing_category() {
        // when
        final String name = action.getCategory();

        // then
        assertThat(name, is(DATA_CLEANSING.getDisplayName()));
    }

    @Test
    public void should_return_name() {
        // when
        final String name = action.getName();

        // then
        assertThat(name, is("make_line_header"));
    }

    @Test
    public void should_delete_line_with_provided_row_id() {
        // given
        Long rowId = 120L;

        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "Bowie");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(rowId);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "Lennon");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");
        parameters.put("row_id", row2.getTdpId().toString());

        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));

        //when
        ActionTestWorkbench.test(row2, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(true));

        assertEquals("John", row2.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row2.getRowMetadata().getById("0001").getName());
    }

    @Test
    public void should_keep_header_after_modifications() {
        // given
        Long rowId = 0L;

        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "Bowie");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(rowId++);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "Lennon");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        // row 3
        rowContent = new HashMap<>();
        rowContent.put("0000", "Johnny");
        rowContent.put("0001", "Lennon");
        final DataSetRow row3 = new DataSetRow(rowContent);
        row3.setTdpId(rowId++);

        // when
        final Map<String, String> makeHeaderParameters = new HashMap<>();
        makeHeaderParameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "line");
        makeHeaderParameters.put("row_id", row2.getTdpId().toString());
        final Action makeHeader = factory.create(action, makeHeaderParameters);
        final Map<String, String> upperCaseParameters = new HashMap<>();
        upperCaseParameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        upperCaseParameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        final Action upperCase = factory.create(new UpperCase(), upperCaseParameters);
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), makeHeader, upperCase);

        // then
        assertEquals("John", row1.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row1.getRowMetadata().getById("0001").getName());
        assertEquals("John", row2.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row2.getRowMetadata().getById("0001").getName());
        assertEquals("John", row3.getRowMetadata().getById("0000").getName());
        assertEquals("Lennon", row3.getRowMetadata().getById("0001").getName());
    }

}