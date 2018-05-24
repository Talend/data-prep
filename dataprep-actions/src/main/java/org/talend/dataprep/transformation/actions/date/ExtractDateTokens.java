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

import java.time.DateTimeException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.BOOLEAN;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.TARGET_COLUMN_CONTEXT_KEY;

/**
 * Change the date pattern on a 'date' column.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + ExtractDateTokens.ACTION_NAME)
public class ExtractDateTokens extends AbstractDate implements ColumnAction {

    /** Action name. */
    public static final String ACTION_NAME = "extract_date_tokens"; //$NON-NLS-1$

    /** Separator. */
    private static final String SEPARATOR = "_";

    /** Quarter constat value. */
    protected static final String QUARTER = "QUARTER";

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

    private static final DateFieldMappingBean[] DATE_FIELDS = new DateFieldMappingBean[] { //
            new DateFieldMappingBean(YEAR, ChronoField.YEAR), //
            new DateFieldMappingBean(QUARTER, ChronoField.MONTH_OF_YEAR), //
            new DateFieldMappingBean(MONTH, ChronoField.MONTH_OF_YEAR), //
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

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = true;

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(parameter(locale).setName(YEAR).setType(BOOLEAN).setDefaultValue(TRUE).build(this));
        parameters.add(parameter(locale).setName(QUARTER).setType(BOOLEAN).setDefaultValue(FALSE).build(this));
        parameters.add(parameter(locale).setName(MONTH).setType(BOOLEAN).setDefaultValue(TRUE).build(this));
        parameters.add(parameter(locale).setName(WEEK_OF_YEAR).setType(BOOLEAN).setDefaultValue(FALSE).build(this));
        parameters.add(parameter(locale).setName(DAY_OF_YEAR).setType(BOOLEAN).setDefaultValue(FALSE).build(this));
        parameters.add(parameter(locale).setName(DAY).setType(BOOLEAN).setDefaultValue(TRUE).build(this));
        parameters.add(parameter(locale).setName(DAY_OF_WEEK).setType(BOOLEAN).setDefaultValue(FALSE).build(this));
        parameters.add(parameter(locale).setName(HOUR_12).setType(BOOLEAN).setDefaultValue(FALSE).build(this));
        parameters.add(parameter(locale).setName(AM_PM).setType(BOOLEAN).setDefaultValue(FALSE).build(this));
        parameters.add(parameter(locale).setName(HOUR_24).setType(BOOLEAN).setDefaultValue(TRUE).build(this));
        parameters.add(parameter(locale).setName(MINUTE).setType(BOOLEAN).setDefaultValue(TRUE).build(this));
        parameters.add(parameter(locale).setName(SECOND).setType(BOOLEAN).setDefaultValue(FALSE).build(this));
        return parameters;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (!ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            return;
        }

        final RowMetadata rowMetadata = context.getRowMetadata();
        final String columnId = context.getColumnId();
        final ColumnMetadata column = rowMetadata.getById(columnId);

        // like ActionsUtils#createNewColumn but using always context column id for RowMetadata#insertAfter
        context.evict(TARGET_COLUMN_CONTEXT_KEY);
        context.get(TARGET_COLUMN_CONTEXT_KEY, r -> {
            Map<String, String> targetColumnIds = new HashMap<>();
            for (DateFieldMappingBean dateField : DATE_FIELDS) {
                if (Boolean.valueOf(context.getParameters().get(dateField.key))) {
                    ColumnMetadata metadata = ColumnMetadata.Builder.column() //
                            .name(column.getName() + SEPARATOR + dateField.key) //
                            .type(Type.INTEGER) //
                            .empty(column.getQuality().getEmpty()) //
                            .invalid(column.getQuality().getInvalid()) //
                            .valid(column.getQuality().getValid()) //
                            .headerSize(column.getHeaderSize()) //
                            .build();

                    targetColumnIds.put(dateField.key, rowMetadata.insertAfter(columnId, metadata));
                }
            }
            return targetColumnIds;
        });
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();

        // Get the most used pattern formatter and parse the date
        final String value = row.get(columnId);
        if (value == null) {
            return;
        }
        TemporalAccessor temporalAccessor = null;
        try {
            temporalAccessor = Providers.get().parse(value, context.getRowMetadata().getById(columnId));
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
                    if (QUARTER.equals(date_field.key)) {
                        newValue = String.valueOf(getQuarter(temporalAccessor.get(date_field.field)));
                    }
                    else {
                        newValue = String.valueOf(temporalAccessor.get(date_field.field));
                    }
                }
                row.set(ActionsUtils.getTargetColumnIds(context).get(date_field.key), newValue);
            }
        }
    }

    /**
     * Get the quarter of the year.
     *
     * @param numMonth the number of the month
     * @return the quarter
     */
    int getQuarter(int numMonth) {
        return (numMonth - 1) / 3 + 1;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS, Behavior.NEED_STATISTICS_PATTERN);
    }

    private static class DateFieldMappingBean {

        private final String key;

        private final ChronoField field;

        private DateFieldMappingBean(String key, ChronoField field) {
            super();
            this.key = key;
            this.field = field;
        }
    }
}
