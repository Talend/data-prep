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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.text.LowerCase;
import org.talend.dataquality.datamasking.semantic.MaskableCategoryEnum;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

/**
 * Test class for LowerCase action. Creates one consumer, and test it.
 *
 * @see LowerCase
 */
public class MaskDataByDomainTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    private MaskDataByDomain action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new MaskDataByDomain();
        parameters = ActionMetadataTestUtils
                .parseParameters(MaskDataByDomainTest.class.getResourceAsStream("maskDataByDomainAction.json"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATA_MASKING.getDisplayName()));
    }

    @Test
    public void testShouldMask() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "azerty@talend.com");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setDomain(MaskableCategoryEnum.EMAIL.name());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "XXXXXX@talend.com");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testShouldNotMaskUnsupportedDataType() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "azerty@talend.com");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setType(Type.ANY.getName());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "azerty@talend.com");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_accept_column() {
        for (MaskableCategoryEnum maskableCat : MaskableCategoryEnum.values()) {
            SemanticCategoryEnum semanticCat = SemanticCategoryEnum.getCategoryById(maskableCat.name());
            if (semanticCat != null) {
                assertTrue(action.acceptColumn(getColumn(Type.STRING, semanticCat)));
            }
        }
        assertTrue(action.acceptColumn(getColumn(Type.DATE)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptColumn(getColumn(Type.ANY)));
    }
}
