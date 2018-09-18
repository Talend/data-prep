// ============================================================================
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

package org.talend.dataprep.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NumericHelperTest {

    @Test
    public void shouldReturnTrueOnNumeric() throws Exception {
        assertTrue(NumericHelper.isBigDecimal("6"));
        assertTrue(NumericHelper.isBigDecimal("6.0"));
        assertTrue(NumericHelper.isBigDecimal("6,0"));
        assertTrue(NumericHelper.isBigDecimal("6000"));
        assertTrue(NumericHelper.isBigDecimal("6 000"));
        assertTrue(NumericHelper.isBigDecimal("6'000"));
        assertTrue(NumericHelper.isBigDecimal("6E3"));
        assertTrue(NumericHelper.isBigDecimal("6e3"));
        assertTrue(NumericHelper.isBigDecimal(".6"));
        assertTrue(NumericHelper.isBigDecimal(",6"));
        assertTrue(NumericHelper.isBigDecimal("(6)"));
        assertTrue(NumericHelper.isBigDecimal("6%"));

        // TODO : should be false
        assertTrue(NumericHelper.isBigDecimal("5%55"));
    }

    @Test
    public void shouldReturnFalseOnNonNumeric() throws Exception {
        assertFalse(NumericHelper.isBigDecimal("a"));
        assertFalse(NumericHelper.isBigDecimal(""));
        assertFalse(NumericHelper.isBigDecimal(" "));
        assertFalse(NumericHelper.isBigDecimal(null));
        assertFalse(NumericHelper.isBigDecimal("6aaa"));
        assertFalse(NumericHelper.isBigDecimal("6 aaa"));
    }
}
