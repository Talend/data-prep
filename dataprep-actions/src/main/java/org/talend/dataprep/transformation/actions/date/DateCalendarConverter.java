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

import static java.util.Collections.*;
import static org.apache.commons.lang.StringUtils.*;

import java.time.DateTimeException;
import java.time.chrono.AbstractChronology;
import java.time.chrono.HijrahChronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.MinguoChronology;
import java.time.chrono.ThaiBuddhistChronology;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + DateCalendarConverter.ACTION_NAME)
public class DateCalendarConverter extends AbstractDate implements ColumnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateCalendarConverter.class);

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "date_calendar_converter"; //$NON-NLS-1$

    /**
     * Action parameters:
     */
    protected static final String FROM_MODE = "from_pattern_mode"; //$NON-NLS-1$

    protected static final String FROM_MODE_BEST_GUESS = "unknown_separators"; //$NON-NLS-1$

    protected static final String FROM_MODE_CUSTOM = "from_custom_mode"; //$NON-NLS-1$

    protected static final String FROM_CUSTOM_PATTERN = "from_custom_pattern"; //$NON-NLS-1$

    protected static final String FROM_CALENDER_TYPE_PARAMETER = "from_calender_type"; //$NON-NLS-1$

    protected static final String TO_CALENDER_TYPE_PARAMETER = "to_calender_type"; //$NON-NLS-1$

    protected static final String CUSTOM = "custom"; //$NON-NLS-1$

    /**
     * Keys for action context:
     */
    private static final String FROM_DATE_PATTERNS_KEY = "from_date_patterns_key"; //$NON-NLS-1$

    private static final String FROM_CALENDER_TYPE_KEY = "from_calender_type_key"; //$NON-NLS-1$

    private static final String TO_CALENDER_TYPE_KEY = "to_calender_type_key"; //$NON-NLS-1$

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return "Conversions"; //$NON-NLS-1$
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                .name(FROM_CALENDER_TYPE_PARAMETER)
                .item(ChronologyUnit.ISO.name(), ChronologyUnit.ISO.toString()) 
                .item(ChronologyUnit.Hijrah.name(), ChronologyUnit.Hijrah.toString()) 
                .item(ChronologyUnit.Japanese.name(), ChronologyUnit.Japanese.toString()) 
                .item(ChronologyUnit.Minguo.name(), ChronologyUnit.Minguo.toString()) 
                .item(ChronologyUnit.ThaiBuddhist.name(), ChronologyUnit.ThaiBuddhist.toString()) 
                .defaultValue(ChronologyUnit.ISO.name()) 
                .build());
        
        parameters.add(SelectParameter.Builder.builder()
                .name(FROM_MODE)
                .item(FROM_MODE_BEST_GUESS, FROM_MODE_BEST_GUESS)
                .item(FROM_MODE_CUSTOM, FROM_MODE_CUSTOM, new Parameter(FROM_CUSTOM_PATTERN, ParameterType.STRING, EMPTY, false, false))
                .defaultValue(FROM_MODE_BEST_GUESS)
                .build());
        
        parameters.add(SelectParameter.Builder.builder()
                .name(TO_CALENDER_TYPE_PARAMETER)
                .item(ChronologyUnit.ISO.name(), ChronologyUnit.ISO.toString()) 
                .item(ChronologyUnit.Hijrah.name(), ChronologyUnit.Hijrah.toString()) 
                .item(ChronologyUnit.Japanese.name(), ChronologyUnit.Japanese.toString()) 
                .item(ChronologyUnit.Minguo.name(), ChronologyUnit.Minguo.toString()) 
                .item(ChronologyUnit.ThaiBuddhist.name(), ChronologyUnit.ThaiBuddhist.toString()) 
                .defaultValue(ChronologyUnit.Hijrah.name()) 
                .build());
        
        parameters.add(SelectParameter.Builder.builder()
                .name(NEW_PATTERN)
                .item("yyyy-MM-dd", "datePatternISO")//$NON-NLS-1$ //$NON-NLS-2$
                .item("M/d/yy",     "datePatternUS") //$NON-NLS-1$ //$NON-NLS-2$
                .item("dd/MM/yy",   "datePatternFR") //$NON-NLS-1$ //$NON-NLS-2$
                .item("dd.MM.yy",   "datePatternDE") //$NON-NLS-1$ //$NON-NLS-2$
                .item("dd/MM/yy",   "datePatternGB") //$NON-NLS-1$ //$NON-NLS-2$
                .item("yy/MM/dd",   "datePatternJP") //$NON-NLS-1$ //$NON-NLS-2$
                .item("yyyy/MM/dd", "datePattern1")  //$NON-NLS-1$ //$NON-NLS-2$
                .item("dd/MM/yyyy", "datePattern2")  //$NON-NLS-1$ //$NON-NLS-2$
                .item("MM/dd/yyyy", "datePattern3")  //$NON-NLS-1$ //$NON-NLS-2$
                .item("yyyy MM dd", "datePattern4")  //$NON-NLS-1$ //$NON-NLS-2$
                .item("yyyyMMdd",   "datePattern5")  //$NON-NLS-1$ //$NON-NLS-2$
                .item(CUSTOM, CUSTOM_PATTERN_PARAMETER)
                .defaultValue("yyyy-MM-dd") //$NON-NLS-1$
                .build());
        //@formatter:on

        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {

            AbstractChronology fromCalenderType = ChronologyUnit.valueOf(
                    actionContext.getParameters().get(FROM_CALENDER_TYPE_PARAMETER)).getCalendarType();
            AbstractChronology toCalenderType = ChronologyUnit.valueOf(
                    actionContext.getParameters().get(TO_CALENDER_TYPE_PARAMETER)).getCalendarType();
            actionContext.get(FROM_CALENDER_TYPE_KEY, p -> fromCalenderType);
            actionContext.get(TO_CALENDER_TYPE_KEY, p -> toCalenderType);

            compileDatePattern(actionContext);

            // register the new pattern in column stats as most used pattern, to be able to process date action more
            // efficiently later
            final DatePattern newPattern = actionContext.get(COMPILED_DATE_PATTERN);
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final String columnId = actionContext.getColumnId();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            final Statistics statistics = column.getStatistics();

            actionContext.get(FROM_DATE_PATTERNS_KEY, p -> compileFromDatePattern(actionContext));

            final PatternFrequency newPatternFrequency = statistics.getPatternFrequencies().stream()
                    .filter(patternFrequency -> StringUtils.equals(patternFrequency.getPattern(), newPattern.getPattern()))
                    .findFirst().orElseGet(() -> {
                        final PatternFrequency newPatternFreq = new PatternFrequency(newPattern.getPattern(), 0);
                        statistics.getPatternFrequencies().add(newPatternFreq);
                        return newPatternFreq;
                    });

            long mostUsedPatternCount = getMostUsedPatternCount(column);
            newPatternFrequency.setOccurrences(mostUsedPatternCount + 1);
            rowMetadata.update(columnId, column);
        }
    }

    private List<DatePattern> compileFromDatePattern(ActionContext actionContext) {
        if (actionContext.getParameters() == null) {
            return emptyList();
        }
        switch (actionContext.getParameters().get(FROM_MODE)) {
        case FROM_MODE_BEST_GUESS:
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final ColumnMetadata column = rowMetadata.getById(actionContext.getColumnId());
            return Providers.get().getPatterns(column.getStatistics().getPatternFrequencies());
        case FROM_MODE_CUSTOM:
            List<DatePattern> fromPatterns = new ArrayList<>();
            fromPatterns.add(new DatePattern(actionContext.getParameters().get(FROM_CUSTOM_PATTERN)));
            return fromPatterns;
        default:
            return emptyList();
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final DatePattern newPattern = context.get(COMPILED_DATE_PATTERN);

        // Change the date calender
        final String value = row.get(columnId);
        if (StringUtils.isBlank(value)) {
            return;
        }

        try {
            String fromPattern = Providers.get().parseDateFromPatterns(value, context.get(FROM_DATE_PATTERNS_KEY),
                    context.get(FROM_CALENDER_TYPE_KEY));
            if (fromPattern != null) {
                row.set(columnId,
                        new org.talend.dataquality.converters.DateCalendarConverter(fromPattern, newPattern.getPattern(), context
                                .get(FROM_CALENDER_TYPE_KEY), context.get(TO_CALENDER_TYPE_KEY)).convert(value));
            }
        } catch (DateTimeException e) {
            // cannot parse the date, let's leave it as is
            LOGGER.debug("Unable to parse date {}.", value, e);
        }
    }

    /**
     * Return the count of the most used pattern.
     *
     * @param column the column to work on.
     * @return the count of the most used pattern.
     */
    private long getMostUsedPatternCount(ColumnMetadata column) {
        final List<PatternFrequency> patternFrequencies = column.getStatistics().getPatternFrequencies();
        if (patternFrequencies.isEmpty()) {
            return 1;
        }
        patternFrequencies.sort((p1, p2) -> Long.compare(p2.getOccurrences(), p1.getOccurrences()));
        return patternFrequencies.get(0).getOccurrences();
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.METADATA_CHANGE_TYPE, Behavior.NEED_STATISTICS_PATTERN);
    }

    /**
     * enum Chronology.
     */
    public enum ChronologyUnit {
        ISO("IsoChronology", IsoChronology.INSTANCE), //$NON-NLS-1$
        Hijrah("HijrahChronology", HijrahChronology.INSTANCE), //$NON-NLS-1$
        Japanese("JapaneseChronology", JapaneseChronology.INSTANCE), //$NON-NLS-1$
        Minguo("MinguoChronology", MinguoChronology.INSTANCE), //$NON-NLS-1$
        ThaiBuddhist("ThaiBuddhistChronology", ThaiBuddhistChronology.INSTANCE); //$NON-NLS-1$

        private final String displayName;

        private final AbstractChronology chronologyType;

        ChronologyUnit(String displayName, AbstractChronology calendarType) {
            this.displayName = displayName;
            this.chronologyType = calendarType;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public AbstractChronology getCalendarType() {
            return chronologyType;
        }
    }

}
