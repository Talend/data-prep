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
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the FahrenheitToCelsius action.
 *
 * @see FahrenheitToCelsius
 */
public class FahrenheitToCelsiusTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private FahrenheitToCelsius action;

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
        assertThat(name, is("fahrenheit_to_celsius"));
    }

    @Test
    public void testBasicValue() {
        testConversion("100", "37.78");
    }

    @Test
    public void test32Value() {
        testConversion("32", "0.00");
    }

    @Test
    public void test_NaN() {
        testConversion("toto", "");
    }

    @Test
    public void testNegativeValue() {
        testConversion("-100", "-73.33");
    }

    public void testConversion(String from, String expected) {
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
        rowContent.put("0001", "32");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row1.get("0002"));
        assertEquals("0.00", row2.get("0002"));

        assertEquals("0001_in Celsius", row1.getRowMetadata().getById("0002").getName());
    }

}