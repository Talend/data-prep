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

package org.talend.dataprep.transformation.actions.fill;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.RowMetadataUtils;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.error.ActionErrorCodes;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.actions.date.DatePattern;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

public abstract class AbstractFillWith extends AbstractActionMetadata implements OtherColumnParameters {

    public static final String DEFAULT_VALUE_PARAMETER = "default_value"; //$NON-NLS-1$

    private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final String DEFAULT_DATE_VALUE = DEFAULT_FORMATTER.format(LocalDateTime.of(1970, Month.JANUARY, 1, 10, 0));

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFillWith.class);

    protected Type type;

    public abstract boolean shouldBeProcessed(DataSetRow dataSetRow, String columnId);

    @Override
    protected boolean createNewColumnParamVisible() {
        return false;
    }

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

        if (shouldBeProcessed(row, columnId)) {
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
            Type type = columnMetadata == null ? Type.ANY : Type.get(columnMetadata.getType());
            if (type.equals(Type.DATE)) {
                try {
                    final LocalDateTime date = Providers.get().parse(newValue, columnMetadata);
                    final String mostUsedDatePattern = RowMetadataUtils.getMostUsedDatePattern(columnMetadata);
                    DateTimeFormatter ourNiceFormatter = mostUsedDatePattern == null ? DEFAULT_FORMATTER
                            : new DatePattern(mostUsedDatePattern).getFormatter();
                    newValue = ourNiceFormatter.format(date);
                } catch (DateTimeException e) {
                    // Nothing to do, if we can't get a valid pattern, keep the raw value
                    LOGGER.debug("Unable to parse date {}.", row.get(columnId), e);
                }
            }

            // At the end, set the new value:
            row.set(getTargetColumnId(context), newValue);
        }
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);

        Parameter constantParameter = null;

        switch (type) {
        case NUMERIC:
        case DOUBLE:
        case FLOAT:
        case STRING:
            constantParameter = Parameter.parameter(locale).setName(DEFAULT_VALUE_PARAMETER)
                    .setType(ParameterType.STRING)
                    .setDefaultValue(StringUtils.EMPTY)
                    .build(this);
            break;
        case INTEGER:
            constantParameter = Parameter.parameter(locale).setName(DEFAULT_VALUE_PARAMETER)
                    .setType(ParameterType.INTEGER)
                    .setDefaultValue("0")
                    .build(this);
            break;
        case BOOLEAN:
            constantParameter = SelectParameter.selectParameter(locale) //
                    .name(DEFAULT_VALUE_PARAMETER) //
                    .item("True") //
                    .item("False") //
                    .defaultValue("True") //
                    .build(this);
            break;
        case DATE:
            constantParameter = Parameter.parameter(locale).setName(DEFAULT_VALUE_PARAMETER)
                    .setType(ParameterType.DATE)
                    .setDefaultValue(DEFAULT_DATE_VALUE)
                    .setCanBeBlank(false)
                    .build(this);
            break;
        case ANY:
        default:
            break;
        }

        //@formatter:off
        parameters.add(SelectParameter.selectParameter(locale)
                        .name(MODE_PARAMETER)
                        .item(CONSTANT_MODE, CONSTANT_MODE, constantParameter)
                        .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE, Parameter.parameter(locale).setName(SELECTED_COLUMN_PARAMETER).setType(ParameterType.COLUMN).setDefaultValue(StringUtils.EMPTY).setCanBeBlank(false).build(this))
                        .defaultValue(CONSTANT_MODE)
                        .build(this )
        );
        //@formatter:on

        return parameters;
    }

    /**
     * Check that the selected column parameter is correct : defined in the parameters and there's a matching column. If
     * the parameter is invalid, an exception is thrown.
     *
     * @param parameters where to look the parameter value.
     * @param rowMetadata the row metadata where to look for the column.
     */
    private void checkParameters(Map<String, String> parameters, RowMetadata rowMetadata) {
        if (!parameters.containsKey(MODE_PARAMETER)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", MODE_PARAMETER));
        }

        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && !parameters.containsKey(DEFAULT_VALUE_PARAMETER)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", DEFAULT_VALUE_PARAMETER));
        } else if (!parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE) && (!parameters.containsKey(SELECTED_COLUMN_PARAMETER)
                || rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER)) == null)) {
            throw new TalendRuntimeException(ActionErrorCodes.BAD_ACTION_PARAMETER,
                    ExceptionContext.build().put("paramName", SELECTED_COLUMN_PARAMETER));
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
