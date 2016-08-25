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

package org.talend.dataprep.transformation.actions.math;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.category.ActionCategory;

/**
 * Test class for RoundHalfUp action. Creates one consumer, and test it.
 *
 * @see RoundHalfUp
 */
public class RoundHalfUpTest extends AbstractRoundTest {

    /** The action ton test. */
    private RoundHalfUp action = new RoundHalfUp();

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(RoundHalfUpTest.class.getResourceAsStream("roundAction.json"));
    }

    @Test
    public void testName() {
        assertEquals(RoundHalfUp.ACTION_NAME, action.getName());
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
        testCommon("5.0", "5");
        testCommon("5.1", "5");
        testCommon("5.5", "6");
        testCommon("5.8", "6");
    }

    @Test
    public void testNegative() {
        testCommon("-5.0", "-5");
        testCommon("-5.4", "-5");
        testCommon("-5.6", "-6");
    }

    @Test
    public void test_huge_numbers_positive() {
        testCommon("131234567890.1", "131234567890");
        testCommon("131234567890.5", "131234567891");
        testCommon("131234567890.9", "131234567891");
        testCommon("89891234567897.9", "89891234567898");
        testCommon("34891234567899.9", "34891234567900");
        testCommon("678999999999999.9", "679000000000000");
    }

    @Test
    public void test_huge_numbers_negative() {
        testCommon("-131234567890.1", "-131234567890");
        testCommon("-89891234567897.9", "-89891234567898");
        testCommon("-34891234567899.9", "-34891234567900");
        testCommon("-678999999999999.9", "-679000000000000");
    }

    @Test
    public void testInteger() {
        testCommon("5", "5");
        testCommon("-5", "-5");
    }

    @Test
    public void testString() {
        testCommon("tagada", "tagada");
        testCommon("", "");
        testCommon("null", "null");
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.INTEGER)));
        assertTrue(action.acceptField(getColumn(Type.DOUBLE)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptField(getColumn(Type.STRING)));
        assertFalse(action.acceptField(getColumn(Type.DATE)));
        assertFalse(action.acceptField(getColumn(Type.BOOLEAN)));
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
