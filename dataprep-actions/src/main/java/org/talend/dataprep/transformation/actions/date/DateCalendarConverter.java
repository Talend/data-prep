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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter.Builder;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.converters.JulianDayConverter;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.chrono.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.JulianFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.talend.dataprep.api.type.Type.DATE;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + DateCalendarConverter.ACTION_NAME)
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

    private static final String JULIAN_DAY_CONVERT_KEY="julian_day_convert_key";

    /**
     * A default output pattern when convert a JulianDay to Chronology
     */
    private static final String DEFAULT_OUTPUT_PATTERN="yyyy-MM-dd G";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.DATE.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        final String domain = column.getDomain().toUpperCase();
        return DATE.equals(Type.get(column.getType())) || SemanticCategoryEnum.DATE.name().equals(domain);
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        //@formatter:off
        parameters.add(Builder.builder()
                .name(FROM_CALENDAR_TYPE_PARAMETER)
                .item(CalendarUnit.ISO.name(), CalendarUnit.ISO.toString())
                .item(CalendarUnit.HIJRI.name(), CalendarUnit.HIJRI.toString())
                .item(CalendarUnit.JAPANESE.name(), CalendarUnit.JAPANESE.toString())
                .item(CalendarUnit.MINGUO.name(), CalendarUnit.MINGUO.toString())
                .item(CalendarUnit.THAI_BUDDHIST.name(), CalendarUnit.THAI_BUDDHIST.toString())
                .item(CalendarUnit.JULIAN_DAY.name(), CalendarUnit.JULIAN_DAY.toString())
                .item(CalendarUnit.MODIFIED_JULIAN_DAY.name(), CalendarUnit.MODIFIED_JULIAN_DAY.toString())
                .item(CalendarUnit.RATA_DIE.name(), CalendarUnit.RATA_DIE.toString())
                .item(CalendarUnit.EPOCH_DAY.name(), CalendarUnit.EPOCH_DAY.toString())
                .defaultValue(CalendarUnit.ISO.name())
                .build());

        parameters.add(Builder.builder()
                .name(TO_CALENDAR_TYPE_PARAMETER)
                .item(CalendarUnit.ISO.name(), CalendarUnit.ISO.toString())
                .item(CalendarUnit.HIJRI.name(), CalendarUnit.HIJRI.toString())
                .item(CalendarUnit.JAPANESE.name(), CalendarUnit.JAPANESE.toString())
                .item(CalendarUnit.MINGUO.name(), CalendarUnit.MINGUO.toString())
                .item(CalendarUnit.THAI_BUDDHIST.name(), CalendarUnit.THAI_BUDDHIST.toString())
                .item(CalendarUnit.JULIAN_DAY.name(), CalendarUnit.JULIAN_DAY.toString())
                .item(CalendarUnit.MODIFIED_JULIAN_DAY.name(), CalendarUnit.MODIFIED_JULIAN_DAY.toString())
                .item(CalendarUnit.RATA_DIE.name(), CalendarUnit.RATA_DIE.toString())
                .item(CalendarUnit.EPOCH_DAY.name(), CalendarUnit.EPOCH_DAY.toString())
                .defaultValue(CalendarUnit.MINGUO.name())
                .build());
        //@formatter:on

        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {

            boolean isFromChronology = CalendarUnit
                    .valueOf(actionContext.getParameters().get(FROM_CALENDAR_TYPE_PARAMETER)).isChronology();
            boolean isToChronology = CalendarUnit
                    .valueOf(actionContext.getParameters().get(TO_CALENDAR_TYPE_PARAMETER)).isChronology();
            if(isFromChronology){
                AbstractChronology fromCalendarType = CalendarUnit
                        .valueOf(actionContext.getParameters().get(FROM_CALENDAR_TYPE_PARAMETER)).getCalendarType();
                Locale fromLocale = CalendarUnit.valueOf(actionContext.getParameters().get(FROM_CALENDAR_TYPE_PARAMETER))
                        .getDefaultLocale();
                actionContext.get(FROM_CALENDAR_TYPE_KEY, p -> fromCalendarType);
                actionContext.get(FROM_LOCALE_KEY, p -> fromLocale);
                actionContext.get(FROM_DATE_PATTERNS_KEY, p -> compileFromDatePattern(actionContext));
            }else{//from JulianDay,no need to input pattern and Locale
                TemporalField fromTemporalField = CalendarUnit
                        .valueOf(actionContext.getParameters().get(FROM_CALENDAR_TYPE_PARAMETER)).getTemporalField();
                actionContext.get(FROM_CALENDAR_TYPE_KEY, p -> fromTemporalField);
            }

            if(isToChronology){
                AbstractChronology toCalendarType = CalendarUnit
                        .valueOf(actionContext.getParameters().get(TO_CALENDAR_TYPE_PARAMETER)).getCalendarType();
                Locale toLocale = CalendarUnit.valueOf(actionContext.getParameters().get(TO_CALENDAR_TYPE_PARAMETER))
                        .getDefaultLocale();
                actionContext.get(TO_CALENDAR_TYPE_KEY, p -> toCalendarType);
                actionContext.get(TO_LOCALE_KEY, p -> toLocale);
            }else{//to JulianDay,no need to output pattern and Locale
                TemporalField toTemporalField = CalendarUnit
                        .valueOf(actionContext.getParameters().get(TO_CALENDAR_TYPE_PARAMETER)).getTemporalField();
                actionContext.get(TO_CALENDAR_TYPE_KEY, p -> toTemporalField);
            }

            //init an instance 'JulianDayConverter' when it is from JulianDay
            if(!isFromChronology) {
                JulianDayConverter julianDayConvert;
                if (isToChronology) {
                    julianDayConvert = new JulianDayConverter( actionContext.get(FROM_CALENDAR_TYPE_KEY), (Chronology) actionContext.get(TO_CALENDAR_TYPE_KEY),DEFAULT_OUTPUT_PATTERN,null);
                } else {
                    julianDayConvert = new JulianDayConverter((TemporalField)actionContext.get(FROM_CALENDAR_TYPE_KEY), (TemporalField) actionContext.get(TO_CALENDAR_TYPE_KEY));
                }
                actionContext.get(JULIAN_DAY_CONVERT_KEY, p -> julianDayConvert);
            }
            // register the new pattern in column stats as most used pattern, to be able to process date action more
            // efficiently later
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final String columnId = actionContext.getColumnId();
            final ColumnMetadata column = rowMetadata.getById(columnId);



            rowMetadata.update(columnId, column);
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
            boolean isFromChronology = CalendarUnit
                    .valueOf(context.getParameters().get(FROM_CALENDAR_TYPE_PARAMETER)).isChronology();
            boolean isToChronology = CalendarUnit
                    .valueOf(context.getParameters().get(TO_CALENDAR_TYPE_PARAMETER)).isChronology();
            String newValue = null;
            if (isFromChronology) {//it is From Chronology
                String fromPattern = parseDateFromPatterns(value, context.get(FROM_DATE_PATTERNS_KEY),
                        context.get(FROM_CALENDAR_TYPE_KEY), context.get(FROM_LOCALE_KEY));
                if (StringUtils.isEmpty(fromPattern)) {
                    return;
                }
                if (isToChronology) {//Chronology->Chronology
                    org.talend.dataquality.converters.DateCalendarConverter date = new org.talend.dataquality.converters.DateCalendarConverter(
                            fromPattern, fromPattern, context.get(FROM_CALENDAR_TYPE_KEY), context.get(TO_CALENDAR_TYPE_KEY),
                            context.get(FROM_LOCALE_KEY), context.get(TO_LOCALE_KEY));
                    newValue = date.convert(value);
                } else {//JulianDay->Chronology
                    JulianDayConverter julianDayConvert = new JulianDayConverter(context.get(FROM_CALENDAR_TYPE_KEY), fromPattern, context.get(FROM_LOCALE_KEY), context.get(TO_CALENDAR_TYPE_KEY));
                    newValue = julianDayConvert.convert(value);
                }
            } else {//it is From JulianDay.JulianDay->Chronology OR JulianDay->JulianDay
                JulianDayConverter julianDayConvert = (JulianDayConverter) context.get(JULIAN_DAY_CONVERT_KEY);
                newValue = julianDayConvert.convert(value);
            }
            if (StringUtils.isNotEmpty(newValue) && StringUtils.isNotBlank(newValue)) {
                row.set(columnId, newValue);
            }

        } catch (DateTimeException e) {
            // cannot parse the date, let's leave it as is
            LOGGER.debug("Unable to parse date {}.", value);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.NEED_STATISTICS_PATTERN);
    }

    /**
     * Parse the date from the given patterns and chronology.
     *
     * @param value the text to parse.
     * @param patterns the patterns to use.
     * @param chronology
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
     * enum Chronology.
     */
    public enum CalendarUnit {
        //Chronology date.
        ISO(true,"IsoChronology", IsoChronology.INSTANCE, Locale.US),
        HIJRI(true,"HijrahChronology", HijrahChronology.INSTANCE, new Locale("ar")),
        JAPANESE(true,"JapaneseChronology", JapaneseChronology.INSTANCE, Locale.JAPANESE),
        MINGUO(true,"MinguoChronology", MinguoChronology.INSTANCE, Locale.CHINESE),
        THAI_BUDDHIST(true,"ThaiBuddhistChronology", ThaiBuddhistChronology.INSTANCE, new Locale("th")),
        //Julian days.
        JULIAN_DAY(false,"JulianDay",JulianFields.JULIAN_DAY),
        MODIFIED_JULIAN_DAY(false,"ModifiedJulianDay",JulianFields.MODIFIED_JULIAN_DAY),
        RATA_DIE(false,"RataDie",JulianFields.RATA_DIE),
        EPOCH_DAY(false,"EpochDay", ChronoField.EPOCH_DAY);

        private String displayName;

        private transient AbstractChronology chronologyType;

        private Locale defaultLocale;

        private transient TemporalField temporalField;

        private boolean isChronology;

        CalendarUnit(boolean isChronology, String displayName, AbstractChronology calendarType, Locale defaultLocale) {
            this.displayName = displayName;
            this.chronologyType = calendarType;
            this.defaultLocale = defaultLocale;
            this.isChronology=isChronology;
        }

        CalendarUnit(boolean isChronology, String displayName, TemporalField temporalField) {
            this.displayName=displayName;
            this.temporalField=temporalField;
            this.isChronology=isChronology;
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

        public Locale getDefaultLocale() {
            return defaultLocale;
        }

        public boolean isChronology() {   return isChronology;  }
    }

}
