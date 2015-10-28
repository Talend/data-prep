// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for ProperCase action. Creates one consumer, and test it.
 *
 * @see ProperCase
 */
public class ProperCaseTest {

    /** The action to test. */
    private ProperCase action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new ProperCase();

        parameters = ActionMetadataTestUtils.parseParameters( //
                //
                ProperCaseTest.class.getResourceAsStream("properCaseAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt(null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS.getDisplayName()));
    }

    @Test
    public void should_transform_lower_to_proper() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("band", "the beatles");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "band");

        //then
        assertEquals("The Beatles", row.get("band"));
    }

    @Test
    public void should_transform_upper_to_proper() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("band", "THE BEATLES");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "band");

        //then
        assertEquals("The Beatles", row.get("band"));
    }

    @Test
    public void should_not_change_other_columns() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("bando", "the beatles");
        final DataSetRow row = new DataSetRow(values);

        //when
        action.applyOnColumn(row, new TransformationContext(), parameters, "band");

        //then
        assertEquals("the beatles", row.get("bando"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }
}
