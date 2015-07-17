package org.talend.dataprep.transformation.api.action.metadata.date;

import static org.talend.dataprep.api.preparation.Action.Builder.builder;

import java.io.IOException;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.action.metadata.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.SingleColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Item;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(ComputeTimeSince.ACTION_BEAN_PREFIX + ComputeTimeSince.TIME_SINCE_ACTION_NAME)
public class ComputeTimeSince extends SingleColumnAction {

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
     * Name of the date pattern.
     */
    protected static final String PATTERN = "date_pattern"; //$NON-NLS-1$

    /**
     * Key to store 'now' in context at the begining of the action.
     */
    protected static final String NOW = "now"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeTimeSince.class);

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return TIME_SINCE_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.DATE.getDisplayName();
    }

    /**
     * @see ActionMetadata#getItems()@return
     */
    @Override
    @Nonnull
    public Item[] getItems() {
        Item.Value[] values = new Item.Value[]{ //
                new Item.Value(ChronoUnit.YEARS.name(), true), //
                new Item.Value(ChronoUnit.MONTHS.name()), //
                new Item.Value(ChronoUnit.DAYS.name()), //
                new Item.Value(ChronoUnit.HOURS.name())};
        return new Item[]{new Item(TIME_UNIT_PARAMETER, "categ", values)};
    }

    /**
     * @see ActionMetadata#create(Map)
     */
    @Override
    public Action create(Map<String, String> parameters) {

        TemporalUnit unit = ChronoUnit.valueOf(parameters.get(TIME_UNIT_PARAMETER).toUpperCase());

        return builder().withRow((row, context) -> {
            String columnId = getColumnIdParameter(parameters);
            // sadly unable to do that outside of the closure since the context is not available
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern((String) context.get(PATTERN));

            String value = row.get(columnId);

            // parse the date
            TemporalAccessor temporalAccessor = null;
            try {
                temporalAccessor = dtf.parse(value, new ParsePosition(0));
            } catch (DateTimeParseException e) {
                // Nothing to do: in this case, temporalAccessor is left null
                LOGGER.debug("Unable to parse date {}.", value, e);
            }

            Temporal now = (Temporal) context.get(NOW);
            Temporal valueAsDate = (unit == ChronoUnit.HOURS ? LocalDateTime.from(temporalAccessor) : LocalDate.from(temporalAccessor));

            long newValue = unit.between(valueAsDate, now);

            row.set(PREFIX + columnId + SUFFIX + unit.toString().toLowerCase(), newValue + "");

        }).withMetadata((rowMetadata, context) -> {

            String columnId = parameters.get(COLUMN_ID);

            JsonFactory jsonFactory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(jsonFactory);

            List<String> columnIds = new ArrayList<>(rowMetadata.size());
            rowMetadata.getColumns().forEach(columnMetadata -> columnIds.add(columnMetadata.getId()));
            if (!columnIds.contains(PREFIX + columnId + SUFFIX + unit.toString().toLowerCase())) {

                // go through the columns to be able to 'insert' the new columns just after the one needed.
                for (int i = 0; i < rowMetadata.getColumns().size(); i++) {
                    ColumnMetadata column = rowMetadata.getColumns().get(i);
                    if (!StringUtils.equals(column.getId(), columnId)) {
                        continue;
                    }

                    // store the current pattern in the context
                    JsonNode rootNode = getStatisticsNode(mapper, column);

                    JsonNode mostUsedPatternNode = rootNode.get("patternFrequencyTable").get(0); //$NON-NLS-1$
                    String datePattern = mostUsedPatternNode.get("pattern").asText(); //$NON-NLS-1$
                    context.put(PATTERN, datePattern);

                    Temporal now = (unit == ChronoUnit.HOURS ? LocalDateTime.now() : LocalDate.now());
                    context.put(NOW, now);

                    // create the new column
                    ColumnMetadata newColumnMetadata = ColumnMetadata.Builder //
                            .column() //
                            .computedId(PREFIX + columnId + SUFFIX + unit.toString().toLowerCase()) //
                            .name(PREFIX + column.getName() + SUFFIX + unit.toString().toLowerCase()) //
                            .type(Type.INTEGER) //
                            .empty(column.getQuality().getEmpty()) //
                            .invalid(column.getQuality().getInvalid()) //
                            .valid(column.getQuality().getValid()) //
                            .headerSize(column.getHeaderSize()) //
                            .build();
                    // add the new column after the current one
                    rowMetadata.getColumns().add(i + 1, newColumnMetadata);
                }
            }
        }).build();
    }

    /**
     * Return the json statistics node.
     *
     * @param mapper jackson object mapper.
     * @param column the column metadata to work on.
     * @return the json statistics node.
     */
    private JsonNode getStatisticsNode(ObjectMapper mapper, ColumnMetadata column) {
        try {
            return mapper.readTree(column.getStatistics());
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    /**
     * @see ActionMetadata#accept(ColumnMetadata)
     */
    @Override
    public boolean accept(ColumnMetadata column) {
        return Type.DATE.equals(Type.get(column.getType()));
    }
}
