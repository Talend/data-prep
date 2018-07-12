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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.DE_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.FR_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.UK_PHONE;
import static org.talend.dataquality.semantic.classifier.SemanticCategoryEnum.US_PHONE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

/**
 * Test class for ExtractPhoneInformation action. Creates one consumer, and test it.
 *
 * @see ExtractPhoneInformation
 */
public class ExtractPhoneInformationTest extends AbstractMetadataBaseTest<ExtractPhoneInformation> {

    private Map<String, String> parameters;

    public ExtractPhoneInformationTest() {
        super(new ExtractPhoneInformation());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(ExtractPhoneInformationTest.class.getResourceAsStream("extractphoneinformation.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.PHONE_NUMBER.getDisplayName(Locale.US)));
    }

    @Override
    protected CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.INVISIBLE_ENABLED;
    }

    @Test
    public void test_apply_inplace() throws Exception {
        // Nothing to test, this action is never applied in place
    }

    @Test
    public void test_apply_in_newcolumn() {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("01 23 45 67 89").domain(SemanticCategoryEnum.FR_PHONE.name()))//
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "01 23 45 67 89");
        expectedValues.put("0003", "Fix_Line");
        expectedValues.put("0004", "33");
        expectedValues.put("0005", "FR");
        expectedValues.put("0006", "France");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "Europe/Paris");
        expectedValues.put("0002", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_GB_apply_in_newcolumn() {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("01234567890").domain(SemanticCategoryEnum.UK_PHONE.name()))//
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "01234567890");
        expectedValues.put("0003", "Fix_Line");
        expectedValues.put("0004", "44");
        expectedValues.put("0005", "GB");
        expectedValues.put("0006", "Bedford");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "Europe/London");
        expectedValues.put("0002", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_apply_in_germannewcolumn() {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("+49-89-636-48018").type(Type.STRING))//
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "+49-89-636-48018");
        expectedValues.put("0003", "Fix_Line");
        expectedValues.put("0004", "49");
        expectedValues.put("0005", "DE");
        expectedValues.put("0006", "Munich");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "Europe/Berlin");
        expectedValues.put("0002", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_apply_in_noNativeSemanticType() {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("+49-89-636-48018").domain("newSemanticType"))//
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "+49-89-636-48018");
        expectedValues.put("0003", "Fix_Line");
        expectedValues.put("0004", "49");
        expectedValues.put("0005", "DE");
        expectedValues.put("0006", "Munich");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "Europe/Berlin");
        expectedValues.put("0002", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_apply_in_USValueInAFrenchColumn() {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("tel:+1-541-754-3010").domain(SemanticCategoryEnum.FR_PHONE.name()))//
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "tel:+1-541-754-3010");
        expectedValues.put("0003", "Fixed_Line_Or_Mobile");
        expectedValues.put("0004", "1");
        expectedValues.put("0005", "US");
        expectedValues.put("0006", "Corvallis, OR");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "America/Los_Angeles");
        expectedValues.put("0002", "tata");
        expectedValues.put("__tdpInvalid", "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_invalid_values() {
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("01 23 45 67 8").domain(SemanticCategoryEnum.FR_PHONE.name()))//
                .with(value("tata").type(Type.STRING)) //
                .build();

        row.setInvalid("0001");
        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "01 23 45 67 8");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0005", "");
        expectedValues.put("0006", "");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "");
        expectedValues.put("0002", "tata");
        expectedValues.put("__tdpInvalid", "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_empty_values() {
        // given
        // given
        final DataSetRow row = builder() //
                .with(value("toto").type(Type.STRING)) //
                .with(value("").domain(SemanticCategoryEnum.FR_PHONE.name()))//
                .with(value("tata").type(Type.STRING)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "toto");
        expectedValues.put("0001", "");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0005", "");
        expectedValues.put("0006", "");
        expectedValues.put("0007", "");
        expectedValues.put("0008", "");
        expectedValues.put("0002", "tata");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_metadata() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "fr_phone"));
        input.add(createMetadata("0002", "last update"));
        final DataSetRow row = new DataSetRow(new RowMetadata(input));

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "fr_phone"));
        expected.add(createMetadata("0003", "fr_phone_type"));
        expected.add(createMetadata("0004", "fr_phone_country"));
        expected.add(createMetadata("0005", "fr_phone_region"));
        expected.add(createMetadata("0006", "fr_phone_geographicArea"));
        expected.add(createMetadata("0007", "fr_phone_carrierName"));
        expected.add(createMetadata("0008", "fr_phone_timezone"));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expected, row.getRowMetadata().getColumns());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING, US_PHONE)));
        assertTrue(action.acceptField(getColumn(Type.STRING, UK_PHONE)));
        assertTrue(action.acceptField(getColumn(Type.STRING, DE_PHONE)));
        assertTrue(action.acceptField(getColumn(Type.STRING, FR_PHONE)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.NEED_STATISTICS_INVALID));
    }

}
