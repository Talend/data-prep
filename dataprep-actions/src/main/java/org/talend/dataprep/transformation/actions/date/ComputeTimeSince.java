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

package org.talend.dataprep.transformation.actions.date;

import static org.talend.dataprep.api.type.Type.INTEGER;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.OTHER_COLUMN_MODE;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.SELECTED_COLUMN_PARAMETER;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + ComputeTimeSince.TIME_SINCE_ACTION_NAME)
public class ComputeTimeSince extends AbstractDate implements ColumnAction {

    /**
     * The action name.
     */
    public static final String TIME_SINCE_ACTION_NAME = "compute_time_since"; //$NON-NLS-1$

    /**
     * Parameter to set which date to compare to. 3 modes: 'now at runtime', specific date defined by user, took from
     * another column.
     */
    protected static final String SINCE_WHEN_PARAMETER = "since_when";

    protected static final String SPECIFIC_DATE_MODE = "specific_date";

    /**
     * The unit in which show the period.
     */
    protected static final String TIME_UNIT_PARAMETER = "time_unit"; //$NON-NLS-1$

    protected static final String SPECIFIC_DATE_PARAMETER = "specific_date"; //$NON-NLS-1$

    protected static final String SINCE_DATE_PARAMETER = "since_date";

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * The new column prefix.
     */
    private static final String PREFIX = "since_"; //$NON-NLS-1$

    /**
     * The new column suffix.
     */
    private static final String SUFFIX = "_in_"; //$NON-NLS-1$

    private static final String NOW_SERVER_SIDE_MODE = "now_server_side";

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeTimeSince.class);

    @Override
    public String getName() {
        return TIME_SINCE_ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = super.getParameters();

        parameters.add(SelectParameter.Builder.builder() //
                .name(TIME_UNIT_PARAMETER) //
                .item(ChronoUnit.YEARS.name(), "years") //
                .item(ChronoUnit.MONTHS.name(), "months") //
                .item(ChronoUnit.DAYS.name(), "days") //
                .item(ChronoUnit.HOURS.name(), "hours") //
                .defaultValue(ChronoUnit.HOURS.name()) //
                .build());

        parameters.add(SelectParameter.Builder.builder() //
                .name(SINCE_WHEN_PARAMETER) //
                .canBeBlank(false) //
                .item(NOW_SERVER_SIDE_MODE) //
                .item(SPECIFIC_DATE_MODE, new Parameter(SPECIFIC_DATE_PARAMETER, //
                        ParameterType.DATE, //
                        StringUtils.EMPTY, //
                        false, //
                        false)) //
                .item(OTHER_COLUMN_MODE, new Parameter(SELECTED_COLUMN_PARAMETER, //
                        ParameterType.COLUMN, //
                        StringUtils.EMPTY, //
                        false, //
                        false)) //
                .defaultValue(NOW_SERVER_SIDE_MODE) //
                .build());

        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            // Create new column
            Map<String, String> parameters = context.getParameters();
            String columnId = context.getColumnId();
            TemporalUnit unit = ChronoUnit.valueOf(parameters.get(TIME_UNIT_PARAMETER).toUpperCase());
            ColumnMetadata column = context.getRowMetadata().getById(columnId);
            context.column("result", r -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .copy(column)//
                        .computedId(StringUtils.EMPTY) //
                        .name(PREFIX + column.getName() + SUFFIX + unit.toString().toLowerCase()) //
                        .computedId(null) // remove the id
                        .statistics(new Statistics()) // clear the statistics
                        .type(INTEGER)//
                        .build();
                context.getRowMetadata().insertAfter(columnId, c);
                return c;
            });
            context.get(SINCE_WHEN_PARAMETER, m -> parameters.containsKey(SINCE_WHEN_PARAMETER) ?
                    parameters.get(SINCE_WHEN_PARAMETER) :
                    NOW_SERVER_SIDE_MODE);
            context.get(SINCE_DATE_PARAMETER, m -> parseSinceDateIfConstant(parameters));
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        RowMetadata rowMetadata = context.getRowMetadata();
        Map<String, String> parameters = context.getParameters();
        String columnId = context.getColumnId();

        final String newColumnId = context.column("result");

        TemporalUnit unit = ChronoUnit.valueOf(parameters.get(TIME_UNIT_PARAMETER).toUpperCase());
        String newValue;
        try {
            String mode = context.get(SINCE_WHEN_PARAMETER);
            LocalDateTime since;
            switch (mode) {
            case OTHER_COLUMN_MODE:
                ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
                String dateToCompare = row.get(selectedColumn.getId());
                since = Providers.get().parse(dateToCompare, selectedColumn);
                break;
            case SPECIFIC_DATE_MODE:
            case NOW_SERVER_SIDE_MODE:
            default:
                since = context.get(SINCE_DATE_PARAMETER);
                break;
            }

            // parse the date
            if (since == null) {
                newValue = StringUtils.EMPTY;
            } else {
                String value = row.get(columnId);
                LocalDateTime temporalAccessor = Providers.get().parse(value, context.getRowMetadata().getById(columnId));
                Temporal valueAsDate = LocalDateTime.from(temporalAccessor);
                newValue = String.valueOf(unit.between(valueAsDate, since));
            }
        } catch (DateTimeException e) {
            LOGGER.trace("Error on dateTime parsing", e);
            // Nothing to do: in this case, temporalAccessor is left null
            newValue = StringUtils.EMPTY;
        }
        row.set(newColumnId, newValue);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

    private static LocalDateTime parseSinceDateIfConstant(Map<String, String> parameters) {
        String mode = parameters.containsKey(SINCE_WHEN_PARAMETER) ? parameters.get(SINCE_WHEN_PARAMETER) : NOW_SERVER_SIDE_MODE;

        LocalDateTime since;
        switch (mode) {
        case SPECIFIC_DATE_MODE:
            try {
                since = LocalDateTime.parse(parameters.get(SPECIFIC_DATE_PARAMETER), DEFAULT_FORMATTER);
            } catch (DateTimeException e) {
                LOGGER.info("Error parsing input date. The front-end might have supplied a corrupted value.", e);
                since = null;
            }
            break;
        case OTHER_COLUMN_MODE:
            since = null; // It will be computed in apply
            break;
        case NOW_SERVER_SIDE_MODE:
        default:
            since = LocalDateTime.now();
            break;
        }
        return since;
    }

}
