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
public class RoundCeilTest extends AbstractRoundTest {

    /** The action ton test. */
    @Autowired
    private RoundCeil action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(RoundCeilTest.class.getResourceAsStream("ceilAction.json"));
    }

    @Test
    public void testName() {
        assertEquals(RoundCeil.ACTION_NAME, action.getName());
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
        testCommon("5.1", "6");
        testCommon("5.5", "6");
        testCommon("5.8", "6");
    }

    @Test
    public void testNegative() {
        testCommon("-5.0", "-5");
        testCommon("-5.4", "-5");
        testCommon("-5.6", "-5");
    }

    @Test
    public void testAltFormat() {
        testCommon("-5 000.2", "-5000");
        testCommon("5,4", "6");
        testCommon("1.200,45", "1201");
    }

    @Test
    public void test_huge_number() {
        testCommon("1234567890.1", "1234567891");
        testCommon("891234567897.9", "891234567898");
        testCommon("891234567899.9", "891234567900");
        testCommon("999999999999.9", "1000000000000");
    }

    @Test
    public void test_huge_number_negative() {
        testCommon("-1234567890.1", "-1234567890");
        testCommon("-891234567897.9", "-891234567897");
        testCommon("-891234567899.9", "-891234567899");
        testCommon("-999999999999.9", "-999999999999");
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
