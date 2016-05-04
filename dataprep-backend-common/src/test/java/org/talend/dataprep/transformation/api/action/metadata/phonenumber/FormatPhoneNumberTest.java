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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

public class FormatPhoneNumberTest extends AbstractMetadataBaseTest {

	@Autowired
	private FormatPhoneNumber action;

	private Map<String, String> parameters;
	
	@Before
	public void init() throws IOException {
		parameters = ActionMetadataTestUtils
				.parseParameters(FormatPhoneNumberTest.class
						.getResourceAsStream("formatphonenumber.json"));
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
	        assertEquals(5, parameters.size());
	    }


	@Test
	public void should_format_FR() {
		parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "FR");
		Map<String, String> values = new HashMap<>();
		values.put("0000", "+33656965822");
		
		DataSetRow row = new DataSetRow(values);

		Map<String, Object> expectedValues = new LinkedHashMap<>();
		expectedValues.put("0000", "+33 6 56 96 58 22"); 
		
		ActionTestWorkbench.test(row, factory.create(action, parameters));
		assertEquals(expectedValues, row.values());
		
		values = new HashMap<>();
		values.put("0000", "+33(0)147554323");
		row = new DataSetRow(values);
		expectedValues.put("0000", "+33 1 47 55 43 23"); 
		ActionTestWorkbench.test(row, factory.create(action, parameters));
		assertEquals(expectedValues, row.values());
	}
	
	@Test
	public void should_format_US() {
		parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "US");
		Map<String, String> values = new HashMap<>();
		values.put("0000", "+1-541-754-3010");
		
		DataSetRow row = new DataSetRow(values);

		Map<String, Object> expectedValues = new LinkedHashMap<>();
		expectedValues.put("0000", "+1 541-754-3010"); 
		
		ActionTestWorkbench.test(row, factory.create(action, parameters));
		assertEquals(expectedValues, row.values());
	}
	
	@Test
	public void should_format_defaut_parameter() {
		parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "");
		Map<String, String> values = new HashMap<>();
		values.put("0000", "1-541-754-3010");
		
		DataSetRow row = new DataSetRow(values);

		Map<String, Object> expectedValues = new LinkedHashMap<>();
		expectedValues.put("0000", "+1 541-754-3010"); 
		
		ActionTestWorkbench.test(row, factory.create(action, parameters));
		assertEquals(expectedValues, row.values());
	}
	
	@Test
	public void should_not_format_defaut_parameter() {
		parameters.put(FormatPhoneNumber.REGIONS_PARAMETER, "");
		Map<String, String> values = new HashMap<>();
		values.put("0000", "+33(0)147554323");//it is FR phone
		
		DataSetRow row = new DataSetRow(values);

		Map<String, Object> expectedValues = new LinkedHashMap<>();
		expectedValues.put("0000", "+33(0)147554323"); 
		
		ActionTestWorkbench.test(row, factory.create(action, parameters));
		assertEquals(expectedValues, row.values());
	}
	
	@Test
	public void should_not_format() {
		Map<String, String> values = new HashMap<>();
		values.put("0000", "000147554323");
		final DataSetRow row1 = new DataSetRow(values);
		row1.getRowMetadata().getById("0000").setDomain("FR Phone");

		final Map<String, Object> expectedValues = new LinkedHashMap<>();
		expectedValues.put("0000", "000147554323"); 
		// when
		ActionTestWorkbench.test(row1, factory.create(action, parameters));

		// then
		assertEquals(expectedValues, row1.values());
	}

}
