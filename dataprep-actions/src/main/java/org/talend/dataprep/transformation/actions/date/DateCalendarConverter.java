// ============================================================================
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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.talend.dataprep.api.type.Type.DATE;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.date.DateCalendarConverter.CalendarUnit.*;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.chrono.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.JulianFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
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
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.converters.JulianDayConverter;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

@Action(DateCalendarConverter.ACTION_NAME)
public class DateCalendarConverter extends AbstractActionMetadata implements ColumnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateCalendarConverter.class);

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "date_calendar_converter";

    /**
     * Action parameters:
     */
    protected static final String FROM_MODE = "from_pattern_mode";

    protected static final String FROM_MODE_BEST_GUESS = "unknown_separators";

    protected static final String FROM_CALENDAR_TYPE_PARAMETER = "from_calendar_type";

    protected static final String TO_CALENDAR_TYPE_PARAMETER = "to_calendar_type";

    private static final String FROM_DATE_PATTERNS_KEY = "from_date_patterns_key";

    private static final String FROM_CALENDAR_TYPE_KEY = "from_calendar_type_key";

    private static final String TO_CALENDAR_TYPE_KEY = "to_calendar_type_key";

    private static final String FROM_LOCALE_KEY = "from_locale_key";

    private static final String TO_LOCALE_KEY = "to_locale_key";

    private static final String JULIAN_DAY_CONVERT_KEY = "julian_day_convert_key";

    private static final String DEFAULT_OUTPUT_PATTERN = "yyyy-MM-dd G";

    private static Map<String,org.talend.dataquality.converters.DateCalendarConverter> dateCalendarConverterMap =null;

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    protected static final String NEW_COLUMN_SUFFIX = "_converted_calendar";

    /**
     * if it converts from Chronology
     */
    private boolean isFromChronology ;

    /**
     * if it converts to Chronology
     */
    private boolean isToChronology ;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.DATE.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        final String domain = column.getDomain().toUpperCase();
        return DATE.equals(Type.get(column.getType())) || SemanticCategoryEnum.DATE.name().equals(domain) || Type.INTEGER.isAssignableFrom(Type.get(column.getType()));
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));
        final Parameter toJulianDayOrISOParameters = selectParameter(locale)
                .name(TO_CALENDAR_TYPE_PARAMETER)
                .item(ISO.name(), ISO.toString())
                .item(JULIAN_DAY.name(), JULIAN_DAY.toString())
                .item(MODIFIED_JULIAN_DAY.name(), MODIFIED_JULIAN_DAY.toString())
                .item(RATA_DIE.name(), RATA_DIE.toString())
                .item(EPOCH_DAY.name(), EPOCH_DAY.toString())
                .defaultValue(ISO.name())
                .build(this);
        final Parameter toCompleteParameters = selectParameter(locale)
                .name(TO_CALENDAR_TYPE_PARAMETER)
                .item(ISO.name(), ISO.toString())
                .item(HIJRI.name(), HIJRI.toString())
                .item(JAPANESE.name(), JAPANESE.toString())
                .item(MINGUO.name(), MINGUO.toString())
                .item(THAI_BUDDHIST.name(), THAI_BUDDHIST.toString())
                .item(JULIAN_DAY.name(), JULIAN_DAY.toString())
                .item(MODIFIED_JULIAN_DAY.name(), MODIFIED_JULIAN_DAY.toString())
                .item(RATA_DIE.name(), RATA_DIE.toString())
                .item(EPOCH_DAY.name(), EPOCH_DAY.toString())
                .defaultValue(MINGUO.name())
                .build(this);
        //@formatter:off
        parameters.add(selectParameter(locale)
                .name(FROM_CALENDAR_TYPE_PARAMETER)
                .item(ISO.name(), ISO.toString(), toCompleteParameters)
                .item(HIJRI.name(), HIJRI.toString(), toCompleteParameters)
                .item(JAPANESE.name(), JAPANESE.toString(), toCompleteParameters)
                .item(MINGUO.name(), MINGUO.toString(), toCompleteParameters)
                .item(THAI_BUDDHIST.name(), THAI_BUDDHIST.toString(), toCompleteParameters)
                .item(JULIAN_DAY.name(), JULIAN_DAY.toString(), toJulianDayOrISOParameters)
                .item(MODIFIED_JULIAN_DAY.name(), MODIFIED_JULIAN_DAY.toString(), toJulianDayOrISOParameters)
                .item(RATA_DIE.name(), RATA_DIE.toString(), toJulianDayOrISOParameters)
                .item(EPOCH_DAY.name(), EPOCH_DAY.toString(), toJulianDayOrISOParameters)
                .defaultValue(ISO.name())
                .build(this ));
        //@formatter:on

        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, singletonList(ActionsUtils.additionalColumn().withName(actionContext.getColumnName() + NEW_COLUMN_SUFFIX)));
        }
        if (actionContext.getActionStatus() == OK) {
            dateCalendarConverterMap = new HashMap<>();
            String fromCalendarParameter = actionContext.getParameters().get(FROM_CALENDAR_TYPE_PARAMETER);
            String toCalendarParameter = actionContext.getParameters().get(TO_CALENDAR_TYPE_PARAMETER);
            isFromChronology = valueOf(fromCalendarParameter).isChronology();
            isToChronology = valueOf(toCalendarParameter).isChronology();
            if (isFromChronology) {
                AbstractChronology fromCalendarType = valueOf(fromCalendarParameter).getCalendarType();
                Locale fromLocale = valueOf(fromCalendarParameter).getDefaultLocale();
                actionContext.get(FROM_CALENDAR_TYPE_KEY, p -> fromCalendarType);
                actionContext.get(FROM_LOCALE_KEY, p -> fromLocale);
                actionContext.get(FROM_DATE_PATTERNS_KEY, p -> compileFromDatePattern(actionContext));
            } else {//from JulianDay,no need to input pattern and Locale
                TemporalField fromTemporalField = valueOf(fromCalendarParameter).getTemporalField();
                actionContext.get(FROM_CALENDAR_TYPE_KEY, p -> fromTemporalField);
            }

            if (isToChronology) {
                AbstractChronology toCalendarType = valueOf(toCalendarParameter).getCalendarType();
                Locale toLocale = valueOf(toCalendarParameter).getDefaultLocale();
                actionContext.get(TO_CALENDAR_TYPE_KEY, p -> toCalendarType);
                actionContext.get(TO_LOCALE_KEY, p -> toLocale);
            } else {//to JulianDay,no need to output pattern and Locale
                TemporalField toTemporalField = valueOf(toCalendarParameter).getTemporalField();
                actionContext.get(TO_CALENDAR_TYPE_KEY, p -> toTemporalField);
            }

            //init an instance 'JulianDayConverter' when the converter is from JulianDay
            if (!isFromChronology) {
                JulianDayConverter julianDayConvert;
                if (isToChronology) {//convert JulianDay to ISO Calendar and use default output pattern.
                    julianDayConvert = new JulianDayConverter(actionContext.get(FROM_CALENDAR_TYPE_KEY), actionContext.get(TO_CALENDAR_TYPE_KEY), DEFAULT_OUTPUT_PATTERN, ISO.getDefaultLocale());
                } else {
                    julianDayConvert = new JulianDayConverter((TemporalField) actionContext.get(FROM_CALENDAR_TYPE_KEY), (TemporalField) actionContext.get(TO_CALENDAR_TYPE_KEY));
                }
                actionContext.get(JULIAN_DAY_CONVERT_KEY, p -> julianDayConvert);
            }
        }
    }

    private List<DatePattern> compileFromDatePattern(ActionContext actionContext) {
        if (actionContext.getParameters() == null) {
            return emptyList();
        }
        final RowMetadata rowMetadata = actionContext.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(actionContext.getColumnId());
        return Providers.get().getPatterns(column.getStatistics().getPatternFrequencies());
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        // Change the date calendar
        final String value = row.get(columnId);
        if (StringUtils.isBlank(value)) {
            return;
        }

        try {
            String newValue = null;
            if (isFromChronology) {//it is From Chronology
                AbstractChronology fromCalendarTypeKey = context.get(FROM_CALENDAR_TYPE_KEY);
                Locale fromLocaleKey = context.get(FROM_LOCALE_KEY);
                String fromPattern = parseDateFromPatterns(value, context.get(FROM_DATE_PATTERNS_KEY),
                        fromCalendarTypeKey, fromLocaleKey);
                org.talend.dataquality.converters.DateCalendarConverter dateConverter =getDateCalendarConverterInstance(fromPattern,context);
                if(dateConverter!=null){
                    newValue = dateConverter.convert(value);
                }
            } else {//it is From JulianDay.JulianDay->Chronology OR JulianDay->JulianDay
                JulianDayConverter julianDayConvert = context.get(JULIAN_DAY_CONVERT_KEY);
                newValue = julianDayConvert.convert(value);
            }
            if (StringUtils.isNotEmpty(newValue) && StringUtils.isNotBlank(newValue)) {
                row.set(ActionsUtils.getTargetColumnId(context), newValue);
            }
        } catch (DateTimeException e) {
            // cannot parse the date, let's leave it as is
            LOGGER.debug("Unable to parse date {}.", value);
        }
    }

    /**
     * Create instance DateCalendarConverter only once for each pattern. It is used to covert from Chronology.
     */
    private org.talend.dataquality.converters.DateCalendarConverter getDateCalendarConverterInstance(String fromPattern,ActionContext context) {
        if (!isFromChronology || StringUtils.isEmpty(fromPattern)) {
            return null;
        }
        org.talend.dataquality.converters.DateCalendarConverter dateConvert=dateCalendarConverterMap.get(fromPattern);
        if (dateConvert != null) {
            return dateConvert;
        }
        AbstractChronology fromCalendarTypeKey = context.get(FROM_CALENDAR_TYPE_KEY);
        Locale fromLocaleKey = context.get(FROM_LOCALE_KEY);
        if (isToChronology) {//Chronology->Chronology
            dateConvert = new org.talend.dataquality.converters.DateCalendarConverter(
                    fromPattern, fromPattern, fromCalendarTypeKey, context.get(TO_CALENDAR_TYPE_KEY),
                    fromLocaleKey, context.get(TO_LOCALE_KEY));
            dateCalendarConverterMap.put(fromPattern, dateConvert);
            return dateConvert;
        } else {//Chronology->TemporalField
            JulianDayConverter julianDayConvert = new JulianDayConverter(fromCalendarTypeKey, fromPattern, fromLocaleKey, context.get(TO_CALENDAR_TYPE_KEY));
            dateCalendarConverterMap.put(fromPattern, julianDayConvert);
            return julianDayConvert;
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.NEED_STATISTICS_PATTERN);
    }

    /**
     * Parse the date from the given patterns and chronology.
     *
     * @param value      the text to parse.
     * @param patterns   the patterns to use.
     * @param chronology the chronology to use.
     * @return the parsed date pattern
     */
    public static String parseDateFromPatterns(String value, List<DatePattern> patterns, AbstractChronology chronology,
                                               Locale locale) {

        // take care of the null value
        if (value == null) {
            throw new DateTimeException("cannot parse null"); //$NON-NLS-1$
        }

        for (DatePattern pattern : patterns) {
            final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                    .appendPattern(pattern.getPattern()).toFormatter().withChronology(chronology).withLocale(locale);
            try {
                TemporalAccessor temporal = formatter.parse(value);
                ChronoLocalDate cDate = chronology.date(temporal);
                LocalDate.from(cDate);
                return pattern.getPattern();
            } catch (DateTimeException e) {
                LOGGER.trace("Unable to parse date '{}' using LocalDate.", value, e);
            }
        }
        throw new DateTimeException("'" + value + "' does not match any known pattern");
    }

    /**
     * Enum CalendarUnit.There are 2 types of Calendar which are Chronology and TemporalField.
     */
    public enum CalendarUnit {
        //Chronology date.
        ISO(true, "IsoChronology", IsoChronology.INSTANCE, Locale.US),
        HIJRI(true, "HijrahChronology", HijrahChronology.INSTANCE, new Locale("ar")),
        JAPANESE(true, "JapaneseChronology", JapaneseChronology.INSTANCE, Locale.JAPANESE),
        MINGUO(true, "MinguoChronology", MinguoChronology.INSTANCE, Locale.TRADITIONAL_CHINESE),
        THAI_BUDDHIST(true, "ThaiBuddhistChronology", ThaiBuddhistChronology.INSTANCE, new Locale("th")),
        //Julian day.
        JULIAN_DAY(false, "JulianDay", JulianFields.JULIAN_DAY),
        MODIFIED_JULIAN_DAY(false, "ModifiedJulianDay", JulianFields.MODIFIED_JULIAN_DAY),
        RATA_DIE(false, "RataDie", JulianFields.RATA_DIE),
        EPOCH_DAY(false, "EpochDay", ChronoField.EPOCH_DAY);

        private final String displayName;

        private transient AbstractChronology chronologyType;

        private Locale defaultLocale;

        /**
         * TemporalField includes JulianDay,ModifiedJulianDay,RataDie,EpochDay in current.
         */
        private transient TemporalField temporalField;

        /**
         * It is a Chronology or a TemporalField instance.
         */
        private final boolean isChronology;

        CalendarUnit(boolean isChronology, String displayName, AbstractChronology calendarType, Locale defaultLocale) {
            this.displayName = displayName;
            this.chronologyType = calendarType;
            this.defaultLocale = defaultLocale;
            this.isChronology = isChronology;
        }

        CalendarUnit(boolean isChronology, String displayName, TemporalField temporalField) {
            this.displayName = displayName;
            this.temporalField = temporalField;
            this.isChronology = isChronology;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public AbstractChronology getCalendarType() {
            return chronologyType;
        }

        public TemporalField getTemporalField() {
            return temporalField;
        }

        public Locale getDefaultLocale() { return defaultLocale; }

        public boolean isChronology() {  return isChronology;  }
    }

}
