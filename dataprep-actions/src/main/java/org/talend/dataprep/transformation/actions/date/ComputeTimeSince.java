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

import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.OTHER_COLUMN_MODE;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.SELECTED_COLUMN_PARAMETER;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(ComputeTimeSince.TIME_SINCE_ACTION_NAME)
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

    public static final Boolean CREATE_NEW_COLUMN_DEFAULT = true;

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));

        parameters.add(SelectParameter.selectParameter(locale) //
                .name(TIME_UNIT_PARAMETER) //
                .item(ChronoUnit.YEARS.name(), "years") //
                .item(ChronoUnit.MONTHS.name(), "months") //
                .item(ChronoUnit.DAYS.name(), "days") //
                .item(ChronoUnit.HOURS.name(), "hours") //
                .item(ChronoUnit.MINUTES.name(), "minutes") //
                .item(ChronoUnit.SECONDS.name(), "seconds") //
                .defaultValue(ChronoUnit.HOURS.name()) //
                .build(this));

        parameters.add(SelectParameter.selectParameter(locale) //
                .name(SINCE_WHEN_PARAMETER) //
                .canBeBlank(false) //
                .item(NOW_SERVER_SIDE_MODE, NOW_SERVER_SIDE_MODE) //
                .item(SPECIFIC_DATE_MODE, SPECIFIC_DATE_MODE, Parameter.parameter(locale).setName(SPECIFIC_DATE_PARAMETER)
                        .setType(ParameterType.DATE)
                        .setDefaultValue(StringUtils.EMPTY)
                        .setCanBeBlank(false)
                        .build(this)) //
                .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE, Parameter.parameter(locale).setName(SELECTED_COLUMN_PARAMETER)
                        .setType(ParameterType.COLUMN)
                        .setDefaultValue(StringUtils.EMPTY)
                        .setCanBeBlank(false)
                        .build(this)) //
                .defaultValue(NOW_SERVER_SIDE_MODE) //
                .build(this));

        return parameters;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        final List<ActionsUtils.AdditionalColumn> additionalColumns = new ArrayList<>();

        TemporalUnit unit = ChronoUnit.valueOf(context.getParameters().get(TIME_UNIT_PARAMETER).toUpperCase());
        additionalColumns.add(ActionsUtils.additionalColumn()
                .withName(PREFIX + context.getColumnName() + SUFFIX + unit.toString().toLowerCase())
                .withType(Type.INTEGER));

        return additionalColumns;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {
            // Create new column
            Map<String, String> parameters = context.getParameters();
            context.get(SINCE_WHEN_PARAMETER, m -> parameters.getOrDefault(SINCE_WHEN_PARAMETER, NOW_SERVER_SIDE_MODE));
            context.get(SINCE_DATE_PARAMETER, m -> parseSinceDateIfConstant(parameters));
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        RowMetadata rowMetadata = context.getRowMetadata();
        Map<String, String> parameters = context.getParameters();
        String columnId = context.getColumnId();

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
        row.set(ActionsUtils.getTargetColumnId(context), newValue);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

    private static LocalDateTime parseSinceDateIfConstant(Map<String, String> parameters) {
        String mode = parameters.getOrDefault(SINCE_WHEN_PARAMETER, NOW_SERVER_SIDE_MODE);

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
