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

package org.talend.dataprep.transformation.actions.fill;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.setStatistics;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.date.ChangeDatePatternTest;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the FillWithStringIfEmpty action.
 *
 * @see FillIfEmpty
 */
public class FillWithDateIfEmptyTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    private FillIfEmpty action = new FillIfEmpty();

    @PostConstruct
    public void init() {
        action = (FillIfEmpty) action.adapt(ColumnMetadata.Builder.column().type(Type.DATE).build());
    }

    @Test
    public void test_adapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.DATE).build();
        assertThat(action.adapt(column), not(is(action)));
    }

    @Test
    public void should_fill_empty_date() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("David Bowie").type(Type.STRING)) //
                .with(value("").type(Type.DATE)) //
                .with(value("100").type(Type.STRING)) //
                .build();
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillEmptyDateAction.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("01/01/1970 10:00:00", row.get("0001"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_not_fill_empty_date() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "not empty");
        values.put("0002", "100");

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.DATE.getName());

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillEmptyDateAction.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("not empty", row.get("0001"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void test_TDP_591() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("David Bowie").type(Type.STRING)) //
                .with(value("").type(Type.DATE).statistics(ChangeDatePatternTest.class.getResourceAsStream("statistics_yyyy-MM-dd.json"))) //
                .with(value("100").type(Type.STRING)) //
                .build();
        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillEmptyDateAction.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("1970-01-01", row.get("0001"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_fill_empty_string_other_column() throws Exception {
        // given
        final DataSetRow row = builder() //
                .with(value("David Bowie").type(Type.STRING)) //
                .with(value("").type(Type.DATE).statistics(ChangeDatePatternTest.class.getResourceAsStream("statistics_yyyy-MM-dd.json"))) //
                .with(value("15/10/1999").type(Type.DATE)) //
                .build();
        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillEmptyIntegerAction.json"));

        // when
        parameters.put(FillIfEmpty.MODE_PARAMETER, FillIfEmpty.OTHER_COLUMN_MODE);
        parameters.put(FillIfEmpty.SELECTED_COLUMN_PARAMETER, "0002");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("15/10/1999", row.get("0002"));
        assertEquals("1999-10-15", row.get("0001"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_fill_empty_string_other_column_not_date() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "");
        values.put("0002", "tagada");

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.DATE.getName());
        rowMetadata.getById("0002").setType(Type.DATE.getName());
        setStatistics(row, "0001", ChangeDatePatternTest.class.getResourceAsStream("statistics_yyyy-MM-dd.json"));

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillEmptyIntegerAction.json"));

        // when
        parameters.put(FillIfEmpty.MODE_PARAMETER, FillIfEmpty.OTHER_COLUMN_MODE);
        parameters.put(FillIfEmpty.SELECTED_COLUMN_PARAMETER, "0002");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("tagada", row.get("0002"));
        assertEquals("tagada", row.get("0001"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

}
