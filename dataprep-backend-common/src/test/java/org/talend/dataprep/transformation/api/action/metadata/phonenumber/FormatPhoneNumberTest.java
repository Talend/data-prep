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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

public class FormatPhoneNumberTest extends AbstractMetadataBaseTest {

	@Autowired
	private FormatPhoneNumber action;

	private Map<String, String> parameters;

	private static final String[][] DATASET = new String[][] {
			{"+33656965822", "FR Phone" }, //
			{"+33(0)147554323", "FR Phone" },
			{"+1-541-754-3010","US Phone"},
			{"1-541-754-3010","US Phone"}
			
	};

	private static final String[][] EXPECTED_FORMAT_DATASET = new String[][] {
			{"+33 6 56 96 58 22", "FR Phone" }, //
			{"+33 1 47 55 43 23", "FR Phone" },
			{"+1 541-754-3010","US Phone"},
			{"+1 541-754-3010","US Phone"}
			
	};

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
	public void should_format() {
		// given
		List<DataSetRow> rowList = new ArrayList<DataSetRow>();
		for (int row = 0; row < DATASET.length; row++) {

			final Map<String, String> values = new HashMap<>();

			for (int col = 0; col < DATASET[0].length; col++) {
				values.put("000" + col, DATASET[row][col]);
			}
			DataSetRow dataSetRow= new DataSetRow(values);

			RowMetadata rowMetadata = dataSetRow.getRowMetadata();
			for (int col = 0; col < DATASET[0].length; col++) {
				ColumnMetadata meta = rowMetadata.getById("000" + col);
				meta.setDomain(DATASET[row][1]);
			}
			rowList.add(dataSetRow);
		}
		
		// when
		ActionTestWorkbench.test(rowList, factory.create(action, parameters));
		
		// then
		
		for (int row = 0; row < EXPECTED_FORMAT_DATASET[0].length; row++) {
			DataSetRow dataSetRow = rowList.get(row);
			assertEquals(dataSetRow.values().get("0000"),EXPECTED_FORMAT_DATASET[row][0]);
		}


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
