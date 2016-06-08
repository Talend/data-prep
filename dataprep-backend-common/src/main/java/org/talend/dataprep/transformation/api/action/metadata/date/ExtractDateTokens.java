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
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;

/**
 * Change the date pattern on a 'date' column.
 */
@Component(ExtractDateTokens.ACTION_BEAN_PREFIX + ExtractDateTokens.ACTION_NAME)
public class ExtractDateTokens extends AbstractDate implements ColumnAction {

    /** Action name. */
    public static final String ACTION_NAME = "extract_date_tokens"; //$NON-NLS-1$

    /** Separator. */
    private static final String SEPARATOR = "_";

    /** Year constant value. */
    private static final String YEAR = "YEAR";

    /** Month constant value. */
    private static final String MONTH = "MONTH";

    /** Day constant value. */
    private static final String DAY = "DAY";

    /** Hour 12 constant value. */
    private static final String HOUR_12 = "HOUR_12";

    /** Hour 24 constant value. */
    private static final String HOUR_24 = "HOUR_24";

    /** Minute constant value. */
    private static final String MINUTE = "MINUTE";

    /** AM_PM constant value. */
    private static final String AM_PM = "AM_PM";

    /** Second constant value. */
    private static final String SECOND = "SECOND";

    /** Day of week constant value. */
    private static final String DAY_OF_WEEK = "DAY_OF_WEEK";

    /** Day of year constant value. */
    private static final String DAY_OF_YEAR = "DAY_OF_YEAR";

    /** Week of year constant value. */
    private static final String WEEK_OF_YEAR = "WEEK_OF_YEAR";

    /** True constant value. */
    private static final String TRUE = "true";

    /** False constant value. */
    private static final String FALSE = "false";


    private static final DateFieldMappingBean[] DATE_FIELDS = new DateFieldMappingBean[]{//
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

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(YEAR, ParameterType.BOOLEAN, TRUE));
        parameters.add(new Parameter(MONTH, ParameterType.BOOLEAN, TRUE));
        parameters.add(new Parameter(DAY, ParameterType.BOOLEAN, TRUE));
        parameters.add(new Parameter(HOUR_12, ParameterType.BOOLEAN, FALSE));
        parameters.add(new Parameter(AM_PM, ParameterType.BOOLEAN, FALSE));
        parameters.add(new Parameter(HOUR_24, ParameterType.BOOLEAN, TRUE));
        parameters.add(new Parameter(MINUTE, ParameterType.BOOLEAN, TRUE));
        parameters.add(new Parameter(SECOND, ParameterType.BOOLEAN, FALSE));
        parameters.add(new Parameter(DAY_OF_WEEK, ParameterType.BOOLEAN, FALSE));
        parameters.add(new Parameter(DAY_OF_YEAR, ParameterType.BOOLEAN, FALSE));
        parameters.add(new Parameter(WEEK_OF_YEAR, ParameterType.BOOLEAN, FALSE));
        return parameters;
    }

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            final RowMetadata rowMetadata = context.getRowMetadata();
            final String columnId = context.getColumnId();
            final Map<String, String> parameters = context.getParameters();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            for (DateFieldMappingBean date_field : DATE_FIELDS) {
                if (Boolean.valueOf(parameters.get(date_field.key))) {
                    context.column(column.getName() + SEPARATOR + date_field.key, r -> {
                        final ColumnMetadata c = ColumnMetadata.Builder //
                                .column() //
                                .name(column.getName() + SEPARATOR + date_field.key) //
                                .type(Type.INTEGER) //
                                .empty(column.getQuality().getEmpty()) //
                                .invalid(column.getQuality().getInvalid()) //
                                .valid(column.getQuality().getValid()) //
                                .headerSize(column.getHeaderSize()) //
                                .build();
                        rowMetadata.insertAfter(columnId, c);
                        return c;
                    });
                }
            }
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final RowMetadata rowMetadata = context.getRowMetadata();
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();
        final ColumnMetadata column = rowMetadata.getById(columnId);

        // Create new columns for date tokens
        final Map<String, String> dateFieldColumns = new HashMap<>();
        for (DateFieldMappingBean date_field : DATE_FIELDS) {
            if (Boolean.valueOf(parameters.get(date_field.key))) {
                final String newColumn = context.column(column.getName() + SEPARATOR + date_field.key);
                dateFieldColumns.put(date_field.key, newColumn);
            }
        }

        // Get the most used pattern formatter and parse the date
        final String value = row.get(columnId);
        if (value == null) {
            return;
        }
        TemporalAccessor temporalAccessor = null;
        try {
            temporalAccessor = dateParser.parse(value, context.getRowMetadata().getById(columnId));
        } catch (DateTimeException e) {
            // temporalAccessor is left null, this will be used bellow to set empty new value for all fields
            LOGGER.debug("Unable to parse date {}.", value, e);
        }

        // insert new extracted values
        for (final DateFieldMappingBean date_field : DATE_FIELDS) {
            if (Boolean.valueOf(parameters.get(date_field.key))) {
                String newValue = StringUtils.EMPTY;
                if (temporalAccessor != null && // may occurs if date can not be parsed with pattern
                        temporalAccessor.isSupported(date_field.field)) {
                    newValue = String.valueOf(temporalAccessor.get(date_field.field));
                }
                row.set(dateFieldColumns.get(date_field.key), newValue);
            }
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

    private static class DateFieldMappingBean {
        private final String key;
        private final ChronoField field;

        public DateFieldMappingBean(String key, ChronoField field) {
            super();
            this.key = key;
            this.field = field;
        }
    }
}
