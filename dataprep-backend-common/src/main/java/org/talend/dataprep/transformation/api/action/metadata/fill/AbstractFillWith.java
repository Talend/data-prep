package org.talend.dataprep.transformation.api.action.metadata.fill;

import org.apache.commons.lang.StringUtils;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.date.DateParser;
import org.talend.dataprep.transformation.api.action.metadata.date.DatePattern;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;
import org.talend.dataprep.api.type.Type;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public abstract class AbstractFillWith extends ActionMetadata {

    public static final String DEFAULT_VALUE_PARAMETER = "default_value"; //$NON-NLS-1$

    private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final String DEFAULT_DATE_VALUE = DEFAULT_FORMATTER.format(LocalDateTime.of(1970, Month.JANUARY, 1, 10, 0));

    /**
     * Mode: tells if fill value is taken from another column or is a constant
     */
    public static final String MODE_PARAMETER = "mode"; //$NON-NLS-1$

    /**
     * The selected column id.
     */
    public static final String SELECTED_COLUMN_PARAMETER = "selected_column"; //$NON-NLS-1$

    /**
     * Constant to represents mode where we fill with a constant.
     */
    public static final String CONSTANT_MODE = "Constant";
    public static final String COLUMN_MODE = "Another column";

    /**
     * Component that parses dates.
     */
    private final DateParser dateParser = new DateParser(); // TODO investigate why this instantiation is required, should be auto with Autowired

    protected Type type;

    public abstract boolean shouldBeProcessed (String value, ColumnMetadata colMetadata);

    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        checkParameters(parameters, row);

        final ColumnMetadata columnMetadata = row.getRowMetadata().getById(columnId);

        final String value = row.get(columnId);
        if (shouldBeProcessed(value, columnMetadata)) {

            String newValue = "";

            // First, get raw new value regarding mode (constant or other column):
            if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
                newValue = parameters.get(DEFAULT_VALUE_PARAMETER);
            } else {
                final RowMetadata rowMetadata = row.getRowMetadata();
                final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
                newValue = row.get(selectedColumn.getId());
            }

            // Second: if we're on a date column, format new value with the most frequent pattenr of the column:
            Type type = (columnMetadata == null ? Type.ANY : Type.get(columnMetadata.getType()));
            if (type.equals(Type.DATE)) {
                try {
                    final LocalDateTime date = dateParser.parse(newValue, columnMetadata);
                    final DatePattern mostFrequentPattern = dateParser.getMostFrequentPattern(columnMetadata);
                    DateTimeFormatter ourNiceFormatter = (mostFrequentPattern == null ? DEFAULT_FORMATTER : mostFrequentPattern
                            .getFormatter());
                    newValue = ourNiceFormatter.format(date);
                } catch (DateTimeException e) {
                    // Nothing to do, if we can't get a valid pattern, keep the raw value
                }
            }

            // At the end, set the new value:
            row.set(columnId, newValue);
        }
    }

    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        Parameter constantParameter = null;

        switch (type) {
            case NUMERIC:
            case DOUBLE:
            case FLOAT:
            case STRING:
                constantParameter=new Parameter(DEFAULT_VALUE_PARAMETER, //
                        ParameterType.STRING, //
                        StringUtils.EMPTY);
                break;
            case INTEGER:
                constantParameter=new Parameter(DEFAULT_VALUE_PARAMETER, //
                        ParameterType.INTEGER, //
                        "0");
                break;
            case BOOLEAN:
                constantParameter= SelectParameter.Builder.builder() //
                        .name(DEFAULT_VALUE_PARAMETER) //
                        .item("True") //
                        .item("False") //
                        .defaultValue("True") //
                        .build();
                break;
            case DATE:
                constantParameter=new Parameter(DEFAULT_VALUE_PARAMETER, //
                        ParameterType.DATE, //
                        DEFAULT_DATE_VALUE, //
                        false, //
                        false);
                break;
            case ANY:
            default:
                break;
        }

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(MODE_PARAMETER)
                        .item(CONSTANT_MODE, constantParameter)
                        .item(COLUMN_MODE, new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, StringUtils.EMPTY, false, false))
                        .defaultValue(CONSTANT_MODE)
                        .build()
        );
        //@formatter:on

        return parameters;
    }

    /**
     * Check that the selected column parameter is correct : defined in the parameters and there's a matching column. If
     * the parameter is invalid, an exception is thrown.
     *
     * @param parameters where to look the parameter value.
     * @param row        the row where to look for the column.
     */
    private void checkParameters(Map<String, String> parameters, DataSetRow row) {
        if (!parameters.containsKey(MODE_PARAMETER)) {
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    MODE_PARAMETER));
        }

        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && !parameters.containsKey(DEFAULT_VALUE_PARAMETER)) {
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    DEFAULT_VALUE_PARAMETER));
        }
        else if (!parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) &&
                (!parameters.containsKey(SELECTED_COLUMN_PARAMETER) || row.getRowMetadata().getById(parameters.get(SELECTED_COLUMN_PARAMETER)) == null)) {
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    SELECTED_COLUMN_PARAMETER));
        }
    }

}
