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
package org.talend.dataprep.transformation.api.action.metadata.phonenumber;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;

public class FormatPhoneNumberTest extends AbstractMetadataBaseTest {

    @Autowired
    private FormatPhoneNumber action;

    private Map<String, String> parameters;

    private Locale previousLocale;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(FormatPhoneNumberTest.class.getResourceAsStream("formatphonenumber.json"));
        previousLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(previousLocale);
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.PHONE_NUMBER.getDisplayName()));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
        assertTrue(action.acceptColumn(getColumn(Type.INTEGER)));
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void testParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters();
        assertEquals(6, parameters.size());
    }

    @Test
    public void should_format_FR_International() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
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
    public void should_format_FR_E164() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "E164");
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
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "National");
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
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "RFC3966");
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
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "other (region)");
        parameters.put(FormatPhoneNumber.MANUAL_REGION_PARAMETER_STRING, "CN");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
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
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
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
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "E164");
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
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "National");
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
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "RFC3966");
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
    public void should_format_US_RFC396_default() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "RFC3966");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "15417543010");
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        if (Locale.getDefault() == Locale.US) {
            expectedValues.put("0000", "tel:+1-541-754-3010");
        } else {
            expectedValues.put("0000", "15417543010");
        }

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());

    }

    @Test
    public void should_format_with_other_column_of_mixed_regions() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "National");
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
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
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
    public void should_not_format_defaut_region() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33(0)147554323");// it is FR phone

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33(0)147554323");

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_format_invalid_phone() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER_CONSTANT_MODE, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
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
    public void should_not_format_FR_E164_with_column_US() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "E164");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.SELECTED_COLUMN_PARAMETER);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33(0)147554323");
        values.put("0001", "US");
        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33(0)147554323");
        expectedValues.put("0001", "US");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_format_FR_International_with_column_CN() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.SELECTED_COLUMN_PARAMETER);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33656965822");
        values.put("0001", "CN");

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33656965822");
        expectedValues.put("0001", "CN");
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());

    }

    @Test
    public void should_notformat_US_National_with_column() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "National");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.SELECTED_COLUMN_PARAMETER);
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
    public void should_not_format_FR_RFC396_with_column() {
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "RFC3966");
        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.SELECTED_COLUMN_PARAMETER);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");
        Map<String, String> values = new HashMap<>();
        values.put("0000", "+33(0)147554323");
        values.put("0001", null);
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33(0)147554323");
        expectedValues.put("0001", null);

        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());

    }

}
