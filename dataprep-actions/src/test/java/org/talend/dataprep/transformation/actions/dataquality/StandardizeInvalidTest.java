// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.actions.dataquality;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.category.ActionScope.INVALID;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

/**
 * Test class for StandardizeInvalid action
 *
 * @see StandardizeInvalid
 */
public class StandardizeInvalidTest extends AbstractMetadataBaseTest<StandardizeInvalid> {

    private final String MATCH_THRESHOLD_PARAMETER = "match_threshold";
    
    private final String fixedName = "David Bowie";

    private final String columnId0 = "0000";

    private final String columnId1 = "0001";

    private Map<String, String> parameters;

    public StandardizeInvalidTest() {
        super(new StandardizeInvalid());
    }

    @Before
    public void setUp() throws Exception {
        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), columnId1);
        parameters.put(MATCH_THRESHOLD_PARAMETER, "DEFAULT");

    }

    @Test
    public void testGetParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        assertEquals(5, parameters.size());
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.DATA_CLEANSING.getDisplayName(Locale.US)));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.INVISIBLE_DISABLED;
    }

    @Override
    public void test_apply_in_newcolumn() {
        // Nothing to do, always in place
    }

    @Test
    public void test_apply_inplace() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(columnId0, fixedName);
        values.put(columnId1, "Russian Federatio");

        final DataSetRow row = createRow(values, columnId1, "COUNTRY");

        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put(columnId0, fixedName);
        expectedValues.put(columnId1, "Russian Federation");
        expectedValues.put("__tdpInvalid", columnId1);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_standardize_no_domain() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(columnId0, fixedName);
        values.put(columnId1, "Russian Federatio");

        final DataSetRow row = createRow(values, columnId1, "");

        values.put("__tdpInvalid", columnId1);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void should_not_standardize_value_is_valid() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(columnId0, fixedName);
        values.put(columnId1, "Russie");

        // set semantic domain
        final DataSetRow row = createRow(values, null, "COUNTRY");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());

    }

    @Test
    public void should_not_standardize_out_of_threshold() {
        // given
        parameters.put(MATCH_THRESHOLD_PARAMETER, "HIGH");
        final Map<String, String> values = new HashMap<>();
        values.put(columnId0, fixedName);
        values.put(columnId1, "Ferrand");

        final DataSetRow row = createRow(values, columnId1, "FR_COMMUNE");

        values.put("__tdpInvalid", columnId1);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void should_not_standardize_empty() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(columnId0, "David Bowie");
        values.put(columnId1, "");

        // set semantic domain
        final DataSetRow row = createRow(values, columnId1, "COUNTRY");

        values.put("__tdpInvalid", columnId1);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void testActionScope() throws Exception {
        assertThat(action.getActionScope(), hasItem("invalid"));
    }

    private DataSetRow createRow(Map<String, String> inputValues, String invalidColumnId, String domain) {
        DataSetRow row = new DataSetRow(inputValues);
        if (!StringUtils.isEmpty(invalidColumnId)) {
            row.setInvalid(invalidColumnId);
        }
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata columnMetadata = rowMetadata.getById(columnId1);
        columnMetadata.setDomain(domain);
        columnMetadata.setType(Type.STRING.getName());
        return row;
    }

    @Test
    public void should_accept_column() {
        // a column with semantic
        SemanticCategoryEnum semantic = SemanticCategoryEnum.COUNTRY;
        List<SemanticDomain> semanticDomainLs = new ArrayList<>();
        semanticDomainLs.add(new SemanticDomain("COUNTRY", "Country", 0.85f));
        ColumnMetadata column = ColumnMetadata.Builder
                .column()
                .id(0)
                .name("name")
                .type(Type.STRING)
                .semanticDomains(semanticDomainLs)
                .domain(semantic.name())
                .build();
        assertTrue(action.acceptField(column));
    }

    @Test
    public void should_not_accept_column() {
        // no semantic
        ColumnMetadata column = ColumnMetadata.Builder.column().id(0).name("name").type(Type.STRING).build();
        assertFalse(action.acceptField(column));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.NEED_STATISTICS_INVALID));
    }

}
