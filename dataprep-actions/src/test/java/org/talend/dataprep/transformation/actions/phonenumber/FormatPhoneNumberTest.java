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
package org.talend.dataprep.transformation.actions.phonenumber;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.column.CreateNewColumnTest;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.*;

import static java.util.Collections.singletonMap;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.DE_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.FR_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.UK_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.US_PHONE;

public class FormatPhoneNumberTest extends AbstractMetadataBaseTest<FormatPhoneNumber> {

    private Map<String, String> parameters;

    public FormatPhoneNumberTest() {
        super(new FormatPhoneNumber());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(FormatPhoneNumberTest.class.getResourceAsStream("formatphonenumber.json"));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.VISIBLE_DISABLED;
    }

    @Test
    public void testCategory() throws Exception {
        assertEquals(action.getCategory(Locale.US), ActionCategory.PHONE_NUMBER.getDisplayName(Locale.US));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING, PHONE)));
        assertTrue(action.acceptField(getColumn(Type.STRING, US_PHONE)));
        assertTrue(action.acceptField(getColumn(Type.STRING, UK_PHONE)));
        assertTrue(action.acceptField(getColumn(Type.STRING, DE_PHONE)));
        assertTrue(action.acceptField(getColumn(Type.STRING, FR_PHONE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.FLOAT)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void testParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(Locale.US);
        assertEquals(7, parameters.size());

        // Test on items label for TDP-2914:
        final SelectParameter useWithParam = (SelectParameter) parameters.get(5);
        assertEquals("Value", useWithParam.getItems().get(1).getLabel());

        final SelectParameter regionsListParam = (SelectParameter) useWithParam.getItems().get(1).getParameters().get(0);
        assertEquals("American standard", regionsListParam.getItems().get(0).getLabel());
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_INTERNATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33656965822");

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33656965822");
        expectedValues.put("0001", "+33 6 56 96 58 22");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(1).name("0000_formatted").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0001");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_in_newcolumn_with_empty_values() {
        // given
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "FR");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(ActionsUtils.CREATE_NEW_COLUMN, "true");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "");

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "");
        expectedValues.put("0001", "");

        //when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(1).name("0000_formatted").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0001");
        assertEquals(expected, actual);
    }

    @Test
    public void test_apply_inplace() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_INTERNATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33656965822");

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33 6 56 96 58 22");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_default_phone_format() {
        Map<String, String> values = new HashMap<>();
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "phone_test");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);

        values.put("0000", "+33656965822");

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33656965822");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_FR_E164() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_E164);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33(0)147554323");
        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33147554323");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_US_National() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "US");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_NATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "15417543010");
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "(541) 754-3010");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_US_RFC396() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "US");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_RFC3966);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);

        Map<String, String> values = new HashMap<>();
        values.put("0000", "+1-541-754-3010");
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "tel:+1-541-754-3010");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_region_is_manual() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, FormatPhoneNumber.OTHER_REGION_TO_BE_SPECIFIED);
        parameters.put(FormatPhoneNumber.MANUAL_REGION_PARAMETER_STRING, "CN");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_INTERNATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "18611281111");// it is CN phone

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+86 186 1128 1111");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_FR_International_with_column() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_INTERNATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33656965822");
        values.put("0001", "FR");

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33 6 56 96 58 22");
        expectedValues.put("0001", "FR");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_FR_E164_with_column() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_E164);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33(0)147554323");
        values.put("0001", "FR");
        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33147554323");
        expectedValues.put("0001", "FR");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_US_National_with_column() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_NATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "15417543010");
        values.put("0001", "US");
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "(541) 754-3010");
        expectedValues.put("0001", "US");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_US_RFC396_with_column() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_RFC3966);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+1-541-754-3010");
        values.put("0001", "US");
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "tel:+1-541-754-3010");
        expectedValues.put("0001", "US");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    // without column and without CONSTANT_MODE
    public void should_not_format_US_RFC396_without_region_code_param() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_RFC3966);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "15417543010");
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "15417543010");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_with_other_column_of_mixed_regions() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_NATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+1-541-754-3010");
        values.put("0001", "US");
        DataSetRow row = new DataSetRow(values);

        Map<String, String> values2 = new HashMap<>();
        values2.put("0000", "+33656965822");
        values2.put("0001", "FR");
        DataSetRow row2 = new DataSetRow(values2);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "(541) 754-3010");
        expectedValues.put("0001", "US");

        Map<String, Object> expectedValues2 = new LinkedHashMap<>();
        expectedValues2.put("0000", "06 56 96 58 22");
        expectedValues2.put("0001", "FR");

        ActionTestWorkbench.test(Arrays.asList(row, row2), actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
        assertEquals(expectedValues2, row2.values());
    }

    @Test
    public void should_not_format_phone_is_null() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "US");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_INTERNATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", null);// it is FR phone

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", null);

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_format_formattype_is_null() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "US");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, null);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "15417543010");
        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "15417543010");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_defaut_region() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_INTERNATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33(0)147554323");// it is FR phone

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33 1 47 55 43 23");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_format_invalid_phone() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_INTERNATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "000147554323");
        final DataSetRow row1 = new DataSetRow(values);
        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "000147554323");
        // when
        ActionTestWorkbench.test(row1, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row1.values());
    }

    @Test
    public void should_not_format_parameters_are_empty() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "15417543010");

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "15417543010");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_FR_E164_even_with_column_US() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_E164);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33(0)147554323");
        values.put("0001", "US");
        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33147554323");
        expectedValues.put("0001", "US");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_FR_International_even_with_column_CN() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_INTERNATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33656965822");
        values.put("0001", "CN");

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33 6 56 96 58 22");
        expectedValues.put("0001", "CN");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_notformat_US_National_with_column() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_NATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "15417543010");
        values.put("0001", "FR");
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "15417543010");
        expectedValues.put("0001", "FR");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_FR_RFC396_with_null_column() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_RFC3966);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33(0)147554323");
        values.put("0001", null);
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "tel:+33-1-47-55-43-23");
        expectedValues.put("0001", null);

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_possible_us_phone_with_column() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_NATIONAL);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "300-456-1500");
        values.put("0001", "US");
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "(300) 456-1500");
        expectedValues.put("0001", "US");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_format() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.TYPE_E164);
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, FormatPhoneNumber.US_REGION_CODE);
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "14/07/2010");
        values.put("0001", null);
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "14/07/2010");
        expectedValues.put("0001", null);

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

    @Test
    public void should_format_old_type_international_TDP_5256() {
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        // old legacy parameters
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.OLD_TYPE_INTERNATIONAL);

        // given
        final DataSetRow row = new DataSetRow(singletonMap("0000", "+33656965822"));

        final Map<String, String> expectedValues = singletonMap("0000", "+33 6 56 96 58 22");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_old_type_national_TDP_5256() {
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        // old legacy parameters
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.OLD_TYPE_NATIONAL);

        // given
        final DataSetRow row = new DataSetRow(singletonMap("0000", "+33656965822"));

        final Map<String, String> expectedValues = singletonMap("0000", "06 56 96 58 22");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_old_region_code_with_old_international_type_TDP_5256() {
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(FormatPhoneNumber.MANUAL_REGION_PARAMETER_STRING, "JP");
        // old legacy parameters
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, FormatPhoneNumber.OLD_OTHER_REGION_TO_BE_SPECIFIED);
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.OLD_TYPE_INTERNATIONAL);

        // given
        final DataSetRow row = new DataSetRow(singletonMap("0000", "757-682-7116"));

        final Map<String, String> expectedValues = singletonMap("0000", "+81 7576827116");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_old_region_code_with_old_national_type_TDP_5256() {
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(FormatPhoneNumber.MANUAL_REGION_PARAMETER_STRING, "JP");
        // old legacy parameters
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, FormatPhoneNumber.OLD_OTHER_REGION_TO_BE_SPECIFIED);
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, FormatPhoneNumber.OLD_TYPE_NATIONAL);

        // given
        final DataSetRow row = new DataSetRow(singletonMap("0000", "757-682-7116"));

        final Map<String, String> expectedValues = singletonMap("0000", "7576827116");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }
}
