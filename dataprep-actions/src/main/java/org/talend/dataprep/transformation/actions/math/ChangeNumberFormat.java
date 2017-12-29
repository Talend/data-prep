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

import static java.util.Collections.singletonList;
import static org.talend.daikon.number.BigDecimalParser.*;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.number.BigDecimalFormatter;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;

/**
 * Change the pattern on a 'number' column.
 */
@Action(ChangeNumberFormat.ACTION_NAME)
public class ChangeNumberFormat extends AbstractActionMetadata implements ColumnAction {

    /** Action name. */
    public static final String ACTION_NAME = "change_number_format"; //$NON-NLS-1$

    /** Parameter to define original decimal & grouping separators. */
    public static final String FROM_SEPARATORS = "from_separators";

    /**
     * The pattern shown to the user as a list. An item in this list is the value 'custom', which allow the user to
     * manually enter his pattern.
     */
    public static final String TARGET_PATTERN = "target_pattern"; //$NON-NLS-1$

    /**
     * Keys used in the values of different parameters:
     */
    public static final String CUSTOM = "custom";

    public static final String US_SEPARATORS = "us_separators";

    public static final String EU_SEPARATORS = "eu_separators";

    public static final String CH_SEPARATORS = "ch_separators";

    public static final String US_PATTERN = "us_pattern";

    public static final String EU_PATTERN = "eu_pattern";

    public static final String CH_PATTERN = "ch_pattern";

    public static final String SCIENTIFIC = "scientific";

    /**
     * Constants to build parameters name by concat:
     */
    public static final String FROM = "from";

    public static final String TARGET = "target";

    public static final String GROUPING = "_grouping";

    public static final String DECIMAL = "_decimal";

    public static final String SEPARATOR = "_separator";

