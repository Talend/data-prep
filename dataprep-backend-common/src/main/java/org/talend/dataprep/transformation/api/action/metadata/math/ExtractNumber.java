// ============================================================================
//
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
package org.talend.dataprep.transformation.api.action.metadata.math;

import java.math.BigDecimal;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * This will extract the numeric part
 *
 * We use metric prefix from <a href="https://en.wikipedia.org/wiki/Metric_prefix">Wikipedia</a>
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
@Component(ActionMetadata.ACTION_BEAN_PREFIX + ExtractNumber.EXTRACT_NUMBER_ACTION_NAME)
public class ExtractNumber extends ActionMetadata implements ColumnAction {

    /** Name of the action. */
    public static final String EXTRACT_NUMBER_ACTION_NAME = "extract_number"; //$NON-NLS-1$

    /** Default result if the input is not a number. */
    private static final String DEFAULT_RESULT = "0";

    public static String DECIMAL_SEPARATOR = "decimal_separator"; //$NON-NLS-1$

    public static String DOT = ".";

    public static String COMMA = ",";

    /** The maximum fraction digits displayed in the output. */
    private static final int MAX_FRACTION_DIGITS_DISPLAY = 30;

    /** List of supported separators. */
    private static final List<Character> SEPARATORS =Arrays.asList('.', ',');

    /** K: the prefix, V: the value. */
    private Map<String, MetricPrefix> metricPrefixes = new HashMap<>(13);

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
    @PostConstruct
    public void initialize() {
        metricPrefixes.put("T", new MetricPrefix(new BigDecimal("1000000000000"), "tera"));
        metricPrefixes.put("G", new MetricPrefix(new BigDecimal("1000000000"), "giga"));
        metricPrefixes.put("M", new MetricPrefix(new BigDecimal("1000000"), "mega"));
        metricPrefixes.put("k", new MetricPrefix(new BigDecimal("1000"), "kilo"));
        metricPrefixes.put("h", new MetricPrefix(new BigDecimal("100"), "hecto"));
        metricPrefixes.put("da", new MetricPrefix(new BigDecimal("10"), "deca"));
        metricPrefixes.put("d", new MetricPrefix(new BigDecimal("0.1"), "deci"));
        metricPrefixes.put("c", new MetricPrefix(new BigDecimal("0.01"), "centi"));
        metricPrefixes.put("m", new MetricPrefix(new BigDecimal("0.001"), "milli"));
        metricPrefixes.put("μ", new MetricPrefix(new BigDecimal("0.000001"), "micro"));
        metricPrefixes.put("n", new MetricPrefix(new BigDecimal("0.000000001"), "nano"));
        metricPrefixes.put("p", new MetricPrefix(new BigDecimal("0.000000000001"), "pico"));
    }

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return EXTRACT_NUMBER_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.SPLIT.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    /**
     * @see ActionMetadata#compile(ActionContext)
     */
    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {

            String columnId = context.getColumnId();
            RowMetadata rowMetadata = context.getRowMetadata();
            ColumnMetadata column = rowMetadata.getById(columnId);

            // create new column and append it after current column
            context.column("result", r -> {
                ColumnMetadata c = ColumnMetadata.Builder //
                    .column() //
                    .name(column.getName() + "_number") //
                    .type(Type.NUMERIC) //
                    .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });
        }
    }


    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String newColumnId = context.column("result");
        row.set(newColumnId, extractNumber(row.get(columnId)));
    }

    /**
     * @param value the value to parse.
     * @return the number extracted out of the given value.
     */
    private String extractNumber(String value) {

        // easy case
        if (StringUtils.isEmpty(value)) {
            return DEFAULT_RESULT;
        }

        // Test if the input value is a valid number before removing any characters:
        try {
            BigDecimalParser.toBigDecimal(value);
            // If yes (no exception thrown), return the value as it, no change required:
            return value;
        } catch (NumberFormatException e) {
            // If no, continue the process to remove non-numeric chars:
        }

        StringCharacterIterator iter = new StringCharacterIterator(value);

        MetricPrefix metricPrefixBefore = null,  metricPrefixAfter= null;
        
        boolean numberFound = false;

        // we build a new value including only number or separator as , or .
        StringBuilder reducedValue = new StringBuilder( value.length() );

        for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
            // we remove all non numeric characters but keep separators
            if (NumberUtils.isNumber(String.valueOf(c)) || SEPARATORS.contains(c)) {
                reducedValue.append(c);
                numberFound = true;
            } else {
                // we take the first metric prefix found before and after a number found
                if (metricPrefixBefore == null) {
                    MetricPrefix found = metricPrefixes.get(String.valueOf(c));
                    if (found != null && !numberFound) {
                        metricPrefixBefore = found;
                    }
                }
                if (metricPrefixAfter == null) {
                    MetricPrefix found = metricPrefixes.get(String.valueOf(c));
                    if (found != null && numberFound) {
                        metricPrefixAfter = found;
                    }
                }

            }
        }

        BigDecimal bigDecimal;
        try {
            bigDecimal = BigDecimalParser.toBigDecimal(reducedValue.toString());
        } catch (NumberFormatException e) {
            return DEFAULT_RESULT;
        }

        if (metricPrefixBefore != null || metricPrefixAfter != null) {
            // the metrix found after use first
            MetricPrefix metricPrefix = metricPrefixAfter != null ? metricPrefixAfter : metricPrefixBefore;
            bigDecimal = bigDecimal.multiply(metricPrefix.getMultiply());
        }

        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        decimalFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS_DISPLAY);
        return decimalFormat.format(bigDecimal.stripTrailingZeros());
    }


    /**
     * Internal class that models a Metric, e.g. kilo -> multiply by 1000
     */
    static class MetricPrefix {

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
