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
package org.talend.dataprep.transformation.actions.column;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see CopyColumnMetadata
 */
public class CopyColumnTest extends AbstractMetadataBaseTest<CopyColumnMetadata> {

    private Map<String, String> parameters;

    public CopyColumnTest() {
        super(new CopyColumnMetadata());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(CopyColumnTest.class.getResourceAsStream("copyColumnAction.json"));
    }

    @Override
    protected  CreateNewColumnPolicy getCreateNewColumnPolicy(){
        return CreateNewColumnPolicy.INVISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // Nothing to test, this action is never applied in place
    }

    @Test
    public void testAdapt() throws Exception {
        Assert.assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        Assert.assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        Assert.assertThat(action.getCategory(Locale.US), is(ActionCategory.COLUMN_METADATA.getDisplayName(Locale.US)));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_update_metadata() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last_update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expectedColumns = new ArrayList<>();
        expectedColumns.add(createMetadata("0000", "recipe"));
        expectedColumns.add(createMetadata("0001", "steps"));
        expectedColumns.add(createMetadata("0003", "steps_copy"));
        expectedColumns.add(createMetadata("0002", "last_update"));
        final RowMetadata expected = new RowMetadata(expectedColumns);

        // when
        final DataSetRow row = new DataSetRow(rowMetadata);
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row.getRowMetadata());
    }

    /**
     * Test with non-consecutive columns ids. This can occurs after some column deletion.
     */
    @Test
    public void test_TDP_1184() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(columnBaseBuilder().name("recipe").type(Type.STRING).build());
        input.add(columnBaseBuilder().name("steps").type(Type.STRING).build());
        input.add(columnBaseBuilder().name("last update").type(Type.STRING).build());
        input.add(columnBaseBuilder().name("column to delete").type(Type.STRING).build());
        final RowMetadata rowMetadata = new RowMetadata(input);

        // when
        rowMetadata.deleteColumnById("0003");
        final DataSetRow row = new DataSetRow(rowMetadata);
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final List<ColumnMetadata> actual = row.getRowMetadata().getColumns();
        assertEquals(actual.size(), 4);
        final ColumnMetadata copiedColumn = row.getRowMetadata().getById("0004");
        assertNotNull(copiedColumn);
        assertEquals("steps_copy", copiedColumn.getName());
    }

    @Test
    public void should_copy_statistics() throws Exception {
        // given
        final ColumnMetadata original = createMetadata("0001", "column");
        original.setStatistics(new Statistics());
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(original);
        final RowMetadata rowMetadata = new RowMetadata(input);

        final ColumnMetadata transformed = createMetadata("0002", "column");
        original.setStatistics(new Statistics());
        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0001", "column"));
        expected.add(transformed);

        // when
        ActionTestWorkbench.test(rowMetadata, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected.get(1).getStatistics(), original.getStatistics());
    }

    @Test
    public void should_copy_semantic() throws Exception {
        List<ColumnMetadata> input = new ArrayList<>();
        final ColumnMetadata original = createMetadata("0001", "column");
        original.setStatistics(new Statistics());

        SemanticDomain semanticDomain = new SemanticDomain("mountain_goat", "Mountain goat pale pale", 1);

        original.setDomain("beer");
        original.setDomainFrequency(1);
        original.setDomainLabel("the best beer");
        original.setSemanticDomains(Collections.singletonList(semanticDomain));

        input.add(original);
        RowMetadata rowMetadata = new RowMetadata(input);

        assertThat(rowMetadata.getColumns()).isNotNull().isNotEmpty().hasSize(1);

        final DataSetRow row = new DataSetRow(rowMetadata);
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        List<ColumnMetadata> actual = row.getRowMetadata().getColumns();

        assertThat(actual).isNotNull().isNotEmpty().hasSize(2);

        assertEquals(actual.get(1).getStatistics(), original.getStatistics());

        assertThat(actual.get(1)) //
                .isEqualToComparingOnlyGivenFields(original, "domain", "domainLabel", "domainFrequency");

        assertThat(actual.get(1).getSemanticDomains()).isNotNull() //
                .isNotEmpty().contains(semanticDomain);
    }

    @Test
    public void test_TDP_567_with_force_true() throws Exception {
        List<ColumnMetadata> input = new ArrayList<>();
        final ColumnMetadata original = createMetadata("0001", "column");
        original.setStatistics(new Statistics());

        SemanticDomain semanticDomain = new SemanticDomain("mountain_goat", "Mountain goat pale pale", 1);

        original.setDomain("beer");
        original.setDomainFrequency(1);
        original.setDomainLabel("the best beer");
        original.setDomainForced(true);
        original.setTypeForced(true);
        original.setSemanticDomains(Collections.singletonList(semanticDomain));

        input.add(original);
        RowMetadata rowMetadata = new RowMetadata(input);

        assertThat(rowMetadata.getColumns()).isNotNull().isNotEmpty().hasSize(1);

        final DataSetRow row = new DataSetRow(rowMetadata);
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        List<ColumnMetadata> actual = row.getRowMetadata().getColumns();

        assertThat(actual).isNotNull().isNotEmpty().hasSize(2);

        assertEquals(actual.get(1).getStatistics(), original.getStatistics());

        assertThat(actual.get(1)) //
                .isEqualToComparingOnlyGivenFields(original, "domain", "domainLabel", "domainFrequency", "domainForced",
                        "typeForced");

        assertThat(actual.get(1).getSemanticDomains()).isNotNull() //
                .isNotEmpty().contains(semanticDomain);
    }

    @Test
    public void test_TDP_567_with_force_false() throws Exception {
        List<ColumnMetadata> input = new ArrayList<>();
        final ColumnMetadata original = createMetadata("0001", "column");
        original.setStatistics(new Statistics());

        SemanticDomain semanticDomain = new SemanticDomain("mountain_goat", "Mountain goat pale pale", 1);

        original.setDomain("beer");
        original.setDomainFrequency(1);
        original.setDomainLabel("the best beer");
        original.setDomainForced(false);
        original.setTypeForced(false);
        original.setSemanticDomains(Collections.singletonList(semanticDomain));

        input.add(original);
        RowMetadata rowMetadata = new RowMetadata(input);

        assertThat(rowMetadata.getColumns()).isNotNull().isNotEmpty().hasSize(1);

        final DataSetRow row = new DataSetRow(rowMetadata);
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        List<ColumnMetadata> actual = row.getRowMetadata().getColumns();

        assertThat(actual).isNotNull().isNotEmpty().hasSize(2);

        assertEquals(actual.get(1).getStatistics(), original.getStatistics());

        assertThat(actual.get(1)) //
                .isEqualToComparingOnlyGivenFields(original, "domain", "domainLabel", "domainFrequency", "domainForced",
                        "typeForced");

        assertThat(actual.get(1).getSemanticDomains()).isNotNull() //
                .isNotEmpty().contains(semanticDomain);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.ANY)));
    }

    @Override
    protected ColumnMetadata.Builder columnBaseBuilder() {
        return super.columnBaseBuilder().headerSize(12).valid(5).invalid(2);
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_COPY_COLUMNS));
    }

}
