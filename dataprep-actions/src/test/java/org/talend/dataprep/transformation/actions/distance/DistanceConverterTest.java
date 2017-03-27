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
package org.talend.dataprep.transformation.actions.distance;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataquality.converters.DistanceEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

/**
 * Test class for Trim action. Creates one consumer, and test it.
 *
 * @see DistanceConverterTest
 */
public class DistanceConverterTest extends AbstractMetadataBaseTest {


    /**
     * The action to test.
     */
    private DistanceConverter action = new DistanceConverter();

    @Test
    public void testCategory() {
        // when
        final String name = action.getCategory();

        // then
        assertThat(name, Is.is(ActionCategory.CONVERSIONS.getDisplayName()));
    }

    @Test
    public void testName() {
        // when
        final String name = action.getName();

        // then
        assertThat(name, Is.is("convert_distance"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

    @Test
    public void shouldGetParameters() throws Exception {
        // given
        List<String> parameterNames = Arrays.asList("from_distance", "to_distance", "precision", "column_id", "row_id",
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

    @Test
    public void meter2yard() { testConversion("1.0", DistanceEnum.METER, "1.093613298", DistanceEnum.YARD, "9"); }

    @Test
    public void yard2meter() { testConversion("1.0", DistanceEnum.YARD, "0.914400000", DistanceEnum.METER, "9"); }

    @Test
    public void mile2kilometer() { testConversion("1.0", DistanceEnum.MILE, "1.609344000", DistanceEnum.KILOMETER, "9"); }

    @Test
    public void kilometer2mile() { testConversion("1.0", DistanceEnum.KILOMETER, "0.621371192", DistanceEnum.MILE, "9"); }

    private void testConversion(String from, DistanceEnum deFrom, String expected, DistanceEnum deTo, String precision) {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", from);
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(123L);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("from_distance", deFrom.name());
        parameters.put("to_distance", deTo.name());
        parameters.put("precision", precision);

        // when
        ActionTestWorkbench.test(Arrays.asList(row1), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row1.get("0001"));
    }

}
