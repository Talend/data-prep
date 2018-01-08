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
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.conversions.TemperaturesConverter.TemperatureUnit.CELSIUS;
import static org.talend.dataprep.transformation.actions.conversions.TemperaturesConverter.TemperatureUnit.FAHRENHEIT;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the CelsiusToFahrenheit action.
 */
public class TemperaturesConverterTest extends AbstractMetadataBaseTest<TemperaturesConverter> {

    public TemperaturesConverterTest() {
        super(new TemperaturesConverter());
    }

    @Test
    public void testCategory() {
        // when
        final String name = action.getCategory(Locale.US);

        // then
        assertThat(name, is(ActionCategory.CONVERSIONS.getDisplayName(Locale.US)));
    }

    @Test
    public void testName() {
        // when
        final String name = action.getName();

        // then
        assertThat(name, is("temperatures_converter"));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void test_apply_inplace() {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "0");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(123L);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("from_temperature", CELSIUS.name());
        parameters.put("to_temperature", FAHRENHEIT.name());
        parameters.put("precision", "0");

        // when
        ActionTestWorkbench.test(Collections.singletonList(row1), actionRegistry, factory.create(action, parameters));

        // then
        // assertEquals("365", row1.get("0001"));
        assertEquals("32", row1.get("0001"));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "0");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(123L);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("from_temperature", CELSIUS.name());
        parameters.put("to_temperature", FAHRENHEIT.name());
        parameters.put("precision", "0");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(Collections.singletonList(row1), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("0", row1.get("0001"));
        assertEquals("32", row1.get("0002"));

        ColumnMetadata expected = ColumnMetadata.Builder.column().id(2).name("0001_in_Fahrenheit").type(Type.DOUBLE).build();
        ColumnMetadata actual = row1.getRowMetadata().getById("0002");
        assertEquals(expected, actual);
    }

    @Test
    public void testBasicValue() {
        testConversion("37.778", CELSIUS, "100.000", FAHRENHEIT);
    }

    @Test
    public void test32Value() {
        testConversion("0", CELSIUS, "32", FAHRENHEIT);
    }

    @Test
    public void test_NaN() {
        testConversion("toto", CELSIUS, "", FAHRENHEIT);
    }

    @Test
    public void testNegativeValue() {
        testConversion("-100", CELSIUS, "-148", FAHRENHEIT);
    }

    @Test
    public void shouldGetParameters() throws Exception {
        // given
        List<String> parameterNames = Arrays.asList("create_new_column", "to_temperature", "from_temperature", "precision", "column_id", "row_id",
                "scope", "filter");

        // when
        final List<Parameter> parameters = action.getParameters(Locale.US);

        // then
        assertNotNull(parameters);
        assertEquals(8, parameters.size()); // 4 implicit parameters + 3 specific
        final List<String> expectedParametersNotFound = parameters.stream() //
                .map(Parameter::getName) //
                .filter(n -> !parameterNames.contains(n)) //
                .collect(Collectors.toList());
        assertTrue(expectedParametersNotFound.toString() + " not found", expectedParametersNotFound.isEmpty());

        // Test on items label for TDP-2943:
        assertEquals("Fahrenheit", ((SelectParameter) parameters.get(5)).getItems().get(0).getLabel());
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

    private void testConversion(String from, TemperaturesConverter.TemperatureUnit fromUnit, String expected,
                               TemperaturesConverter.TemperatureUnit toUnit) {
        // given
        long rowId = 120;

        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", from);
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(rowId++);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "John");
        rowContent.put("0001", "0");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("from_temperature", fromUnit.name());
        parameters.put("to_temperature", toUnit.name());

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row1.get("0002"));
        assertEquals("32", row2.get("0002"));

        assertEquals("0001_in_Fahrenheit", row1.getRowMetadata().getById("0002").getName());
    }

}
