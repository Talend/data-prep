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

package org.talend.dataprep.number;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

public class BigDecimalFormatterTest {

    @Test
    public void testToBigDecimal_US() throws Exception {
        assertEquals("12.5", BigDecimalFormatter.format(new BigDecimal(12.50), BigDecimalParser.US_DECIMAL_PATTERN));
        assertEquals("12", BigDecimalFormatter.format(new BigDecimal(12), BigDecimalParser.US_DECIMAL_PATTERN));
        assertEquals("12.58", BigDecimalFormatter.format(new BigDecimal(12.57708), BigDecimalParser.US_DECIMAL_PATTERN));
    }

    @Test
    public void testToBigDecimal_US_grouping() throws Exception {
        assertEquals("4,512.5", BigDecimalFormatter.format(new BigDecimal(4512.50), BigDecimalParser.US_DECIMAL_PATTERN));
    }

    @Test
    public void testToBigDecimal_US_negative() throws Exception {
        assertEquals("-12.5", BigDecimalFormatter.format(new BigDecimal(-12.5), BigDecimalParser.US_DECIMAL_PATTERN));
    }

    @Test
    public void testToBigDecimal_EU() throws Exception {
        assertEquals("12,5", BigDecimalFormatter.format(new BigDecimal(12.50), BigDecimalParser.EU_DECIMAL_PATTERN));
        assertEquals("12", BigDecimalFormatter.format(new BigDecimal(12), BigDecimalParser.EU_DECIMAL_PATTERN));
        assertEquals("12,58", BigDecimalFormatter.format(new BigDecimal(12.57708), BigDecimalParser.EU_DECIMAL_PATTERN));
    }

    @Test
    public void testToBigDecimal_EU_grouping() throws Exception {
        assertEquals("4" + ((char) 160) + "512,5", BigDecimalFormatter.format(new BigDecimal(4512.50), BigDecimalParser.EU_DECIMAL_PATTERN));
    }

    @Test
    public void testToBigDecimal_EU_negative() throws Exception {
        assertEquals("-12,5", BigDecimalFormatter.format(new BigDecimal(-12.5), BigDecimalParser.EU_DECIMAL_PATTERN));
    }

    @Test
    public void testToBigDecimal_US_scientific() throws Exception {
        assertEquals("1.216E3", BigDecimalFormatter.format(new BigDecimal(1215.50), BigDecimalParser.US_SCIENTIFIC_DECIMAL_PATTERN));
    }

    @Test
    public void testToBigDecimal_EU_scientific() throws Exception {
        assertEquals("1,216E3", BigDecimalFormatter.format(new BigDecimal(1215.50), BigDecimalParser.EU_SCIENTIFIC_DECIMAL_PATTERN));
    }

}
