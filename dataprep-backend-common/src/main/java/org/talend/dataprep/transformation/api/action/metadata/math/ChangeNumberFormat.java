package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.talend.dataprep.number.BigDecimalParser.*;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.STRING;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.number.BigDecimalFormatter;
import org.talend.dataprep.number.BigDecimalParser;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

/**
 * Change the pattern on a 'number' column.
 */
@Component(ChangeNumberFormat.ACTION_BEAN_PREFIX + ChangeNumberFormat.ACTION_NAME)
public class ChangeNumberFormat extends ActionMetadata implements ColumnAction {

    /** Action name. */
    public static final String ACTION_NAME = "change_number_format"; //$NON-NLS-1$

    /**
     * Parameter to define original decimal & grouping separators.
     */
    public static final String FROM_SEPARATORS = "from_separators";
    
    /**
     * The pattern shown to the user as a list. An item in this list is the value 'custom', which allow the user to
     * manually enter his pattern.
     */
    public static final String TARGET_PATTERN = "target_pattern"; //$NON-NLS-1$

    /**
     * Key to store the compiled format in action context.
     */
    private static final String COMPILED_TARGET_FORMAT = "compiled_number_format";

    /**
     * Keys used in the values of different parameters:
     */
    protected static final String CUSTOM = "custom";
    protected static final String UNKNOWN_SEPARATORS = "unknown_separators";
    protected static final String US_SEPARATORS = "us_separators";
    protected static final String EU_SEPARATORS = "eu_separators";
    protected static final String US_PATTERN = "us_pattern";
    protected static final String EU_PATTERN = "eu_pattern";
    protected static final String SCIENTIFIC = "scientific";

    /**
     * Constants to build parameters name by concat:
     */
    protected static final String FROM = "from";
    protected static final String TARGET = "target";
    protected static final String GROUPING = "_grouping";
    protected static final String DECIMAL = "_decimal";
    protected static final String SEPARATOR = "_separator";

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.MATH.getDisplayName();
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        // @formatter:off
        parameters.add(SelectParameter.Builder.builder()
                .name(FROM_SEPARATORS)
                .item(UNKNOWN_SEPARATORS)
                .item(US_SEPARATORS)
                .item(EU_SEPARATORS)
                .item(CUSTOM, buildDecimalSeparatorParameter(FROM), buildGroupingSeparatorParameter(FROM))
                .defaultValue(UNKNOWN_SEPARATORS)
                .build());
        // @formatter:on

        // @formatter:off
        parameters.add(SelectParameter.Builder.builder()
                .name(TARGET_PATTERN)
                .item(US_PATTERN)
                .item(EU_PATTERN)
                .item(SCIENTIFIC)
                .item(CUSTOM, new Parameter(TARGET_PATTERN + "_" + CUSTOM, STRING, US_DECIMAL_PATTERN.toPattern()),
                        buildDecimalSeparatorParameter(TARGET),
                        buildGroupingSeparatorParameter(TARGET))
                .defaultValue(US_PATTERN)
                .build());
        // @formatter:on

        return parameters;
    }

    private Parameter buildDecimalSeparatorParameter(String prefix) {
        final String name = prefix + DECIMAL + SEPARATOR;
        // @formatter:off
        return  SelectParameter.Builder.builder()
                .name(name)
                .item(".")
                .item(",")
                .item(CUSTOM, new Parameter(name + "_" + CUSTOM, STRING, "."))
                .defaultValue(".")
                .build();
        // @formatter:on
    }

    private Parameter buildGroupingSeparatorParameter(String prefix) {
        final String name = prefix + GROUPING + SEPARATOR;
        // @formatter:off
        return  SelectParameter.Builder.builder()
                .name(name)
                .item(",")
                .item(" ")
                .item(".")
                .item("", "None")
                .item(CUSTOM, new Parameter(name + "_" + CUSTOM, STRING, ","))
                .canBeBlank(true)
                .defaultValue(",")
                .build();
        // @formatter:on
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
        case US_PATTERN:
            return US_DECIMAL_PATTERN;
        case EU_PATTERN:
            return EU_DECIMAL_PATTERN;
        case SCIENTIFIC:
            return US_SCIENTIFIC_DECIMAL_PATTERN;
        }

        throw new IllegalArgumentException("Pattern is empty");
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            try {
                actionContext.get(COMPILED_TARGET_FORMAT, (p) -> getFormat(actionContext.getParameters()));
            } catch (IllegalArgumentException e) {
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
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

        final ColumnMetadata columnMetadata = row.getRowMetadata().getById(columnId);
        columnMetadata.setType(Type.DOUBLE.toString());
        columnMetadata.setTypeForced(true);
        columnMetadata.setDomain("");
        columnMetadata.setDomainLabel("");
        columnMetadata.setDomainForced(true);

        final String value = row.get(columnId);
        if (StringUtils.isBlank(value)) {
            return;
        }

        try {
            BigDecimal bd = null;

            switch (context.getParameters().get(FROM_SEPARATORS)) {
            case UNKNOWN_SEPARATORS:
            case US_SEPARATORS:
                bd = BigDecimalParser.toBigDecimal(value);
                break;
            case EU_SEPARATORS:
                bd = BigDecimalParser.toBigDecimal(value, ',', '.');
                break;
            case CUSTOM:
                String decSep = getCustomizableParam(FROM + DECIMAL + SEPARATOR, context.getParameters());
                String groupSep = getCustomizableParam(FROM + GROUPING + SEPARATOR, context.getParameters());

                if (StringUtils.isEmpty(decSep)) {
                    decSep = ".";
                }
                if (StringUtils.isEmpty(groupSep)) {
                    groupSep = ",";
                }

                bd = BigDecimalParser.toBigDecimal(value, decSep.charAt(0), groupSep.charAt(0));
                break;
            }

            String newValue = BigDecimalFormatter.format(bd, decimalTargetFormat);

            row.set(columnId, newValue);
        } catch (ParseException e) {
            // Do not change this value, not a number
        }
    }

}
