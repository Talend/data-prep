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

package org.talend.dataprep.transformation.api.action.metadata.datamasking;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.setStatistics;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataquality.datamasking.semantic.MaskableCategoryEnum;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

/**
 * Test class for MaskDataByDomain action.
 *
 * @see MaskDataByDomain
 */
public class MaskDataByDomainTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    private MaskDataByDomain maskDataByDomain;

    private Map<String, String> parameters;

    private static final Logger LOGGER = LoggerFactory.getLogger(MaskDataByDomainTest.class);

    @Before
    public void init() throws IOException {
        maskDataByDomain = new MaskDataByDomain();
        parameters = ActionMetadataTestUtils
                .parseParameters(MaskDataByDomainTest.class.getResourceAsStream("maskDataByDomainAction.json"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(maskDataByDomain.getCategory(), is(ActionCategory.DATA_MASKING.getDisplayName()));
    }

    @Test
    public void testShouldMaskDatetime() throws IOException {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "2015-09-15");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata colMeta = rowMetadata.getById("0000");
        colMeta.setType(Type.DATE.getName());
        setStatistics(row, "0000", MaskDataByDomainTest.class.getResourceAsStream("statistics_datetime.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(maskDataByDomain, parameters));

        // then
        final String resultValue = row.values().get("0000").toString();
        assertTrue("The result [" + resultValue + "] should be a masked date but actually not.",
                resultValue.matches("^2015\\-[0-1][0-9]\\-[0-3][0-9]$"));
    }

    @Test
    public void testShouldMaskEmail() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "azerty@talend.com");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata colMeta = rowMetadata.getById("0000");
        colMeta.setDomain(SemanticCategoryEnum.EMAIL.name());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "XXXXXX@talend.com");

        // when
        final Action action = factory.create(maskDataByDomain, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        action.getRowAction().compile(context);
        action.getRowAction().apply(row, context);

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testShouldMaskInteger() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "12");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata colMeta = rowMetadata.getById("0000");
        colMeta.setType(Type.INTEGER.getName());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(maskDataByDomain, parameters));

        // then
        int realValueAsInteger = Integer.parseInt((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsInteger);
        assertTrue(realValueAsInteger >= 10 && realValueAsInteger <= 14);
    }

    @Test
    public void testShouldMaskDecimal_well_typed() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "12.5");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata colMeta = rowMetadata.getById("0000");
        colMeta.setType(Type.FLOAT.getName());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(maskDataByDomain, parameters));

        // then
        float realValueAsFloat = Float.parseFloat((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsFloat);
        assertTrue(realValueAsFloat >= 10 && realValueAsFloat <= 14);
    }

    @Test
    public void testShouldMaskDecimal_wrongly_typed() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "12.5");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata colMeta = rowMetadata.getById("0000");
        colMeta.setType(Type.INTEGER.getName());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(maskDataByDomain, parameters));

        // then
        float realValueAsFloat = Float.parseFloat((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsFloat);
        assertTrue(realValueAsFloat >= 10 && realValueAsFloat <= 14);
    }

    @Test
    public void testShouldMaskInteger_wrongly_typed() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "12");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata colMeta = rowMetadata.getById("0000");
        colMeta.setType(Type.FLOAT.getName());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(maskDataByDomain, parameters));

        // then
        int realValueAsInteger = Integer.parseInt((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsInteger);
        assertTrue(realValueAsInteger >= 10 && realValueAsInteger <= 14);
    }

    @Test
    public void testShouldIgnoreEmpty() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", " ");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata colMeta = rowMetadata.getById("0000");
        colMeta.setDomain(MaskableCategoryEnum.EMAIL.name());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", " ");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(maskDataByDomain, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }


    @Test
    public void testShouldUseDefaultMaskingForInvalid() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "bla bla");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata colMeta = rowMetadata.getById("0000");
        colMeta.setDomain(MaskableCategoryEnum.EMAIL.name());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "XXXXXXX");

        // when
        final Action action = factory.create(maskDataByDomain, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        action.getRowAction().compile(context);
        action.getRowAction().apply(row, context);

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testShouldNotMaskUnsupportedDataType() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "azerty@talend.com");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata colMeta = rowMetadata.getById("0000");
        colMeta.setType(Type.ANY.getName());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "azerty@talend.com");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(maskDataByDomain, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_accept_column() {
        assertTrue(maskDataByDomain.acceptColumn(getColumn(Type.STRING)));
        assertTrue(maskDataByDomain.acceptColumn(getColumn(Type.DATE)));
        assertTrue(maskDataByDomain.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(maskDataByDomain.acceptColumn(getColumn(Type.INTEGER)));
        assertTrue(maskDataByDomain.acceptColumn(getColumn(Type.FLOAT)));
        assertTrue(maskDataByDomain.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(maskDataByDomain.acceptColumn(getColumn(Type.ANY)));
    }
}