    protected static final String NEW_COLUMN_SUFFIX = "_formatted";

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeNumberFormat.class);

    /**
     * Key to store the compiled format in action context.
     */
    private static final String COMPILED_TARGET_FORMAT = "compiled_number_format";

    private static final String UNKNOWN_SEPARATORS = "unknown_separators";

    private static final DecimalFormat CH_DECIMAL_PATTERN = new DecimalFormat("#,##0.##",
            DecimalFormatSymbols.getInstance(new Locale("FR", "CH")));

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.NUMBERS.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX));
    }

    @Override
        public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));

        // @formatter:off
        parameters.add(selectParameter(locale)
                .name(FROM_SEPARATORS)
                .item(UNKNOWN_SEPARATORS, UNKNOWN_SEPARATORS)
                .item(US_SEPARATORS, US_SEPARATORS)
                .item(EU_SEPARATORS, EU_SEPARATORS)
                .item(CH_SEPARATORS, CH_SEPARATORS)
                .item(CUSTOM, CUSTOM,  buildDecimalSeparatorParameter(FROM, locale), buildGroupingSeparatorParameter(FROM, locale))
                .defaultValue(UNKNOWN_SEPARATORS)
                .build(this ));
        // @formatter:on

        // @formatter:off
        parameters.add(selectParameter(locale)
                .name(TARGET_PATTERN)
                .item(US_PATTERN, US_PATTERN)
                .item(EU_PATTERN, EU_PATTERN)
                .item(CH_PATTERN, CH_PATTERN)
                .item(SCIENTIFIC, SCIENTIFIC)
                .item(CUSTOM, CUSTOM, parameter(locale).setName(TARGET_PATTERN + "_" + CUSTOM).setType(STRING).setDefaultValue(US_DECIMAL_PATTERN.toPattern()).build(this),
                        buildDecimalSeparatorParameter(TARGET, locale),
                        buildGroupingSeparatorParameter(TARGET, locale))
                .defaultValue(US_PATTERN)
                .build(this ));
        // @formatter:on

        return parameters;
    }

    private Parameter buildDecimalSeparatorParameter(String prefix, Locale locale) {
        final String name = prefix + DECIMAL + SEPARATOR;
        return  SelectParameter.selectParameter(locale) //
                .name(name) //
                .item(".") //
                .item(",") //
                .item(CUSTOM, CUSTOM, Parameter.parameter(locale).setName(name + "_" + CUSTOM)
                        .setType(STRING)
                        .setDefaultValue(".")
                        .build(this)) //
                .defaultValue(".") //
                .build(this);
    }

    private Parameter buildGroupingSeparatorParameter(String prefix, Locale locale) {
        final String name = prefix + GROUPING + SEPARATOR;
        return SelectParameter.selectParameter(locale) //
                .name(name) //
                .item(",", "comma") //
                .item(" ", "space") //
                .item(".", "dot") //
                .item("'", "quote") //
                .item("", "none") //
                .item(CUSTOM, CUSTOM, Parameter.parameter(locale).setName(name + "_" + CUSTOM)
                        .setType(STRING)
                        .setDefaultValue(",")
                        .build(this)) //
                .canBeBlank(true) //
                .defaultValue(",") //
                .build(this);
    }

    private String getCustomizableParam(String pName, Map<String, String> parameters) {
        if (!parameters.containsKey(pName)) {
            return "";
        } else if (parameters.get(pName).equals(CUSTOM)) {
            return parameters.get(pName + "_" + CUSTOM);
        } else {
            return parameters.get(pName);
        }
    }

    /**
     * @param parameters the action parameters.
     * @return the pattern to use according to the given parameters.
     */
    private NumberFormat getFormat(Map<String, String> parameters) {
        switch (parameters.get(TARGET_PATTERN)) {
        case CUSTOM:
            return getCustomFormat(parameters);
        case US_PATTERN:
            return US_DECIMAL_PATTERN;
        case EU_PATTERN:
            return EU_DECIMAL_PATTERN;
        case CH_PATTERN:
            return CH_DECIMAL_PATTERN;
        case SCIENTIFIC:
            return US_SCIENTIFIC_DECIMAL_PATTERN;
        default:
            throw new IllegalArgumentException("Pattern is empty");
        }
    }

    /**
     * Return the custom format out of the parameters.
     *
     * @param parameters the action parameters.
     * @return the custom format out of the parameters.
     */
    private NumberFormat getCustomFormat(Map<String, String> parameters) {
        final DecimalFormat decimalFormat = new DecimalFormat(parameters.get(TARGET_PATTERN + "_" + CUSTOM));

        DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();

        String decimalSeparator = getCustomizableParam(TARGET + DECIMAL + SEPARATOR, parameters);
        if (!StringUtils.isEmpty(decimalSeparator)) {
            decimalFormatSymbols.setDecimalSeparator(decimalSeparator.charAt(0));
        }

        String groupingSeparator = getCustomizableParam(TARGET + GROUPING + SEPARATOR, parameters);
        if (StringUtils.isEmpty(groupingSeparator) || groupingSeparator.equals(decimalSeparator)) {
            decimalFormat.setGroupingUsed(false);
        } else {
            decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
        }

        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        return decimalFormat;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, getAdditionalColumns(actionContext));
        }
        if (actionContext.getActionStatus() == OK) {
            try {
                actionContext.get(COMPILED_TARGET_FORMAT, p -> getFormat(actionContext.getParameters()));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Unsupported number format", e);
                actionContext.setActionStatus(CANCELED);
            }
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final DecimalFormat decimalTargetFormat = context.get(COMPILED_TARGET_FORMAT);

        final ColumnMetadata columnMetadata = context.getRowMetadata().getById(columnId);
        columnMetadata.setType(Type.DOUBLE.toString());
        columnMetadata.setTypeForced(true);
        columnMetadata.setDomain("");
        columnMetadata.setDomainLabel("");
        columnMetadata.setDomainForced(true);

        final String value = row.get(columnId);
        final String mode = context.getParameters().get(FROM_SEPARATORS);
        if (StringUtils.isBlank(value) || (!NumericHelper.isBigDecimal(value) && !CUSTOM.equals(mode))) {
            LOGGER.debug("Unable to parse {} value as Number, it is blank or not numeric", value);
            return;
        }

        final BigDecimal bd;
        switch (mode) {
        case EU_SEPARATORS:
            bd = BigDecimalParser.toBigDecimal(value, ',', '.');
            break;
        case CH_SEPARATORS:
            bd = BigDecimalParser.toBigDecimal(value, '.', '\'');
            break;
        case CUSTOM:
            try {
                bd = parseCustomNumber(context, value);
                break;
            } catch (Exception e) {
                // User specified custom separators that doesn't validate value
                LOGGER.debug("Unable to use custom separators to parse value '{}'", value);
                return;
            }
        case US_SEPARATORS:
            bd = BigDecimalParser.toBigDecimal(value, '.', ',');
            break;
        case UNKNOWN_SEPARATORS:
        default:
            bd = BigDecimalParser.toBigDecimal(value);
            break;
        }

        String newValue = BigDecimalFormatter.format(bd, decimalTargetFormat);
        row.set(ActionsUtils.getTargetColumnId(context), newValue);
    }

    /**
     * Parse the the given number with a custom separator.
     *
     * @param context the action context.
     * @param number the number to parse.
     * @return the given number parsed as BigDecimal using the action context custome separator.
     */
    private BigDecimal parseCustomNumber(ActionContext context, String number) {
        BigDecimal bd;
        String decSep = getCustomizableParam(FROM + DECIMAL + SEPARATOR, context.getParameters());
        String groupSep = getCustomizableParam(FROM + GROUPING + SEPARATOR, context.getParameters());

        if (StringUtils.isEmpty(decSep)) {
            decSep = ".";
        }
        if (StringUtils.isEmpty(groupSep)) {
            groupSep = ",";
        }

        bd = BigDecimalParser.toBigDecimal(number, decSep.charAt(0), groupSep.charAt(0));
        return bd;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
