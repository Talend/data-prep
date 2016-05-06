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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

public class FormatPhoneNumberTest extends AbstractMetadataBaseTest {

    @Autowired
    private FormatPhoneNumber action;

    private Map<String, String> parameters;

    private final String PHONE_FR1 = "+33656965822";

    private final String PHONE_FR2 = "+33(0)147554323";

    private final String PHONE_FR3 = "000147554323";

    private final String PHONE_US1 = "+1-541-754-3010";

    private final String PHONE_US2 = "15417543010";

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(FormatPhoneNumberTest.class.getResourceAsStream("formatphonenumber.json"));
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
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
        Map<String, String> values = new HashMap<>();
        values.put("0000", PHONE_FR1);

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33 6 56 96 58 22");

        ActionTestWorkbench.test(row, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());

    }

    @Test
    public void should_format_FR_E164() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "E164");
        Map<String, String> values = new HashMap<>();
        values.put("0000", PHONE_FR2);
        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+33147554323");
        ActionTestWorkbench.test(row, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_format_US_National() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "US");

        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "National");
        Map<String, String> values = new HashMap<>();
        values.put("0000", PHONE_US2);
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "(541) 754-3010");
        ActionTestWorkbench.test(row, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());

    }

    @Test
    public void should_format_US_RFC396() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "US");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "RFC396");

        Map<String, String> values = new HashMap<>();
        values.put("0000", PHONE_US1);
        DataSetRow row = new DataSetRow(values);
        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "tel:+1-541-754-3010");

        ActionTestWorkbench.test(row, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());

    }

    @Test
    public void should_format_defaut_parameter() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "");
        Map<String, String> values = new HashMap<>();
        values.put("0000", PHONE_US2);

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", "+1 541-754-3010");

        ActionTestWorkbench.test(row, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_format_defaut_parameter() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
        Map<String, String> values = new HashMap<>();
        values.put("0000", PHONE_FR2);// it is FR phone

        DataSetRow row = new DataSetRow(values);

        Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", PHONE_FR2);

        ActionTestWorkbench.test(row, factory.create(action, parameters));
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_not_format_invalid_phone() {
        parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "FR");
        parameters.put(FormatPhoneNumber.FORMAT_TYPE_PARAMETER, "International");
        Map<String, String> values = new HashMap<>();
        values.put("0000", PHONE_FR3);
        final DataSetRow row1 = new DataSetRow(values);
        final Map<String, Object> expectedValues = new LinkedHashMap<>();
        expectedValues.put("0000", PHONE_FR3);
        // when
        ActionTestWorkbench.test(row1, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row1.values());
    }

}
