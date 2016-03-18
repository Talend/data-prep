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
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for LowerCase action. Creates one consumer, and test it.
 *
 * @see UpperCase
 */

public class UpperCaseTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private UpperCase action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(UpperCaseTest.class.getResourceAsStream("uppercase.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS.getDisplayName()));
    }

    @Test
    public void should_uppercase() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("city", "Vancouver");
        values.put("country", "Canada");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("city", "VANCOUVER"); // Vancouver --> VANCOUVER
        expectedValues.put("country", "Canada");

        //when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test()
    public void should_do_nothing_since_column_does_not_exist() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("country", "Canada");
        values.put("capital", "Ottawa");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("country", "Canada");
        expectedValues.put("capital", "Ottawa");

        //when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
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
