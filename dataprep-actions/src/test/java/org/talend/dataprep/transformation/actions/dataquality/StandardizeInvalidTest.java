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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.category.ActionScope.HIDDEN_IN_ACTION_LIST;

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
public class StandardizeInvalidTest extends AbstractMetadataBaseTest {

    private final String MATCH_THRESHOLD_PARAMETER = "match_threshold";

    private final List<String> ACTION_SCOPE = Collections.singletonList(HIDDEN_IN_ACTION_LIST.getDisplayName());

    private final String fixedName = "David Bowie";

    private final String columnId0 = "0000";

    private final String columnId1 = "0001";

    /**
     * The action to test.
     */
    private StandardizeInvalid standardizeInvalid;

    private Map<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        standardizeInvalid = new StandardizeInvalid();
        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), columnId1);
        parameters.put(MATCH_THRESHOLD_PARAMETER, "DEFAULT");

    }

    @Test
    public void testGetParameters() throws Exception {
        final List<Parameter> parameters = standardizeInvalid.getParameters(Locale.US);
        assertEquals(5, parameters.size());
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(standardizeInvalid.adapt((ColumnMetadata) null), is(standardizeInvalid));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(standardizeInvalid.adapt(column), is(standardizeInvalid));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(standardizeInvalid.getCategory(Locale.US), is(ActionCategory.DATA_CLEANSING.getDisplayName(Locale.US)));
    }

    @Test
    public void should_standardize() {
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
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

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

        final Map<String, String> expectedValues = values;
        expectedValues.put("__tdpInvalid", columnId1);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_standardize_value_is_valid() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(columnId0, fixedName);
        values.put(columnId1, "Russie");

        // set semantic domain
        final DataSetRow row = createRow(values, null, "COUNTRY");

        final Map<String, String> expectedValues = values;

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());

    }

    @Test
    public void should_not_standardize_out_of_threshold() {
        // given
        parameters.put(MATCH_THRESHOLD_PARAMETER, "HIGH");
        final Map<String, String> values = new HashMap<>();
        values.put(columnId0, fixedName);
        values.put(columnId1, "Ferrand");

        final DataSetRow row = createRow(values, columnId1, "FR_COMMUNE");

        final Map<String, String> expectedValues = values;
        expectedValues.put("__tdpInvalid", columnId1);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_standardize_empty() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put(columnId0, "David Bowie");
        values.put(columnId1, "");

        // set semantic domain
        final DataSetRow row = createRow(values, columnId1, "COUNTRY");

        final Map<String, String> expectedValues = values;
        expectedValues.put("__tdpInvalid", columnId1);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_action_Scope() {
        assertTrue(standardizeInvalid.getActionScope().size() == 1);
        assertTrue(standardizeInvalid.getActionScope().equals(ACTION_SCOPE));
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
        assertTrue(standardizeInvalid.acceptField(column));
    }

    @Test
    public void should_not_accept_column() {
        // no semantic
        ColumnMetadata column = ColumnMetadata.Builder.column().id(0).name("name").type(Type.STRING).build();
        assertFalse(standardizeInvalid.acceptField(column));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, standardizeInvalid.getBehavior().size());
        assertTrue(standardizeInvalid.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
        assertTrue(standardizeInvalid.getBehavior().contains(ActionDefinition.Behavior.NEED_STATISTICS_INVALID));
    }

}
