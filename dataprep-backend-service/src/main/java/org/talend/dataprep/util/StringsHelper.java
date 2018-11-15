package org.talend.dataprep.util;

import java.text.Normalizer;

import org.apache.commons.lang3.StringUtils;

public class StringsHelper {

    public static boolean match(final String reference, final String value, final boolean strict) {
        return strict ? StringUtils.equalsIgnoreCase(reference, value)
                : StringUtils.containsIgnoreCase(reference, value);
    }

    /**
     * Normalize string to NFC.
     * @see <a href="https://en.wikipedia.org/wiki/Unicode_equivalence#Normal_forms">Normalization Form Canonical Composition</a>
     * @param s The string to normalize.
     * @return The normalized String with {@link Normalizer.Form#NFC} normalization form.
     */
    public static String normalizeString(String s) {
        if (!Normalizer.isNormalized(s, Normalizer.Form.NFC)) {
            return Normalizer.normalize(s, Normalizer.Form.NFC);
        } else {
            return s;
        }
    }
}
