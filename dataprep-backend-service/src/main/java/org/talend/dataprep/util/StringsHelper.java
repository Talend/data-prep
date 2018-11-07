package org.talend.dataprep.util;

import java.text.Normalizer;

import org.apache.commons.lang3.StringUtils;

public class StringsHelper {

    public static boolean match(final String reference, final String value, final boolean strict) {
        return strict ? StringUtils.equalsIgnoreCase(reference, value)
                : StringUtils.containsIgnoreCase(reference, value);
    }

    /**
     * Normalize string to NFC: Normalization Form Canonical Composition.
     * See https://en.wikipedia.org/wiki/Unicode_equivalence#Normal_forms
     * @param s The string to normalize.
     * @return The normalized form of the string.
     */
    public static String normalizeString(String s) {
        if (!Normalizer.isNormalized(s, Normalizer.Form.NFC)) {
            return Normalizer.normalize(s, Normalizer.Form.NFC);
        } else {
            return s;
        }
    }
}
