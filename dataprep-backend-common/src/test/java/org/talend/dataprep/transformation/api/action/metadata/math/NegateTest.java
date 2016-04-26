//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.junit.Assert.assertEquals;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;

/**
 * Unit test for the Negate action.
 *
 * @see Negate
 */
public class NegateTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private Negate action;

    /** The action parameters. */
    private Map<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = NegateTest.class.getResourceAsStream("negateAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void negate_with_positive() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("-5.0", row.get("0003"));
    }

    @Test
    public void negate_with_negative() {
        // given
        DataSetRow row = getRow("-5", "3", "Done !");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("5.0", row.get("0003"));
    }

    @Test
    public void negate_with_NaN() {
        // given
        DataSetRow row = getRow("beer", "3", "Done !");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    private void assertColumnWithResultCreated(DataSetRow row) {
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_negate").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

}