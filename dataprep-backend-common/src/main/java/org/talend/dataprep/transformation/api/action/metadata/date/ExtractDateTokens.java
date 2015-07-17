package org.talend.dataprep.transformation.api.action.metadata.date;

import static org.talend.dataprep.api.preparation.Action.Builder.builder;

import java.io.IOException;
import java.text.ParsePosition;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
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
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Change the date pattern on a 'date' column.
 */
@Component(ExtractDateTokens.ACTION_BEAN_PREFIX + ExtractDateTokens.ACTION_NAME)
public class ExtractDateTokens extends SingleColumnAction {

    /** Action name. */
    public static final String ACTION_NAME = "extract_date_tokens"; //$NON-NLS-1$

    /** Name of the date pattern. */
    protected static final String PATTERN = "date_pattern"; //$NON-NLS-1$

    private static final String SEPARATOR = "_";

    private static final String YEAR = "YEAR";

    private static final String MONTH = "MONTH";

    private static final String DAY = "DAY";

    private static final String HOUR_12 = "HOUR_12";

    private static final String HOUR_24 = "HOUR_24";

    private static final String MINUTE = "MINUTE";

    private static final String AM_PM = "AM_PM";

    private static final String SECOND = "SECOND";

    private static final String DAY_OF_WEEK = "DAY_OF_WEEK";

    private static final String DAY_OF_YEAR = "DAY_OF_YEAR";

    private static final String WEEK_OF_YEAR = "WEEK_OF_YEAR";

    private static final DateFieldMappingBean[] DATE_FIELDS = new DateFieldMappingBean[] {//
    new DateFieldMappingBean(YEAR, ChronoField.YEAR),//
            new DateFieldMappingBean(MONTH, ChronoField.MONTH_OF_YEAR),//
            new DateFieldMappingBean(DAY, ChronoField.DAY_OF_MONTH), //
            new DateFieldMappingBean(HOUR_12, ChronoField.HOUR_OF_AMPM), //
            new DateFieldMappingBean(AM_PM, ChronoField.AMPM_OF_DAY), //
            new DateFieldMappingBean(HOUR_24, ChronoField.HOUR_OF_DAY), //
            new DateFieldMappingBean(MINUTE, ChronoField.MINUTE_OF_HOUR), //
            new DateFieldMappingBean(SECOND, ChronoField.SECOND_OF_MINUTE), //
            new DateFieldMappingBean(DAY_OF_WEEK, ChronoField.DAY_OF_WEEK), //
            new DateFieldMappingBean(DAY_OF_YEAR, ChronoField.DAY_OF_YEAR), //
            new DateFieldMappingBean(WEEK_OF_YEAR, ChronoField.ALIGNED_WEEK_OF_YEAR), //
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractDateTokens.class);

    private static class DateFieldMappingBean {

        String key;

        ChronoField field;

        public DateFieldMappingBean(String key, ChronoField field) {
            super();
            this.key = key;
            this.field = field;
        }

    }

    @Override
    @Nonnull
    public Parameter[] getParameters() {
        return new Parameter[] { COLUMN_ID_PARAMETER, COLUMN_NAME_PARAMETER, //
                new Parameter(YEAR, Type.BOOLEAN.getName(), "true"),//
                new Parameter(MONTH, Type.BOOLEAN.getName(), "true"),//
                new Parameter(DAY, Type.BOOLEAN.getName(), "true"),//
                new Parameter(HOUR_12, Type.BOOLEAN.getName(), "false"),//
                new Parameter(AM_PM, Type.BOOLEAN.getName(), "false"),//
                new Parameter(HOUR_24, Type.BOOLEAN.getName(), "true"),//
                new Parameter(MINUTE, Type.BOOLEAN.getName(), "true"),//
                new Parameter(SECOND, Type.BOOLEAN.getName(), "false"),//
                new Parameter(DAY_OF_WEEK, Type.BOOLEAN.getName(), "false"),//
                new Parameter(DAY_OF_YEAR, Type.BOOLEAN.getName(), "false"),//
                new Parameter(WEEK_OF_YEAR, Type.BOOLEAN.getName(), "false"),//
        };
    }

    private ColumnMetadata createNewColumn(ColumnMetadata column, String suffix) {
        return ColumnMetadata.Builder //
                .column() //
                .computedId(column.getId() + SEPARATOR + suffix) //
                .name(column.getName() + SEPARATOR + suffix) //
                .type(Type.INTEGER) //
                .empty(column.getQuality().getEmpty()) //
                .invalid(column.getQuality().getInvalid()) //
                .valid(column.getQuality().getValid()) //
                .headerSize(column.getHeaderSize()) //
                .build();
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
     * @see ActionMetadata#create(Map)
     */
    @Override
    public Action create(Map<String, String> parameters) {
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
                // Nothing to do: in this case, temporalAccessor is left null, this will be used bellow to set empty
                // new value for all fields
                LOGGER.debug("Unable to parse date {}.", value, e);
            }

            for (DateFieldMappingBean date_field : DATE_FIELDS) {
                if (Boolean.valueOf(parameters.get(date_field.key))) {
                    String newValue = "";

                    if (temporalAccessor != null && // may occurs if date can not be parsed with pattern
                            temporalAccessor.isSupported(date_field.field)) {
                        newValue = temporalAccessor.get(date_field.field) + "";
                    }
                    row.set(columnId + SEPARATOR + date_field.key, newValue);
                }
            }
        }).withMetadata((rowMetadata, context) -> {
            // check the column id parameter
            String columnId = getColumnIdParameter(parameters);

            JsonFactory jsonFactory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(jsonFactory);

            List<ColumnMetadata> newColumns = new ArrayList<>(rowMetadata.size() + 1);

            for (ColumnMetadata column : rowMetadata.getColumns()) {
                ColumnMetadata newColumnMetadata = ColumnMetadata.Builder.column().copy(column).build();
                newColumns.add(newColumnMetadata);

                // append the split column
                if (StringUtils.equals(columnId, column.getId())) {
                    // apply the new columns to the row metadata
                    rowMetadata.setColumns(newColumns);

                    // store the current pattern in the context
                    JsonNode rootNode = getStatisticsNode(mapper, column);

                    JsonNode mostUsedPatternNode = rootNode.get("patternFrequencyTable").get(0); //$NON-NLS-1$
                    String datePattern = mostUsedPatternNode.get("pattern").asText(); //$NON-NLS-1$
                    context.put(PATTERN, datePattern);

                    for (DateFieldMappingBean date_field : DATE_FIELDS) {
                        if (Boolean.valueOf(parameters.get(date_field.key))) {
                            // create the new column
                            newColumnMetadata = createNewColumn(column, date_field.key);

                            // add the new column after the current one
                            rowMetadata.getColumns().add(newColumnMetadata);
                        }
                    }
                }

            }
        }).build();
    }

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.DATE.getDisplayName();
    }

    /**
     * Only works on 'date' columns.
     * 
     * @see ActionMetadata#accept(ColumnMetadata)
     */
    @Override
    public boolean accept(ColumnMetadata column) {
        return Type.DATE.equals(Type.get(column.getType()));
    }
}
