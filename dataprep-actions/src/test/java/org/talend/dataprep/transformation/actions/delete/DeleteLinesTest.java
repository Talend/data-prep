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

package org.talend.dataprep.transformation.actions.delete;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class DeleteLinesTest extends AbstractMetadataBaseTest {

    private DeleteLines action = new DeleteLines();

    private Map<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        parameters = ActionMetadataTestUtils.parseParameters( //
                DeleteLinesTest.class.getResourceAsStream("deleteLines.json"));
    }

    @Test
    public void should_accept_column() {
        final List<Type> allTypes = Type.ANY.list();
        for (Type type : allTypes) {
            assertTrue(action.acceptField(getColumn(type)));
        }
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.FILTERED.getDisplayName()));
    }

    @Test
    public void should_delete() {
        // given
        final DataSetRow row = getRow("David Bowie", "Berlin");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertTrue(row.isDeleted());
    }

    @Test
    public void should_not_delete() {
        // given
        final DataSetRow row = getRow("David Bowie", "Paris");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row.isDeleted());
    }
}
