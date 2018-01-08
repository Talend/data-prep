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

package org.talend.dataprep.transformation.actions.datamasking;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValueBuilder.value;
import static org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest.ValuesBuilder.builder;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataquality.datamasking.semantic.MaskableCategoryEnum;

/**
 * Test class for MaskDataByDomain action.
 *
 * @see MaskDataByDomain
 */
public class MaskDataByDomainTest extends AbstractMetadataBaseTest<MaskDataByDomain> {

    private Map<String, String> parameters;

    private static final Logger LOGGER = LoggerFactory.getLogger(MaskDataByDomainTest.class);

    public MaskDataByDomainTest() {
        super(new MaskDataByDomain());
    }

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(MaskDataByDomainTest.class.getResourceAsStream("maskDataByDomainAction.json"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(Locale.US), is(ActionCategory.DATA_MASKING.getDisplayName(Locale.US)));
    }

    @Override
    public CreateNewColumnPolicy getCreateNewColumnPolicy() {
        return CreateNewColumnPolicy.INVISIBLE_DISABLED;
    }

    @Test
    public void testShouldMaskDatetime() throws IOException {

        // given
        final DataSetRow row = builder() //
                .with(value("2015-09-15") //
                        .type(Type.DATE) //
                        .statistics(MaskDataByDomainTest.class.getResourceAsStream("statistics_datetime.json"))
                ) //
                .build();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final String resultValue = row.values().get("0000").toString();
        assertTrue("The result [" + resultValue + "] should be a masked date but actually not.",
                resultValue.matches("^2015\\-[0-1][0-9]\\-[0-3][0-9]$"));
    }

    @Test
    public void test_apply_in_newcolumn() throws Exception {
        // Nothing to test, this action is never applied in place
    }

    @Test
    public void test_apply_inplace() {

        // given
        final DataSetRow row = builder() //
                .with(value("azerty@talend.com").type(Type.STRING).domain(MaskableCategoryEnum.EMAIL.name())) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "XXXXXX@talend.com");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testShouldMaskInteger() {
        // given
        final DataSetRow row = builder() //
                .with(value("12").type(Type.INTEGER)) //
                .build();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        int realValueAsInteger = Integer.parseInt((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsInteger);
        assertTrue(realValueAsInteger >= 10 && realValueAsInteger <= 14);
    }

    @Test
    public void testShouldMaskDecimal_well_typed() {
        // given
        final DataSetRow row = builder() //
                .with(value("12.5").type(Type.FLOAT)) //
                .build();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        float realValueAsFloat = Float.parseFloat((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsFloat);
        assertTrue(realValueAsFloat >= 10 && realValueAsFloat <= 14);
    }

    @Test
    public void testShouldMaskDecimal_wrongly_typed() {
        // given
        final DataSetRow row = builder() //
                .with(value("12.5").type(Type.INTEGER)) //
                .build();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        float realValueAsFloat = Float.parseFloat((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsFloat);
        assertTrue(realValueAsFloat >= 10 && realValueAsFloat <= 14);
    }

    @Test
    public void testShouldMaskInteger_wrongly_typed() {
        // given
        final DataSetRow row = builder() //
                .with(value("12").type(Type.FLOAT)) //
                .build();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        int realValueAsInteger = Integer.parseInt((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsInteger);
        assertTrue(realValueAsInteger >= 10 && realValueAsInteger <= 14);
    }

    @Test
    public void testShouldIgnoreEmpty() {

        // given
        final DataSetRow row = builder() //
                .with(value(" ").type(Type.STRING).domain(MaskableCategoryEnum.EMAIL.name())) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", " ");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }


    @Test
    public void testShouldUseDefaultMaskingForInvalid() {

        // given
        final DataSetRow row = builder() //
                .with(value("bla bla").type(Type.STRING).domain(MaskableCategoryEnum.EMAIL.name())) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "XXXXXXX");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testShouldNotMaskUnsupportedDataType() {

        // given
        final DataSetRow row = builder() //
                .with(value("azerty@talend.com").type(Type.ANY)) //
                .build();

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "azerty@talend.com");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
        assertTrue(action.acceptField(getColumn(Type.DATE)));
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(2, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.NEED_STATISTICS_INVALID));
    }

}
