package org.talend.dataprep.number;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Main goal of this class is to provide BigDecimal instance from a String.
 */
public class BigDecimalFormatter {

    private BigDecimalFormatter() {
    }

    public static String format(BigDecimal bd, DecimalFormat format) {
        return format.format(bd).trim();
    }

}
