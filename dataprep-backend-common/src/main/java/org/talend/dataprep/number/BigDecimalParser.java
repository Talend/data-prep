package org.talend.dataprep.number;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main goal of this class is to provide BigDecimal instance from a String.
 */
public class BigDecimalParser {

    /**
     * Patterns used to check different cases in guessSeparators(String):
     */
    private static final Pattern ENDS_BY_NOT_3_DIGITS_PATTERN = Pattern.compile("^[(-]?\\d+([,.])(?:\\d{0,2}|\\d{4,})[)]?");

    private static final Pattern STARTS_WITH_DECIMAL_SEPARATOR_PATTERN = Pattern
            .compile("^[(-]?(?:\\d{3,}|\\d{0})([,.])\\d+[)]?");

    private static final Pattern FEW_GROUP_SEP_PATTERN = Pattern.compile("^[(-]?\\d+([., ]\\d{3}){2,}[)]?");

    private static final Pattern TWO_DIFFERENT_SEPARATORS_PATTERN = Pattern.compile(".*\\d+([. ])\\d+[,]\\d+[)]?");

    public static DecimalFormat US_DECIMAL_PATTERN = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.US));

    public static DecimalFormat US_DECIMAL_PATTERN_ALT = new DecimalFormat("#,##0.##;(#)",
            DecimalFormatSymbols.getInstance(Locale.US));

    public static DecimalFormat EU_DECIMAL_PATTERN = new DecimalFormat("#,##0.##",
            DecimalFormatSymbols.getInstance(Locale.FRENCH));

    public static DecimalFormat EU_SCIENTIFIC_DECIMAL_PATTERN = new DecimalFormat("0.###E0", DecimalFormatSymbols.getInstance(Locale.FRENCH));

    public static DecimalFormat US_SCIENTIFIC_DECIMAL_PATTERN = new DecimalFormat("0.###E0", DecimalFormatSymbols.getInstance(Locale.US));

    private BigDecimalParser() {
    }

    /**
     * Parse the given sting to a BigDecimal with default BigDecimal(String) constructor.
     * <p>
     * This is useful when the number is standard US format (decimal separator='.' and grouping separator in {'', ',', '
     * '}) and for scientific notation.
     *
     * @param from string to convert to BigDecimal
     * @return an instance of BigDecimal
     * @throws ParseException if <code>from</code> can not be parsed as a number or if <code>from</code> is
     * <code>null</code>.
     */
    public static BigDecimal toBigDecimal(String from) throws ParseException {
        if (from == null) {
            throw new ParseException("null is not a valid number", 0);
        }
        final DecimalFormatSymbols decimalFormatSymbols = guessSeparators(from);
        return toBigDecimal(from, decimalFormatSymbols.getDecimalSeparator(), decimalFormatSymbols.getGroupingSeparator());
    }

    /**
     * Parse the given string to a BigDecimal with decimal separator explicitly defined.
     * <p>
     * Useful only when decimal separator is different than '.' or grouping separator is different than {'', ',' }.
     *
     * @param from string to convert to BigDecimal
     * @param decimalSeparator the character used for decimal sign
     * @param groupingSeparator the grouping separator
     * @return an instance of BigDecimal
     * @throws ParseException if <code>from</code> can not be parsed as a number with the given separators
     */
    public static BigDecimal toBigDecimal(String from, char decimalSeparator, char groupingSeparator) throws ParseException {
        if (from == null) {
            throw new ParseException("null is not a valid number", 0);
        }
        // Remove grouping separators:
        from = from.replaceAll("[" + groupingSeparator + "]", "");

        // Replace decimal separator:
        from = from.replaceAll("[" + decimalSeparator + "]", ".");

        // Remove spaces:
        from = from.replaceAll(" ", "");

        try {
            return new BigDecimal(from);
        } catch (NumberFormatException e) {
            for (DecimalFormat format : new DecimalFormat[] { US_DECIMAL_PATTERN, US_DECIMAL_PATTERN_ALT }) {
                try {
                    return toBigDecimal(format.parse(from));
                } catch (ParseException e1) {
                    // nothing to do, just test next format
                }
            }
            throw new ParseException("'" + from + "' can not parsed as a number", 0);
        }
    }

    protected static DecimalFormatSymbols guessSeparators(String from) {
        final DecimalFormatSymbols toReturn = DecimalFormatSymbols.getInstance(Locale.US);

        /*
         * This part checks cases where two separators are present. In this case, the first one is probably the grouping
         * separator, and the second the decimal separator.
         *
         * Like in 1.254.789,45 or 1 254 789.45
         */
        Matcher matcher = TWO_DIFFERENT_SEPARATORS_PATTERN.matcher(from);
        if (matcher.matches()) {
            toReturn.setDecimalSeparator(',');
            toReturn.setGroupingSeparator(matcher.group(1).charAt(0));
        }

        /*
         * This part checks cases where there is one separator, following by not 3 digits (less or more). In this case,
         * it's probably a decimal separator. Like in 12,3456 or 12,34
         */
        matcher = ENDS_BY_NOT_3_DIGITS_PATTERN.matcher(from);
        if (matcher.matches()) {
            String firstMatchingGroup = matcher.group(1);
            final char decimalSeparator = firstMatchingGroup.charAt(0);
            toReturn.setDecimalSeparator(decimalSeparator);
            toReturn.setGroupingSeparator(inferGroupingSeparator(decimalSeparator));
        }

        /*
         * This part checks 2 cases: - where value starts with a separator. In this case, it's probably a decimal
         * separator. Like in .254 or ,888 - where value starts with more than 3 digits then a separator. In this case,
         * it's probably a decimal separator. Like in 1234.24 or 1234,888
         */
        matcher = STARTS_WITH_DECIMAL_SEPARATOR_PATTERN.matcher(from);
        if (matcher.matches()) {
            String firstMatchingGroup = matcher.group(1);
            final char decimalSeparator = firstMatchingGroup.charAt(0);
            toReturn.setDecimalSeparator(decimalSeparator);
            toReturn.setGroupingSeparator(inferGroupingSeparator(decimalSeparator));
        }

        /*
         * This part checks cases where a single separator is present, but many times. In this case, it's probably a
         * grouping separator.
         *
         * Like in 2.452.254 or 1 454 888
         */
        matcher = FEW_GROUP_SEP_PATTERN.matcher(from);
        if (matcher.matches()) {
            String firstMatchingGroup = matcher.group(1);
            final char groupingSeparator = firstMatchingGroup.charAt(0);
            toReturn.setGroupingSeparator(groupingSeparator);
            toReturn.setDecimalSeparator(inferDecimalSeparator(groupingSeparator));
        }

        return toReturn;
    }

    /**
     * Infers the probable decimal separator given a grouping separator.
     * <p>
     * To use when you've guess a probable grouping separator but no clue about a decimal separator (like in a integer
     * for example).
     * <p>
     * Its based on the hypothesis that if we have standard EU grouping separator, it returns standard EU decimal
     * separator, standard US decimal separator otherwise.
     */
    private static char inferDecimalSeparator(char groupingSeparator) {
        switch (groupingSeparator) {
        case '.':
            return ',';
        default:
            return '.';
        }
    }

    /**
     * Infers the probable grouping separator given a decimal separator.
     * <p>
     * To use when you've guess a probable decimal separator but no clue about a grouping separator (not group, or less
     * than 3 digits in integer part).
     * <p>
     * Its based on the hypothesis that if we have standard US decimal separator, it returns standard US grouping
     * separator, standard EU possible decimal separator otherwise.
     */
    private static char inferGroupingSeparator(char decimalSeparator) {
        switch (decimalSeparator) {
        case '.':
            return ',';
        default:
            return '.';
        }
    }

    /**
     * Basic implementation to get a BigDecimal instance from a Number instance WITHOUT precision lost.
     */
    private static BigDecimal toBigDecimal(Number number) {
        return new BigDecimal(number.toString());
    }

}
