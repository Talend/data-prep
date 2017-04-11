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
import static org.talend.dataprep.api.type.Type.DATE;

import java.time.DateTimeException;
import java.time.chrono.*;
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
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter.Builder;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

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

    protected static final String FROM_MODE_CUSTOM = "from_custom_mode";

    protected static final String FROM_CUSTOM_PATTERN = "from_custom_pattern";

    protected static final String FROM_CALENDER_TYPE_PARAMETER = "from_calender_type";

    protected static final String TO_CALENDER_TYPE_PARAMETER = "to_calender_type";

    private static final String FROM_DATE_PATTERNS_KEY = "from_date_patterns_key";

    private static final String FROM_CALENDER_TYPE_KEY = "from_calender_type_key";

    private static final String TO_CALENDER_TYPE_KEY = "to_calender_type_key";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.CONVERSIONS.getDisplayName();
    }

    @Override public boolean acceptField(ColumnMetadata column) {
        final String domain = column.getDomain().toUpperCase();
        return DATE.equals(Type.get(column.getType())) || SemanticCategoryEnum.DATE.name().equals(domain);
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        //@formatter:off
        parameters.add(Builder.builder()
                .name(FROM_CALENDER_TYPE_PARAMETER)
                .item(ChronologyUnit.ISO.name(), ChronologyUnit.ISO.toString())
                .item(ChronologyUnit.HIJRI.name(), ChronologyUnit.HIJRI.toString())
                .item(ChronologyUnit.JAPANESE.name(), ChronologyUnit.JAPANESE.toString())
                .item(ChronologyUnit.MINGUO.name(), ChronologyUnit.MINGUO.toString())
                .item(ChronologyUnit.THAI_BUDDHIST.name(), ChronologyUnit.THAI_BUDDHIST.toString())
                .defaultValue(ChronologyUnit.ISO.name())
                .build());

        parameters.add(Builder.builder()
                .name(TO_CALENDER_TYPE_PARAMETER)
                .item(ChronologyUnit.ISO.name(), ChronologyUnit.ISO.toString())
                .item(ChronologyUnit.HIJRI.name(), ChronologyUnit.HIJRI.toString())
                .item(ChronologyUnit.JAPANESE.name(), ChronologyUnit.JAPANESE.toString())
                .item(ChronologyUnit.MINGUO.name(), ChronologyUnit.MINGUO.toString())
                .item(ChronologyUnit.THAI_BUDDHIST.name(), ChronologyUnit.THAI_BUDDHIST.toString())
                .defaultValue(ChronologyUnit.MINGUO.name())
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
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final String columnId = actionContext.getColumnId();
            final ColumnMetadata column = rowMetadata.getById(columnId);

            actionContext.get(FROM_DATE_PATTERNS_KEY, p -> compileFromDatePattern(actionContext));

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

        // Change the date calender
        final String value = row.get(columnId);
        if (StringUtils.isBlank(value)) {
            return;
        }

        try {
            String fromPattern = DateParser.parseDateFromPatterns(value, context.get(FROM_DATE_PATTERNS_KEY),
                    context.get(FROM_CALENDER_TYPE_KEY));

            if (fromPattern != null) {
                row.set(columnId,
                        new org.talend.dataquality.converters.DateCalendarConverter(fromPattern, fromPattern, context
                                .get(FROM_CALENDER_TYPE_KEY), context.get(TO_CALENDER_TYPE_KEY)).convert(value));
            }
        } catch (DateTimeException e) {
            // cannot parse the date, let's leave it as is
            LOGGER.debug("Unable to parse date {}.", value, e);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.NEED_STATISTICS_PATTERN);
    }

    void compileDatePattern(ActionContext actionContext) {
        // no op, just to override the default behaviour
    }

    /**
     * enum Chronology.
     */
    public enum ChronologyUnit {
        ISO("IsoChronology", IsoChronology.INSTANCE),
        HIJRI("HijrahChronology", HijrahChronology.INSTANCE),
        JAPANESE("JapaneseChronology", JapaneseChronology.INSTANCE),
        MINGUO("MinguoChronology", MinguoChronology.INSTANCE),
        THAI_BUDDHIST("ThaiBuddhistChronology", ThaiBuddhistChronology.INSTANCE);

        private final String displayName;

        private final transient AbstractChronology chronologyType;

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
