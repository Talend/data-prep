// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.conversions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.COUNTRY_CODE_ISO2;
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.COUNTRY_CODE_ISO3;
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.COUNTRY_NAME;
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.COUNTRY_NUMBER;
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.FROM_UNIT_PARAMETER;
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.TO_UNIT_PARAMETER;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

public class CountryConverterTest extends AbstractMetadataBaseTest<CountryConverter> {

    private Map<String, String> parameters;

    public CountryConverterTest() {
        super(new CountryConverter());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(CountryConverter.class.getResourceAsStream("countryConverterAction.json"));
    }

    @Test
    public void testCategory() {
        // when
        final String name = action.getCategory(Locale.US);

        // then
        assertThat(name, Is.is(ActionCategory.CONVERSIONS.getDisplayName(Locale.US)));
    }

    @Test
    public void testName() {
        // when
        final String name = action.getName();

        // then
        assertThat(name, Is.is("country_converter"));
    }

    @Override
    protected CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_ENABLED;
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

    @Test
    public void shouldGetParameters() throws Exception {
        // given
        List<String> parameterNames =
                Arrays.asList("create_new_column", "from_unit", "to_unit", "column_id", "row_id", "scope", "filter");

        // when
        final List<Parameter> parameters = action.getParameters(Locale.US);

        // then
        assertNotNull(parameters);
        assertEquals(7, parameters.size()); // 4 implicit parameters + 3 specific
        final List<String> expectedParametersNotFound = parameters
                .stream() //
                .map(Parameter::getName) //
                .filter(n -> !parameterNames.contains(n)) //
                .collect(Collectors.toList());
        assertTrue(expectedParametersNotFound.toString() + " not found", expectedParametersNotFound.isEmpty());
    }

    @Test
    public void test_apply_inplace() {
        // given
        final Map<String, String> values = new LinkedHashMap<>();
        values.put("0000", "France");
        values.put("0001", "Pomme de terre");
        values.put("0002", "01/08/2018");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "250");
        expectedValues.put("0001", "Pomme de terre");
        expectedValues.put("0002", "01/08/2018");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_NAME);
        parameters.put(TO_UNIT_PARAMETER, COUNTRY_NUMBER);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final Map<String, String> values = new LinkedHashMap<>();
        values.put("0000", "GEO");
        values.put("0001", "Pomme de terre");
        values.put("0002", "01/08/2018");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "GEO");
        expectedValues.put("0001", "Pomme de terre");
        expectedValues.put("0002", "01/08/2018");
        expectedValues.put("0003", "GE");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_CODE_ISO3);
        parameters.put(TO_UNIT_PARAMETER, COUNTRY_CODE_ISO2);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * Use country converter with the same param for input and output
     * should normalize the column.
     */
    @Test
    public void test_should_normalize_iso2_column() {
        // given
        final DataSetRow row1 = getRow("AF", "Pomme de terre");

        final DataSetRow row2 = getRow("AX", "Pomme de pain");

        final DataSetRow row3 = getRow("GEO", "Pomme d'api");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_CODE_ISO2);
        parameters.put(TO_UNIT_PARAMETER, COUNTRY_CODE_ISO2);

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, factory.create(action, parameters));

        // then
        assertFalse(row1.get("0000").isEmpty());
        assertFalse(row2.get("0000").isEmpty());
        assertTrue(row3.get("0000").isEmpty());
    }

    @Test
    public void test_should_return_empty_cell_because_not_a_valid_country_name() {
        // given
        final Map<String, String> values = new LinkedHashMap<>();
        values.put("0000", "Pomme de reinette");
        values.put("0001", "Pomme de terre");
        values.put("0002", "01/08/2018");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "Pomme de reinette");
        expectedValues.put("0001", "Pomme de terre");
        expectedValues.put("0002", "01/08/2018");
        expectedValues.put("0003", "");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_NAME);
        parameters.put(TO_UNIT_PARAMETER, COUNTRY_NUMBER);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_should_convert_with_english_french_country_name() {
        // given
        final DataSetRow row1 = getRow("Chine", "Pomme de terre");

        final DataSetRow row2 = getRow("Colombia", "Pomme de pain");

        final DataSetRow row3 = getRow("Japon", "Pomme d'api");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_NAME);
        parameters.put(TO_UNIT_PARAMETER, COUNTRY_CODE_ISO3);

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(row1.get("0000"), "CHN");
        assertEquals(row2.get("0000"), "COL");
        assertEquals(row3.get("0000"), "JPN");
    }
    
}