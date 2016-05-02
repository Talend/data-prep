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

package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for RoundCeil action. Creates one consumer, and test it.
 *
 * @see RoundCeil
 */
public class RoundDownTest extends AbstractRoundTest {

    /** The action ton test. */
    @Autowired
    private RoundDownReal action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(RoundDownTest.class.getResourceAsStream("roundDownAction.json"));
    }

    @Test
    public void testName() {
        assertEquals(RoundDownReal.ACTION_NAME, action.getName());
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.NUMBERS.getDisplayName()));
    }


    @Test
    public void testPositive() {
        testCommon("5.0", "5", 0);
        testCommon("5.1", "5", 0);
        testCommon("5.5", "5", 0);
        testCommon("5.8", "5", 0);

        testCommon("5.0", "5.0", 1);
        testCommon("5.1", "5.1", 1);
        testCommon("5.5", "5.5", 1);
        testCommon("5.8", "5.8", 1);

        testCommon("5.0", "5.00", 2);
        testCommon("5.1", "5.10", 2);
        testCommon("5.5", "5.50", 2);
        testCommon("5.8", "5.80", 2);

        testCommon("5.0", "5", -2);
        testCommon("5.1", "5", -2);
        testCommon("5.5", "5", -2);
        testCommon("5.8", "5", -2);
    }

    @Test
    public void testNegative() {
        testCommon("-5.0", "-5", 0);
        testCommon("-5.4", "-5", 0);
        testCommon("-5.6", "-5", 0);

        testCommon("-5.0", "-5.0", 1);
        testCommon("-5.4", "-5.4", 1);
        testCommon("-5.6", "-5.6", 1);

        testCommon("-5.00", "-5.0", 1);
        testCommon("-5.45", "-5.4", 1);
        testCommon("-5.63", "-5.6", 1);
    }

    @Test
    public void test_huge_numbers_positive() {
        testCommon("131234567890.1", "131234567890", 0);
        testCommon("89891234567897.9", "89891234567897", 0);
        testCommon("34891234567899.9", "34891234567899", 0);
        testCommon("678999999999999.9", "678999999999999", 0);
    }

    @Test
    public void test_huge_numbers_negative() {
        testCommon("-131234567890.1", "-131234567890", 0);
        testCommon("-89891234567897.9", "-89891234567897", 0);
        testCommon("-34891234567899.9", "-34891234567899", 0);
        testCommon("-678999999999999.9", "-678999999999999", 0);
    }

    @Test
    public void testInteger() {
        testCommon("5", "5", 0);
        testCommon("-5", "-5", 0);

        testCommon("5", "5.0", 1);
        testCommon("-5", "-5.0", 1);
    }

    @Test
    public void testString() {
        for (int precision = 0; precision <= 5; precision++) {
            testCommon("tagada", "tagada", precision);
            testCommon("", "", precision);
            testCommon("null", "null", precision);
        }
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptColumn(getColumn(Type.INTEGER)));
        assertTrue(action.acceptColumn(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptColumn(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    @Override
    protected AbstractRound getAction() {
        return action;
    }

    @Override
    protected Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    protected List<String> getExpectedParametersName() {
        return Arrays.asList("column_id", "row_id", "scope", "filter", "precision");
    }

}
