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

package org.talend.dataprep.transformation.api.action.metadata.date;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;

import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.*;
import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.CONSTANT_MODE;
import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.SELECTED_COLUMN_PARAMETER;

/**
 * Change the date pattern on a 'date' column.
 */
@Component(ModifyDate.ACTION_BEAN_PREFIX + ModifyDate.ACTION_NAME)
public class ModifyDate extends AbstractDate implements ColumnAction, DatePatternParamModel {

    /** Action name. */
    public static final String ACTION_NAME = "modify_date"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyDate.class);

    private static final String PATTERN_CONTEXT_KEY = "pattern"; //$NON-NLS-1$

    private static final String UNIT_CONTEXT_KEY = "time_unit"; //$NON-NLS-1$

    private static final String AMOUNT_CONTEXT_KEY = "amount"; //$NON-NLS-1$

    /**
     * The unit of the amount to subtract.
     */
    protected static final String TIME_UNIT_PARAMETER = "time_unit"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = super.getParameters();

        parameters.add(SelectParameter.Builder.builder() //
                .name(TIME_UNIT_PARAMETER) //
                .item(ChronoUnit.YEARS.name()) //
                .item(ChronoUnit.MONTHS.name()) //
                .item(ChronoUnit.DAYS.name()) //
                .item(ChronoUnit.HOURS.name()) //
                .defaultValue(ChronoUnit.YEARS.name()) //
                .build());

        parameters.add( SelectParameter.Builder.builder() //
                .name(MODE_PARAMETER) //
                .item(CONSTANT_MODE, new Parameter(CONSTANT_VALUE, ParameterType.INTEGER, "1")) //
                .item(OTHER_COLUMN_MODE,
                        new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, //
                                StringUtils.EMPTY, false, false, //
                                StringUtils.EMPTY, getMessagesBundle())) //
                .defaultValue(CONSTANT_MODE) //
                .build());

        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            try {
                actionContext.get(PATTERN_CONTEXT_KEY, p ->
                        dateParser.getMostFrequentPattern(actionContext.getRowMetadata().getById(actionContext.getColumnId())));

                actionContext.get(UNIT_CONTEXT_KEY, p ->
                        ChronoUnit.valueOf(actionContext.getParameters().get(TIME_UNIT_PARAMETER).toUpperCase()));

                if (actionContext.getParameters().get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
                    actionContext.get(AMOUNT_CONTEXT_KEY, p ->
                            computeAmount(actionContext.getParameters().get(CONSTANT_VALUE)));
                }

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

        final String value = row.get(columnId);
        if (StringUtils.isBlank(value)) {
            return;
        }

        Map<String, String> parameters = context.getParameters();
        String mode = parameters.get(MODE_PARAMETER);

        long amount;
        switch (mode) {
        case CONSTANT_MODE:
            amount = context.get(AMOUNT_CONTEXT_KEY);
            break;
        case OTHER_COLUMN_MODE:
            String otherColId = parameters.get(SELECTED_COLUMN_PARAMETER);
            try {
                amount = computeAmount(row.get(otherColId));
            } catch (NumberFormatException e) {
                // Int this case, do not change the original value
                return;
            }
            break;
        default:
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, //
                    ExceptionContext.build().put("paramName", OtherColumnParameters.CONSTANT_MODE));
        }

        try {
            final DatePattern outputPattern = context.get( PATTERN_CONTEXT_KEY);

            LocalDateTime date = dateParser.parse(value, context.getRowMetadata().getById(columnId));

            date = date.plus(amount, context.get(UNIT_CONTEXT_KEY));

            row.set(columnId, outputPattern.getFormatter().format(date));

        } catch (DateTimeException e) {
            // cannot parse the date, let's leave it as is
            LOGGER.debug("Unable to parse date {}.", value, e);
        }
    }

    private long computeAmount(String amount) {
        return BigDecimalParser.toBigDecimal(amount).longValue();
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
