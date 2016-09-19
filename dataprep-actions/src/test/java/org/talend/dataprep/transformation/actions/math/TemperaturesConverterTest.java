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

package org.talend.dataprep.transformation.actions.math;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.math.TemperaturesConverter.TemperatureUnit.CELSIUS;
import static org.talend.dataprep.transformation.actions.math.TemperaturesConverter.TemperatureUnit.FAHRENHEIT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the CelsiusToFahrenheit action.
 */
public class TemperaturesConverterTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private TemperaturesConverter action;

    @Test
    public void testCategory() {
        // when
        final String name = action.getCategory();

        // then
        assertThat(name, is("Conversions"));
    }

    @Test
    public void testName() {
        // when
        final String name = action.getName();

        // then
        assertThat(name, is("temperatures_converter"));
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
        List<String> parameterNames = Arrays.asList("to_temperature", "from_temperature", "precision", "column_id", "row_id",
                "scope", "filter");

        // when
        final List<Parameter> parameters = action.getParameters();

        // then
        assertNotNull(parameters);
        assertEquals(7, parameters.size()); // 4 implicit parameters + 3 specific
        final List<String> expectedParametersNotFound = parameters.stream() //
                .map(Parameter::getName) //
                .filter(n -> !parameterNames.contains(n)) //
                .collect(Collectors.toList());
        assertTrue(expectedParametersNotFound.toString() + " not found", expectedParametersNotFound.isEmpty());
    }

    public void testConversion(String from, TemperaturesConverter.TemperatureUnit fromUnit, String expected,
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

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row1.get("0002"));
        assertEquals("32", row2.get("0002"));

        assertEquals("0001_in Fahrenheit", row1.getRowMetadata().getById("0002").getName());
    }

}