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
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.ENGLISH_COUNTRY_NAME;
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.FRENCH_COUNTRY_NAME;
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.FROM_UNIT_PARAMETER;
import static org.talend.dataprep.transformation.actions.conversions.CountryConverter.TO_UNIT_PARAMETER;

import java.io.IOException;
import java.util.ArrayList;
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
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.SemanticDomain;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

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
    public void shouldAcceptColumn() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
    }

    @Test
    public void shouldNotAcceptColumn() {
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void shouldHaveExpectedBehavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

    @Test
    public void shouldGetParameters() throws Exception {
        // given
        List<String> parameterNames = Arrays.asList("create_new_column", "from_unit", "to_unit", "column_id", "row_id", "scope",
                "filter");

        // when
        final List<Parameter> parameters = action.getParameters(Locale.US);

        // then
        assertNotNull(parameters);
        assertEquals(7, parameters.size()); // 4 implicit parameters + 3 specific
        final List<String> expectedParametersNotFound = parameters.stream() //
                .map(Parameter::getName) //
                .filter(n -> !parameterNames.contains(n)) //
                .collect(Collectors.toList());
        assertTrue(expectedParametersNotFound.toString() + " not found", expectedParametersNotFound.isEmpty());
    }

    @Test
    public void testDefaultParamsISO3input() {
        boolean result;
        ColumnMetadata column = new ColumnMetadata();
        List<SemanticDomain> domain = new ArrayList<>();
        SemanticCategoryEnum inputSem = SemanticCategoryEnum.COUNTRY_CODE_ISO3;
        domain.add(new SemanticDomain(inputSem.getId(), inputSem.getDisplayName(), 1));
        column.setSemanticDomains(domain);

        List<Parameter> params = action.adapt(column).getParameters(Locale.getDefault());

        List<Parameter> fromUnit = params.stream() //
                .filter(param -> param.getName().equals(FROM_UNIT_PARAMETER)) //
                .collect(Collectors.toList());

        List<Parameter> toUnit = params.stream() //
                .filter(param -> param.getName().equals(TO_UNIT_PARAMETER)) //
                .collect(Collectors.toList());

        result = toUnit.get(0).getDefault().equals(ENGLISH_COUNTRY_NAME) //
                && fromUnit.get(0).getDefault().equals(COUNTRY_CODE_ISO3);

        assertTrue(result);
    }

    @Test
    public void testDefaultParamsISO2input() {
        boolean result;
        ColumnMetadata column = new ColumnMetadata();
        List<SemanticDomain> domain = new ArrayList<>();
        SemanticCategoryEnum inputSem = SemanticCategoryEnum.COUNTRY_CODE_ISO2;
        domain.add(new SemanticDomain(inputSem.getId(), inputSem.getDisplayName(), 1));
        column.setSemanticDomains(domain);

        List<Parameter> params = action.adapt(column).getParameters(Locale.getDefault());

        List<Parameter> fromUnit = params.stream() //
                .filter(param -> param.getName().equals(FROM_UNIT_PARAMETER)) //
                .collect(Collectors.toList());

        List<Parameter> toUnit = params.stream() //
                .filter(param -> param.getName().equals(TO_UNIT_PARAMETER)) //
                .collect(Collectors.toList());

        result = toUnit.get(0).getDefault().equals(ENGLISH_COUNTRY_NAME) //
                && fromUnit.get(0).getDefault().equals(COUNTRY_CODE_ISO2);

        assertTrue(result);
    }

    @Test
    public void testDefaultParamsDefaultInput() {
        boolean result;
        ColumnMetadata column = new ColumnMetadata();

        List<Parameter> params = action.adapt(column).getParameters(Locale.getDefault());

        List<Parameter> fromUnit = params.stream() //
                .filter(param -> param.getName().equals(FROM_UNIT_PARAMETER)) //
                .collect(Collectors.toList());

        List<Parameter> toUnit = params.stream() //
                .filter(param -> param.getName().equals(TO_UNIT_PARAMETER)) //
                .collect(Collectors.toList());

        result = toUnit.get(0).getDefault().equals(COUNTRY_CODE_ISO2) //
                && fromUnit.get(0).getDefault().equals(COUNTRY_NAME);

        assertTrue(result);
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
        values.put("0000", "GAB");
        values.put("0001", "Pomme de terre");
        values.put("0002", "01/08/2018");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "GAB");
        expectedValues.put("0001", "Pomme de terre");
        expectedValues.put("0002", "01/08/2018");
        expectedValues.put("0003", "Gabon");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_CODE_ISO3);
        parameters.put(TO_UNIT_PARAMETER, FRENCH_COUNTRY_NAME);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testCountryNumberInput() {
        // given
        final Map<String, String> values = new LinkedHashMap<>();
        values.put("0000", "710");
        values.put("0001", "Pomme de terre");
        values.put("0002", "01/08/2018");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "South Africa");
        expectedValues.put("0001", "Pomme de terre");
        expectedValues.put("0002", "01/08/2018");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_NUMBER);
        parameters.put(TO_UNIT_PARAMETER, ENGLISH_COUNTRY_NAME);

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
    public void testShouldNormalizeISO2column() {
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
        assertEquals("AF", row1.get("0000"));
        assertEquals("AX", row2.get("0000"));
        assertTrue(row3.get("0000").isEmpty());
    }

    @Test
    public void testShouldConvertFromIso2ToFrenchCountryName() {
        // given
        final DataSetRow row1 = getRow("AF", "Pomme de terre");
        final DataSetRow row2 = getRow("Ax", "Pomme de pain");
        final DataSetRow row3 = getRow("af", "Pomme d'ammour");
        final DataSetRow row4 = getRow("aF", "Pomme de reinnette");
        final DataSetRow row5 = getRow("ir", "Pomme de pin");
        final DataSetRow row6 = getRow("AX", "Pomme d'arrosoir");
        final DataSetRow row7 = getRow("GEO", "Pomme d'api");
        final DataSetRow row8 = getRow("TW", "Pomme d'Eve");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_CODE_ISO2);
        parameters.put(TO_UNIT_PARAMETER, FRENCH_COUNTRY_NAME);

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5, row6, row7, row8), actionRegistry,
                factory.create(action, parameters));

        // then
        assertEquals("Afghanistan", row1.get("0000"));
        assertEquals("Îles Åland", row2.get("0000"));
        assertEquals("Afghanistan", row3.get("0000"));
        assertEquals("Afghanistan", row4.get("0000"));
        assertEquals("Iran", row5.get("0000"));
        assertEquals("Îles Åland", row6.get("0000"));
        assertTrue(row7.get("0000").isEmpty());
        assertEquals("Taiwan", row8.get("0000"));
    }

    @Test
    public void testShouldConvertFromIso3ToEnglishCountryName() {
        // given
        final DataSetRow row1 = getRow("AF", "Pomme de terre");
        final DataSetRow row2 = getRow("ALA", "Pomme de pain");
        final DataSetRow row3 = getRow("ala", "Pomme d'ammour");
        final DataSetRow row4 = getRow("aLa", "Pomme de reinnette");
        final DataSetRow row5 = getRow("irN", "Pomme de pin");
        final DataSetRow row6 = getRow("AX", "Pomme d'arrosoir");
        final DataSetRow row7 = getRow("GEO", "Pomme d'api");
        final DataSetRow row8 = getRow("TWn", "Pomme d'Eve");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_CODE_ISO3);
        parameters.put(TO_UNIT_PARAMETER, ENGLISH_COUNTRY_NAME);

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5, row6, row7, row8), actionRegistry,
                factory.create(action, parameters));

        // then
        assertTrue(row1.get("0000").isEmpty());
        assertEquals("Åland Islands", row2.get("0000"));
        assertEquals("Åland Islands", row3.get("0000"));
        assertEquals("Åland Islands", row4.get("0000"));
        assertEquals("Iran", row5.get("0000"));
        assertTrue(row6.get("0000").isEmpty());
        assertEquals("Georgia", row7.get("0000"));
        assertEquals("Taiwan", row8.get("0000"));
    }

    @Test
    public void testShouldConvertFromFrenchToEnglishCountryName() {
        // given
        final DataSetRow row1 = getRow("Îles ÅLand", "Pomme de terre");
        final DataSetRow row2 = getRow("OuzBékistan", "Pomme de pain");
        final DataSetRow row3 = getRow("Antigua et Barbuda", "Pomme d'ammour");
        final DataSetRow row4 = getRow("Iran", "Pomme de reinnette");
        final DataSetRow row5 = getRow("Kirghizistan ", "Pomme de pin");
        final DataSetRow row6 = getRow("Taiwan", "Pomme d'arrosoir");
        final DataSetRow row7 = getRow("Zimbabwe", "Pomme d'api");
        final DataSetRow row8 = getRow("Afghanistan", "Pomme d'Eve");
        final DataSetRow row9 = getRow("France", "Pomme de douche");
        final DataSetRow row10 = getRow("Taïwan", "Pomme du Calvados");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        parameters.put(FROM_UNIT_PARAMETER, FRENCH_COUNTRY_NAME);
        parameters.put(TO_UNIT_PARAMETER, ENGLISH_COUNTRY_NAME);

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5, row6, row7, row8, row9, row10), actionRegistry,
                factory.create(action, parameters));

        // then
        assertEquals("Åland Islands", row1.get("0000"));
        assertEquals("Uzbekistan", row2.get("0000"));
        assertEquals("Antigua and Barbuda", row3.get("0000"));
        assertEquals("Iran", row4.get("0000"));
        // TODO : this part should be improved and result should be Kirghizistan because this name is recognized by DQ as
        // TODO a valid country name
        assertEquals("", row5.get("0000"));
        assertEquals("Taiwan", row6.get("0000"));
        assertEquals("Zimbabwe", row7.get("0000"));
        assertEquals("Afghanistan", row8.get("0000"));
        assertEquals("France", row9.get("0000"));
        assertEquals("Taiwan", row10.get("0000"));

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        parameters.put(FROM_UNIT_PARAMETER,ENGLISH_COUNTRY_NAME );
        parameters.put(TO_UNIT_PARAMETER, FRENCH_COUNTRY_NAME);

        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5, row6, row7, row8, row9, row10), actionRegistry,
                factory.create(action, parameters));

        // then
        assertEquals("Îles Åland", row1.get("0000"));
        assertEquals("Ouzbékistan", row2.get("0000"));
        assertEquals("Antigua et Barbuda", row3.get("0000"));
        assertEquals("Iran", row4.get("0000"));
        assertEquals("", row5.get("0000"));
        assertEquals("Taiwan", row6.get("0000"));
        assertEquals("Zimbabwe", row7.get("0000"));
        assertEquals("Afghanistan", row8.get("0000"));
        assertEquals("France", row9.get("0000"));
        assertEquals("Taiwan", row10.get("0000"));
    }

    @Test
    public void testShouldReturnEmptyCellBecauseNotValidCountryName() {
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
    public void testShouldConvertWithEnglishOrFrenchCountryName() {
        // given
        final DataSetRow row1 = getRow("Chine", "Pomme de terre");
        final DataSetRow row2 = getRow("Colombia", "Pomme de pain");
        final DataSetRow row3 = getRow("Japon", "Pomme d'api");

        // test case sensitive
        final DataSetRow row4 = getRow("chine", "Pomme de douche");
        final DataSetRow row5 = getRow("colomBIa", "Pomme de discorde");
        final DataSetRow row6 = getRow("JAPON", "Pomme d'Adam");

        final DataSetRow row7 = getRow("Antigua and Barbuda", "Pomme d'Adam"); // Antigua-et-Barbuda
        final DataSetRow row8 = getRow("uzbekistan", "Pomme de tournevire");
        final DataSetRow row9 = getRow("Ouzbékistan", "Pomme de merveille");

        final DataSetRow row10 = getRow("chinA", "Pomme de Newton");
        final DataSetRow row11 = getRow("coLOMbie", "Pomme d'églantier");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "false");
        parameters.put(FROM_UNIT_PARAMETER, COUNTRY_NAME);
        parameters.put(TO_UNIT_PARAMETER, COUNTRY_CODE_ISO3);

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5, row6, row7, row8, row9, row10, row11),
                actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("CHN", row1.get("0000"));
        assertEquals("COL", row2.get("0000"));
        assertEquals("JPN", row3.get("0000"));
        assertEquals("CHN", row4.get("0000"));
        assertEquals("COL", row5.get("0000"));
        assertEquals("JPN", row6.get("0000"));
        assertEquals("ATG", row7.get("0000"));
        assertEquals("UZB", row8.get("0000"));
        assertEquals("UZB", row9.get("0000"));
        assertEquals("CHN", row10.get("0000"));
        assertEquals("COL", row11.get("0000"));
    }

}