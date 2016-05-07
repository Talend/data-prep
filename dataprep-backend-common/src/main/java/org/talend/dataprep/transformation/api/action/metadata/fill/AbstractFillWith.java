//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.fill;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.metadata.date.DateParser;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;

public abstract class AbstractFillWith extends ActionMetadata implements OtherColumnParameters {

    public static final String DEFAULT_VALUE_PARAMETER = "default_value"; //$NON-NLS-1$

    private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final String DEFAULT_DATE_VALUE = DEFAULT_FORMATTER.format(LocalDateTime.of(1970, Month.JANUARY, 1, 10, 0));

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFillWith.class);

    /**
     * Component that parses dates.
     */
    @Autowired
    protected DateParser dateParser;

    protected Type type;

    public abstract boolean shouldBeProcessed (String value, ColumnMetadata colMetadata);

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            final RowMetadata input = actionContext.getRowMetadata();
            checkParameters(actionContext.getParameters(), input);
        }
    }

    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        final String columnId = context.getColumnId();
        final ColumnMetadata columnMetadata = context.getRowMetadata().getById(columnId);

        final String value = row.get(columnId);
        if (shouldBeProcessed(value, columnMetadata)) {
            String newValue;
            // First, get raw new value regarding mode (constant or other column):
            if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
                newValue = parameters.get(DEFAULT_VALUE_PARAMETER);
            } else {
                final RowMetadata rowMetadata = context.getRowMetadata();
                final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
                newValue = row.get(selectedColumn.getId());
            }

            // Second: if we're on a date column, format new value with the most frequent pattern of the column:
            Type type = ((columnMetadata == null) ? Type.ANY : Type.get(columnMetadata.getType()));
            if (type.equals(Type.DATE)) {
                try {
                    final LocalDateTime date = dateParser.parse(newValue, columnMetadata);
                    final DatePattern mostFrequentPattern = dateParser.getMostFrequentPattern(columnMetadata);
                    DateTimeFormatter ourNiceFormatter = (mostFrequentPattern == null ? DEFAULT_FORMATTER : mostFrequentPattern
                            .getFormatter());
                    newValue = ourNiceFormatter.format(date);
                } catch (DateTimeException e) {
                    // Nothing to do, if we can't get a valid pattern, keep the raw value
                    LOGGER.debug("Unable to parse date {}.", value, e);
                }
            }

            // At the end, set the new value:
            row.set(columnId, newValue);
        }
    }

    @Override
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
                        false, //
                        StringUtils.EMPTY, //
                        getMessagesBundle());
                break;
            case ANY:
            default:
                break;
        }

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(MODE_PARAMETER)
                        .item(CONSTANT_MODE, constantParameter)
                        .item(OTHER_COLUMN_MODE, new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, //
                                                               StringUtils.EMPTY, false, false, StringUtils.EMPTY, getMessagesBundle()))
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
     * @param rowMetadata        the row metadata where to look for the column.
     */
    private void checkParameters(Map<String, String> parameters, RowMetadata rowMetadata) {
        if (!parameters.containsKey(MODE_PARAMETER)) {
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    MODE_PARAMETER));
        }

        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && !parameters.containsKey(DEFAULT_VALUE_PARAMETER)) {
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    DEFAULT_VALUE_PARAMETER));
        }
        else if (!parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) &&
                (!parameters.containsKey(SELECTED_COLUMN_PARAMETER) || rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER)) == null)) {
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, ExceptionContext.build().put("paramName",
                    SELECTED_COLUMN_PARAMETER));
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
