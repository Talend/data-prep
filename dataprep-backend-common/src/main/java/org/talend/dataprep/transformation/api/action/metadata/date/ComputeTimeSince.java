package org.talend.dataprep.transformation.api.action.metadata.date;

import static org.talend.dataprep.api.type.Type.INTEGER;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

@Component(ComputeTimeSince.ACTION_BEAN_PREFIX + ComputeTimeSince.TIME_SINCE_ACTION_NAME)
public class ComputeTimeSince extends AbstractDate implements ColumnAction {

    /**
     * The action name.
     */
    public static final String TIME_SINCE_ACTION_NAME = "compute_time_since"; //$NON-NLS-1$

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

        return parameters;
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {

        TemporalUnit unit = ChronoUnit.valueOf(parameters.get(TIME_UNIT_PARAMETER).toUpperCase());
        Temporal now = LocalDateTime.now();

        final ColumnMetadata column = row.getRowMetadata().getById(columnId);

        // create the new column and add the new column after the current one
        String computeTimeSinceColumn = context.in(this).column(PREFIX + column.getName() + SUFFIX + unit.toString().toLowerCase(),
            row.getRowMetadata(),
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
                row.getRowMetadata().insertAfter(columnId, c);
                return c;
            }
        );

        // parse the date
        final String value = row.get(columnId);
        try {
            final LocalDateTime temporalAccessor = dateParser.parse(value, row.getRowMetadata().getById(columnId));
            final Temporal valueAsDate = LocalDateTime.from(temporalAccessor);
            final long newValue = unit.between(valueAsDate, now);
            row.set(computeTimeSinceColumn, String.valueOf(newValue));
        } catch (DateTimeException e) {
            // Nothing to do: in this case, temporalAccessor is left null
            LOGGER.debug("Unable to parse date {} for {} @ {}", value, columnId, row.getTdpId(), e);
            row.set(computeTimeSinceColumn, StringUtils.EMPTY);
        }
    }

}
