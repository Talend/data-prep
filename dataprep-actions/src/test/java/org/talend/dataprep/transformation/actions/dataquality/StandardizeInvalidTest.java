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
package org.talend.dataprep.transformation.actions.dataquality;

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
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.fill.GenerateSequence;
import org.talend.dataprep.transformation.actions.text.LowerCase;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

/**
 * Test class for StandardizeInvalid action
 *
 * @see StandardizeInvalid
 */
public class StandardizeInvalidTest extends AbstractMetadataBaseTest {

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
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0001");
        parameters.put(StandardizeInvalid.MATCH_THRESHOLD_PARAMETER, StandardizeInvalid.MatchThresholdEnum.DEFAULT.name());

    }

    @Test
    public void testGetParameters() throws Exception {
        final List<Parameter> parameters = standardizeInvalid.getParameters();
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
        assertThat(standardizeInvalid.getCategory(), is(ActionCategory.DATA_CLEANSING.getDisplayName()));
    }

    @Test
    public void should_standardize() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "Russian Federatio");

        final DataSetRow row = new DataSetRow(values);
        row.setInvalid("0001");
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setDomain("COUNTRY");
        rowMetadata.getById("0001").setType(Type.STRING.getName());
        List<SemanticDomain> semanticDomains = new ArrayList<SemanticDomain>();
        semanticDomains.add(new SemanticDomain("COUNTRY", "Country", 0.85f));
        rowMetadata.getById("0001").setSemanticDomains(semanticDomains);

        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "David Bowie");
        expectedValues.put("0001", "Russian Federation");
        expectedValues.put("__tdpInvalid", "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_standardize_no_semantic() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "Russian Federatio");

        final DataSetRow row = new DataSetRow(values);
        row.setInvalid("0001");
        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "David Bowie");
        expectedValues.put("0001", "Russian Federatio");
        expectedValues.put("__tdpInvalid", "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_standardize_no_domain() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "Russian Federatio");

        final DataSetRow row = new DataSetRow(values);
        row.setInvalid("0001");
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.STRING.getName());

        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "David Bowie");
        expectedValues.put("0001", "Russian Federatio");
        expectedValues.put("__tdpInvalid", "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_standardize_value_is_valid() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "Russie");

        //set semantic domain
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        List<SemanticDomain> semanticDomains = new ArrayList<SemanticDomain>();
        semanticDomains.add(new SemanticDomain("COUNTRY", "Country", 0.85f));
        rowMetadata.getById("0001").setSemanticDomains(semanticDomains);
        rowMetadata.getById("0001").setType(Type.STRING.getName());

        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "David Bowie");
        expectedValues.put("0001", "Russie");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());

    }

    @Test
    public void should_not_standardize_out_of_threshold() {
        // given
        parameters.put(StandardizeInvalid.MATCH_THRESHOLD_PARAMETER, StandardizeInvalid.MatchThresholdEnum.HIGH.name());
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "Ferrand");

        final DataSetRow row = new DataSetRow(values);
        row.setInvalid("0001");
        //set semantic domain
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setDomain("FR_COMMUNE");
        List<SemanticDomain> semanticDomains = new ArrayList<SemanticDomain>();
        semanticDomains.add(new SemanticDomain("FR_COMMUNE", "Fr_Commune", 0.85f));
        rowMetadata.getById("0001").setSemanticDomains(semanticDomains);
        rowMetadata.getById("0001").setType(Type.STRING.getName());

        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "David Bowie");
        expectedValues.put("0001", "Ferrand");
        expectedValues.put("__tdpInvalid", "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_standardize_empty() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "");

        //set semantic domain
        final DataSetRow row = new DataSetRow(values);
        row.setInvalid("0001");
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setDomain("COUNTRY");
        rowMetadata.getById("0001").setType(Type.STRING.getName());
        List<SemanticDomain> semanticDomains = new ArrayList<SemanticDomain>();
        semanticDomains.add(new SemanticDomain("COUNTRY", "Country", 0.85f));
        rowMetadata.getById("0001").setSemanticDomains(semanticDomains);

        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "David Bowie");
        expectedValues.put("0001", "");
        expectedValues.put("__tdpInvalid", "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(standardizeInvalid, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_action_Scope() {
        assertTrue(standardizeInvalid.getActionScope().size() == 1);
        assertTrue(standardizeInvalid.getActionScope().equals(standardizeInvalid.ACTION_SCOPE));
    }

    @Test
    public void should_accept_column() {
        //a column with semantic
        SemanticCategoryEnum semantic = SemanticCategoryEnum.COUNTRY;
        List semanticDomainLs = new ArrayList();
        semanticDomainLs.add(new SemanticDomain("COUNTRY", "Country", 0.85f));
        ColumnMetadata column = ColumnMetadata.Builder.column().id(0).name("name").type(Type.STRING).semanticDomains(semanticDomainLs).domain(semantic.name()).build();
        assertTrue(standardizeInvalid.acceptField(column));
    }

    @Test
    public void should_not_accept_column() {
        //no semantic
        ColumnMetadata column = ColumnMetadata.Builder.column().id(0).name("name").type(Type.STRING).build();
        assertFalse(standardizeInvalid.acceptField(column));
        column = ColumnMetadata.Builder.column().id(0).name("name").type(Type.STRING).domain("COUNTRY").build();
        assertFalse(standardizeInvalid.acceptField(column));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, standardizeInvalid.getBehavior().size());
        assertTrue(standardizeInvalid.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
        assertTrue(standardizeInvalid.getBehavior().contains(ActionDefinition.Behavior.NEED_STATISTICS_INVALID));
    }

}
