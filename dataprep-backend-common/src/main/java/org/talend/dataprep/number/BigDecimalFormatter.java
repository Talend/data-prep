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

import java.math.BigDecimal;
import java.text.DecimalFormat;

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
