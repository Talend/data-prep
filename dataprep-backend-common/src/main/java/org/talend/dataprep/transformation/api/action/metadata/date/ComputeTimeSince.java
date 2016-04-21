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

import static org.talend.dataprep.api.type.Type.INTEGER;
import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.*;

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
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

@Component(ComputeTimeSince.ACTION_BEAN_PREFIX + ComputeTimeSince.TIME_SINCE_ACTION_NAME)
public class ComputeTimeSince extends AbstractDate implements ColumnAction {

    /**
     * The action name.
     */
    public static final String TIME_SINCE_ACTION_NAME = "compute_time_since"; //$NON-NLS-1$

    public static final String SINCE_WHEN_PARAMETER = "since_when_value"; //$NON-NLS-1$

    public static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";

    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern( DATE_PATTERN);

    /**
     * The column prefix.
     */
    public static final String PREFIX = "since_"; //$NON-NLS-1$

    /**
     * The column suffix.
     */
    public static final String SUFFIX = "_in_"; //$NON-NLS-1$

    /**
     * The unit in which show the period.
     */
    public static final String TIME_UNIT_PARAMETER = "time_unit"; //$NON-NLS-1$

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeTimeSince.class);

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return TIME_SINCE_ACTION_NAME;
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
                .defaultValue(ChronoUnit.HOURS.name()) //
                .build());

        Parameter constantParameter = //
            new Parameter( SINCE_WHEN_PARAMETER, //
                           ParameterType.DATE, //
                           "now", //DEFAULT_FORMATTER.format(LocalDateTime.now()), //
                           false, //
                           false, //
                           getMessagesBundle());

        parameters.add(SelectParameter.Builder.builder() //
                .name(MODE_PARAMETER) //
                .item(CONSTANT_MODE, constantParameter) //
                .item(OTHER_COLUMN_MODE,
                        new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, //
                                StringUtils.EMPTY, false, false, getMessagesBundle())) //
                .defaultValue(CONSTANT_MODE) //
                .build());
        
        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            // Create new column
            final Map<String, String> parameters = context.getParameters();
            final String columnId = context.getColumnId();
            final TemporalUnit unit = ChronoUnit.valueOf(parameters.get(TIME_UNIT_PARAMETER).toUpperCase());
            final ColumnMetadata column = context.getRowMetadata().getById(columnId);
            context.column(PREFIX + column.getName() + SUFFIX + unit.toString().toLowerCase(),
                    (r) -> {
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
                    }
            );
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        final String columnId = context.getColumnId();

        TemporalUnit unit = ChronoUnit.valueOf(parameters.get(TIME_UNIT_PARAMETER).toUpperCase());

        String dateToCompare = parameters.get( SINCE_WHEN_PARAMETER );
        // FIXME column to compare with
        LocalDateTime since = StringUtils.isBlank( dateToCompare) ? LocalDateTime.now() : LocalDateTime.parse( dateToCompare, DEFAULT_FORMATTER );

        final ColumnMetadata column = context.getRowMetadata().getById(columnId);

        // create the new column and add the new column after the current one
        String computeTimeSinceColumn = context.column(PREFIX + column.getName() + SUFFIX + unit.toString().toLowerCase());

        // parse the date
        final String value = row.get(columnId);
        try {
            final LocalDateTime temporalAccessor = dateParser.parse(value, context.getRowMetadata().getById(columnId));
            final Temporal valueAsDate = LocalDateTime.from(temporalAccessor);
            final long newValue = unit.between(valueAsDate, since);
            row.set(computeTimeSinceColumn, String.valueOf(newValue));
        } catch (DateTimeException e) {
            // Nothing to do: in this case, temporalAccessor is left null
            LOGGER.debug("Unable to parse date {} for {} @ {}", value, columnId, row.getTdpId(), e);
            row.set(computeTimeSinceColumn, StringUtils.EMPTY);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
