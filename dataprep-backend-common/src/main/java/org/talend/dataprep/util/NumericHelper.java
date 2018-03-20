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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.AbstractFormatValidator;
import org.apache.commons.validator.routines.BigDecimalValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.number.BigDecimalParser;

/**
 * A helper to help checking if a string is a number.
 */
public class NumericHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NumericHelper.class);

    private static final Locale[] LOCALES = { Locale.FRENCH, Locale.ENGLISH };

    private static final char[] ALLOWED_NUMERIC_CHARACTERS = new char[]{',', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '+', 'E', 'e', ' ', '\'', '%'};

    private NumericHelper() {
    }

    private static boolean isValid(String str, AbstractFormatValidator validator) {
        for (Locale locale : LOCALES) {
            if (validator.isValid(str, locale)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether <code>str</code> can be parsed by {@link BigDecimalParser} without throwing an exception.
     * @param str The string to be tested, can be <code>null</code> or empty.
     * @return <code>true</code> if string can be parsed by {@link BigDecimalParser}, <code>false</code> otherwise.
     */
    public static boolean isBigDecimal(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        // Check for (nnnn) values (negative values in accounting).
        String strForValidation = StringUtils.remove(str, ' ');
        if (StringUtils.isEmpty(strForValidation)) {
            return false;
        }
        if (strForValidation.lastIndexOf('(') == 0 && strForValidation.lastIndexOf(')') == strForValidation.length() - 1) {
            strForValidation = strForValidation.substring(1, strForValidation.length() - 1); // Keep only nnnn
        }

        if (!StringUtils.containsOnly(strForValidation, ALLOWED_NUMERIC_CHARACTERS) && !isValid(strForValidation, new BigDecimalValidator())) {
            return false;
        }

        // Support for values that starts with ',' or '.' (like .5 or ,5).
        if (strForValidation.charAt(0) == ',' || strForValidation.charAt(0) == '.') {
            return true;
        }

        // Try custom decimal formats
        DecimalFormat[] supportedFormats = { BigDecimalParser.EU_DECIMAL_PATTERN,
                BigDecimalParser.EU_SCIENTIFIC_DECIMAL_PATTERN,
                BigDecimalParser.US_DECIMAL_PATTERN,
                BigDecimalParser.US_SCIENTIFIC_DECIMAL_PATTERN
        };
        for (DecimalFormat supportedFormat : supportedFormats) {
            try {
                if (supportedFormat.parse(strForValidation) != null) {
                    return true;
                }
            } catch (ParseException e) {
                LOGGER.debug("Unable to parse '{}' using custom decimal format '{}'.", strForValidation, supportedFormat.toPattern(), e);
            }
        }

        return false;
    }

}
