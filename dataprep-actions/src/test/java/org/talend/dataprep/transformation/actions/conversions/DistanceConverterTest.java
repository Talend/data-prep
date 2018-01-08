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
package org.talend.dataprep.transformation.actions.conversions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataquality.converters.DistanceEnum;

/**
 * Test class for Trim action. Creates one consumer, and test it.
 *
 * @see DistanceConverterTest
 */
public class DistanceConverterTest extends AbstractMetadataBaseTest<DistanceConverter> {

    public DistanceConverterTest() {
        super(new DistanceConverter());
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
        assertThat(name, Is.is("distance_converter"));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
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
        List<String> parameterNames = Arrays.asList("create_new_column", "from_unit", "to_unit", "precision", "column_id", "row_id",
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
    }

    @Test
    public void testEmpty() { testConversion("", DistanceEnum.METER, "", DistanceEnum.YARD, "9"); }

    @Test
    public void testNull() { testConversion(null, DistanceEnum.METER, null, DistanceEnum.YARD, "9"); }

    @Test
    public void testBlank() { testConversion(" ", DistanceEnum.METER, " ", DistanceEnum.YARD, "9"); }

    @Test
    public void testZero() { testConversion("0", DistanceEnum.METER, "0.0", DistanceEnum.YARD, "1"); }

    @Test
    public void testNanInput1() { testConversion(" a ", DistanceEnum.METER, " a ", DistanceEnum.YARD, "9"); }

    @Test
    public void testNanInput2() { testConversion(" 3a ", DistanceEnum.METER, " 3a ", DistanceEnum.YARD, "9"); }

    @Test
    public void testNanInput3() { testConversion(" 5@ ", DistanceEnum.METER, " 5@ ", DistanceEnum.YARD, "9"); }

    @Test
    public void testNanInput4() { testConversion(" 5@6a8 ", DistanceEnum.METER, " 5@6a8 ", DistanceEnum.YARD, "9"); }

    @Test
    public void testMaxValue() { testConversion(String.valueOf(Double.MAX_VALUE) , DistanceEnum.METER, String.valueOf(Double.MAX_VALUE), DistanceEnum.YARD, "1"); }

    @Test
    public void testPositiveInfinityValue() {
        BigDecimal simple_max = BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal double_max = simple_max.add(simple_max);
        testConversion(double_max.toPlainString() , DistanceEnum.METER, double_max.toPlainString(), DistanceEnum.YARD, "1");
    }

    @Test
    public void testNegativeInfinityValue() {
        BigDecimal simple_max = BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal double_max = simple_max.add(simple_max);
        BigDecimal neg_double_max = double_max.negate();
        testConversion(neg_double_max.toPlainString() , DistanceEnum.METER, neg_double_max.toPlainString(), DistanceEnum.YARD, "1");
    }

    @Test
    public void testMinValue() { testConversion(String.valueOf(Double.MIN_VALUE) , DistanceEnum.METER, String.valueOf(0.0), DistanceEnum.YARD, "1"); }

    @Test
    public void testNegativeNumber() { testConversion("-1", DistanceEnum.METER, "-1.093613298", DistanceEnum.YARD, "9"); }

    @Test
    public void meter2yardWithBlank() { testConversion(" 1 ", DistanceEnum.METER, "1.093613298", DistanceEnum.YARD, "9"); }

    @Test
    public void meter2yard() { testConversion("1.0", DistanceEnum.METER, "1.093613298", DistanceEnum.YARD, "9"); }

    @Test
    public void yard2meter() { testConversion("1.0", DistanceEnum.YARD, "0.914400000", DistanceEnum.METER, "9"); }

    @Test
    public void mile2kilometer() { testConversion("1.0", DistanceEnum.MILE, "1.609344000", DistanceEnum.KILOMETER, "9"); }

    @Test
    public void test_apply_inplace() { testConversion("1.0", DistanceEnum.KILOMETER, "0.621371192", DistanceEnum.MILE, "9"); }

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
        parameters.put("from_unit", deFrom.name());
        parameters.put("to_unit", deTo.name());
        parameters.put("precision", precision);

        // when
        ActionTestWorkbench.test(Collections.singletonList(row1), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row1.get("0001"));
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "David");
        rowContent.put("0001", "1.0");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(123L);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put("column_id", "0001");
        parameters.put("from_unit", DistanceEnum.KILOMETER.name());
        parameters.put("to_unit", DistanceEnum.MILE.name());
        parameters.put("precision", "9");

        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");

        // when
        ActionTestWorkbench.test(Collections.singletonList(row1), actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("1.0", row1.get("0001"));
        assertEquals("0.621371192", row1.get("0002"));

        ColumnMetadata expected = ColumnMetadata.Builder.column().id(2).name("0001_in_MILE").type(Type.DOUBLE).build();
        ColumnMetadata actual = row1.getRowMetadata().getById("0002");
        assertEquals(expected, actual);
    }

}
