package org.talend.dataprep.util;

import org.apache.commons.lang3.StringUtils;

public class StringsHelper {

    public static boolean match(final String reference, final String value, final boolean strict) {
        return strict ? StringUtils.equalsIgnoreCase(reference, value)
                : StringUtils.containsIgnoreCase(reference, value);
    }
}
