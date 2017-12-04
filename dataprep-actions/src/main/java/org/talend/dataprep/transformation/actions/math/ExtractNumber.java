// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.math;

import static java.lang.String.valueOf;
import static java.text.CharacterIterator.DONE;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.math.NumberUtils.isNumber;
import static org.talend.daikon.number.BigDecimalParser.toBigDecimal;
import static org.talend.dataprep.api.action.ActionDefinition.Behavior.METADATA_CREATE_COLUMNS;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.SPLIT;
import static org.talend.dataprep.util.NumericHelper.isBigDecimal;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.StringCharacterIterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * This will extract the numeric part
 * <p>
 * We use metric prefix from <a href="https://en.wikipedia.org/wiki/Metric_prefix">Wikipedia</a>
 * <p>
 * <ul>
 * <li>tera, T, 1000000000000</li>
 * <li>giga, G, 1000000000</li>
 * <li>mega, M, 1000000</li>
 * <li>kilo, k, 1000</li>
 * <li>hecto, h, 100</li>
 * <li>deca, da, 10</li>
 * <li>(none), (none), 1</li>
 * <li>deci, d, 0.1</li>
 * <li>centi, c, 0.01</li>
 * <li>milli, m, 0.001</li>
 * <li>micro, μ, 0.000001</li>
 * <li>nano, n, 0.000000001</li>
 * <li>pico p 0.000000000001</li>
 * </ul>
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + ExtractNumber.EXTRACT_NUMBER_ACTION_NAME)
public class ExtractNumber extends AbstractActionMetadata implements ColumnAction {

    /** Name of the action. */
    public static final String EXTRACT_NUMBER_ACTION_NAME = "extract_number"; //$NON-NLS-1$

    /** Default result if the input is not a number. */
    private static final String DEFAULT_RESULT = "0";

    /** The maximum fraction digits displayed in the output. */
    private static final int MAX_FRACTION_DIGITS_DISPLAY = 30;

    /** List of supported separators. */
    private static final List<Character> SEPARATORS = asList('.', ',');

    /** K: the prefix, V: the value. */
    private static Map<String, MetricPrefix> METRICPREFIXES = new ConcurrentHashMap<>(13);

    /**
     * Initialize the supported metrics.
     *
     * <ul>
     * <li>tera, T, 1000000000000</li>
     * <li>giga, G, 1000000000</li>
     * <li>mega, M, 1000000</li>
     * <li>kilo, k, 1000</li>
     * <li>hecto, h, 100</li>
     * <li>deca, da, 10</li>
     * <li>(none), (none), 1</li>
     * <li>deci, d, 0.1</li>
     * <li>centi, c, 0.01</li>
     * <li>milli, m, 0.001</li>
     * <li>micro, μ, 0.000001</li>
     * <li>nano, n, 0.000000001</li>
     * <li>pico p 0.000000000001</li>
     * </ul>
     */
    static {
        METRICPREFIXES.put("T", new MetricPrefix(new BigDecimal("1000000000000"), "tera"));
        METRICPREFIXES.put("G", new MetricPrefix(new BigDecimal("1000000000"), "giga"));
        METRICPREFIXES.put("M", new MetricPrefix(new BigDecimal("1000000"), "mega"));
        METRICPREFIXES.put("k", new MetricPrefix(new BigDecimal("1000"), "kilo"));
        METRICPREFIXES.put("h", new MetricPrefix(new BigDecimal("100"), "hecto"));
        METRICPREFIXES.put("da", new MetricPrefix(new BigDecimal("10"), "deca"));
        METRICPREFIXES.put("d", new MetricPrefix(new BigDecimal("0.1"), "deci"));
        METRICPREFIXES.put("c", new MetricPrefix(new BigDecimal("0.01"), "centi"));
        METRICPREFIXES.put("m", new MetricPrefix(new BigDecimal("0.001"), "milli"));
        METRICPREFIXES.put("μ", new MetricPrefix(new BigDecimal("0.000001"), "micro"));
        METRICPREFIXES.put("n", new MetricPrefix(new BigDecimal("0.000000001"), "nano"));
        METRICPREFIXES.put("p", new MetricPrefix(new BigDecimal("0.000000000001"), "pico"));
    }

    private static String extractNumber(String value) {
        return extractNumber(value, DEFAULT_RESULT);
    }

    public ExtractNumber() {
    }

    /**
     * @param value        the value to parse.
     * @param defaultValue the value to return when no number can be extracted
     * @return the number extracted out of the given value.
     */
    protected static String extractNumber(String value, String defaultValue) {

        // easy case
        if (isEmpty(value)) {
            return defaultValue;
        }

        // Test if the input value is a valid number before removing any characters:
        if (isBigDecimal(value)) {
            // If yes (no exception thrown), return the value as it, no change required:
            return valueOf(toBigDecimal(value));
        }

        StringCharacterIterator iter = new StringCharacterIterator(value);

        MetricPrefix metricPrefixBefore = null, metricPrefixAfter = null;

        boolean numberFound = false;

        // we build a new value including only number or separator as , or .
        StringBuilder reducedValue = new StringBuilder(value.length());

        for (char c = iter.first(); c != DONE; c = iter.next()) {
            // we remove all non numeric characters but keep separators
            if (isNumber(valueOf(c)) || SEPARATORS.contains(c)) {
                reducedValue.append(c);
                numberFound = true;
            } else {
                // we take the first metric prefix found before and after a number found
                if (metricPrefixBefore == null) {
                    MetricPrefix found = METRICPREFIXES.get(valueOf(c));
                    if (found != null && !numberFound) {
                        metricPrefixBefore = found;
                    }
                }
                if (metricPrefixAfter == null) {
                    MetricPrefix found = METRICPREFIXES.get(valueOf(c));
                    if (found != null && numberFound) {
                        metricPrefixAfter = found;
                    }
                }

            }
        }

        if (!isBigDecimal(reducedValue.toString())) {
            return defaultValue;
        }
        BigDecimal bigDecimal = toBigDecimal(reducedValue.toString());

        if (metricPrefixBefore != null || metricPrefixAfter != null) {
            // the metrix found after use first
            MetricPrefix metricPrefix = metricPrefixAfter != null ? metricPrefixAfter : metricPrefixBefore;
            bigDecimal = bigDecimal.multiply(metricPrefix.getMultiply());
        }

        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        decimalFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS_DISPLAY);
        return decimalFormat.format(bigDecimal.stripTrailingZeros());
    }

    @Override
    public String getName() {
        return EXTRACT_NUMBER_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return SPLIT.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    protected boolean createNewColumnParamVisible() {
        return false;
    }

    @Override
    public boolean getCreateNewColumnDefaultValue() {
        return true;
    }

    @Override
    protected List<AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(new AdditionalColumn(Type.STRING, context.getColumnName() + "_number"));
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        row.set(getTargetColumnId(context), extractNumber(row.get(columnId)));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return singleton(METADATA_CREATE_COLUMNS);
    }

    /**
     * Internal class that models a Metric, e.g. kilo -> multiply by 1000
     */
    private static class MetricPrefix {

        private final String name;

        private final BigDecimal multiply;

        MetricPrefix(BigDecimal multiply, String name) {
            this.multiply = multiply;
            this.name = name;
        }

        BigDecimal getMultiply() {
            return multiply;
        }

        public String getName() {
            return name;
        }
    }
}
