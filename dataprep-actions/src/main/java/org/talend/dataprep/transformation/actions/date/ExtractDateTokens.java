// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.BOOLEAN;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;

import java.text.DateFormatSymbols;
import java.time.DateTimeException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Change the date pattern on a 'date' column.
 */
@Action(ExtractDateTokens.ACTION_NAME)
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

    /** Month constant value. */
    protected static final String MONTH_LABEL = "MONTH_LABEL";

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

    /** Day label constant value. */
    protected static final String DAY_LABEL = "DAY_LABEL";

    /** Day of year constant value. */
    private static final String DAY_OF_YEAR = "DAY_OF_YEAR";

    /** Week of year constant value. */
    private static final String WEEK_OF_YEAR = "WEEK_OF_YEAR";

    /** Language name parameter */
    private static final String LANGUAGE = "LANGUAGE";

    /** Key for the locale value in context */
    private static String LOCALE = "LOCALE";

    private static final DateFieldMappingBean[] DATE_FIELDS = new DateFieldMappingBean[] { //
            new DateFieldMappingBean(YEAR, ChronoField.YEAR), //
            new DateFieldMappingBean(QUARTER, ChronoField.MONTH_OF_YEAR), //
            new DateFieldMappingBean(MONTH, ChronoField.MONTH_OF_YEAR), //
            new DateFieldMappingBean(MONTH_LABEL, ChronoField.MONTH_OF_YEAR), //
            new DateFieldMappingBean(WEEK_OF_YEAR, ChronoField.ALIGNED_WEEK_OF_YEAR), //
            new DateFieldMappingBean(DAY_OF_YEAR, ChronoField.DAY_OF_YEAR), //
            new DateFieldMappingBean(DAY, ChronoField.DAY_OF_MONTH), //
            new DateFieldMappingBean(DAY_OF_WEEK, ChronoField.DAY_OF_WEEK), //
            new DateFieldMappingBean(DAY_LABEL, ChronoField.DAY_OF_WEEK), //
            new DateFieldMappingBean(HOUR_12, ChronoField.HOUR_OF_AMPM), //
            new DateFieldMappingBean(AM_PM, ChronoField.AMPM_OF_DAY), //
            new DateFieldMappingBean(HOUR_24, ChronoField.HOUR_OF_DAY), //
            new DateFieldMappingBean(MINUTE, ChronoField.MINUTE_OF_HOUR), //
            new DateFieldMappingBean(SECOND, ChronoField.SECOND_OF_MINUTE), //
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractDateTokens.class);

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = true;

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(parameter(locale).setName(YEAR).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(QUARTER).setType(BOOLEAN).setDefaultValue(false).build(this));
        parameters.add(parameter(locale).setName(MONTH).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(MONTH_LABEL).setType(BOOLEAN).setDefaultValue(false).build(this));
        parameters.add(parameter(locale).setName(WEEK_OF_YEAR).setType(BOOLEAN).setDefaultValue(false).build(this));
        parameters.add(parameter(locale).setName(DAY_OF_YEAR).setType(BOOLEAN).setDefaultValue(false).build(this));
        parameters.add(parameter(locale).setName(DAY).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(DAY_OF_WEEK).setType(BOOLEAN).setDefaultValue(false).build(this));
        parameters.add(parameter(locale).setName(DAY_LABEL).setType(BOOLEAN).setDefaultValue(false).build(this));
        parameters.add(parameter(locale).setName(HOUR_12).setType(BOOLEAN).setDefaultValue(false).build(this));
        parameters.add(parameter(locale).setName(AM_PM).setType(BOOLEAN).setDefaultValue(false).build(this));
        parameters.add(parameter(locale).setName(HOUR_24).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(MINUTE).setType(BOOLEAN).setDefaultValue(true).build(this));
        parameters.add(parameter(locale).setName(SECOND).setType(BOOLEAN).setDefaultValue(false).build(this));

        List<Locale> locales = Stream
                .of(Locale.CHINESE, Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, //
                        Locale.ITALIAN, Locale.JAPANESE, Locale.KOREAN, Locale.forLanguageTag("es")) //
                .sorted(Comparator.comparing(o -> o.getDisplayLanguage(locale))) //
                .collect(Collectors.toList());

        SelectParameter.SelectParameterBuilder builder = selectParameter(locale).name(LANGUAGE);
        for (Locale currentLocale : locales) {
            builder = builder.constant(currentLocale.getLanguage(), currentLocale.getDisplayLanguage(locale));
        }
        builder = builder.defaultValue(Locale.ENGLISH.getLanguage());

        parameters.add(builder.build(this));
        return parameters;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            final List<ActionsUtils.AdditionalColumn> additionalColumns = new ArrayList<>();
            for (DateFieldMappingBean date_field : DATE_FIELDS) {
                if (Boolean.valueOf(context.getParameters().get(date_field.key))) {
                    additionalColumns.add(ActionsUtils
                            .additionalColumn()
                            .withKey(date_field.key)
                            .withName(context.getColumnName() + SEPARATOR + date_field.key)
                            .withType(Type.INTEGER));
                }
            }

            ActionsUtils.createNewColumn(context, additionalColumns);
            context.get(LOCALE, p -> new Locale(context.getParameters().get(LANGUAGE)));
        }
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
                    switch (date_field.key) {
                    case QUARTER:
                        newValue = String.valueOf(getQuarter(temporalAccessor.get(date_field.field)));
                        break;
                    case DAY_LABEL:
                        newValue = String
                                .valueOf(getLabelDay(temporalAccessor.get(date_field.field), context.get(LOCALE)));
                        break;
                    case MONTH_LABEL:
                        newValue = String
                                .valueOf(getLabelMonth(temporalAccessor.get(date_field.field), context.get(LOCALE)));
                        break;
                    default:
                        newValue = String.valueOf(temporalAccessor.get(date_field.field));
                        break;
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

    /**
     * Get the day label of the week.
     *
     * @param numDayOfWeek the number of the month
     * @return the day label
     */
    String getLabelDay(int numDayOfWeek, Locale locale) {
        String[] label = DateFormatSymbols.getInstance(locale).getWeekdays();
        if (numDayOfWeek >= 0 && numDayOfWeek < 7) {
            return label[numDayOfWeek + 1];
        } else if (numDayOfWeek == 7){
            return label[1];
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Get the month label of the year.
     *
     * @param numMonth the number of the month
     * @return the month label
     */
    String getLabelMonth(int numMonth, Locale locale) {
        String[] label = DateFormatSymbols.getInstance(locale).getMonths();
        if (numMonth >= 1 && numMonth <= 12) {
            return label[numMonth - 1];
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
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
