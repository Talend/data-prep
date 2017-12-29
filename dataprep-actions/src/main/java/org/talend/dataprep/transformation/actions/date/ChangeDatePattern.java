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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Change the date pattern on a 'date' column.
 */
@Action(ChangeDatePattern.ACTION_NAME)
public class ChangeDatePattern extends AbstractDate implements ColumnAction {

    /** Action name. */
    public static final String ACTION_NAME = "change_date_pattern"; //$NON-NLS-1$

    /**
     * Action parameters:
     */
    protected static final String FROM_MODE = "from_pattern_mode"; //$NON-NLS-1$

    protected static final String FROM_MODE_CUSTOM = "from_custom_mode"; //$NON-NLS-1$

    protected static final String FROM_CUSTOM_PATTERN = "from_custom_pattern"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeDatePattern.class);

    protected static final String FROM_MODE_BEST_GUESS = "unknown_separators"; //$NON-NLS-1$

    /**
     * Keys for action context:
     */
    private static final String FROM_DATE_PATTERNS = "from_date_patterns";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    protected static final String NEW_COLUMN_SUFFIX = "_format_changed";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));

        // @formatter:off
        parameters.add(SelectParameter.selectParameter(locale)
                .name(FROM_MODE)
                .item(FROM_MODE_BEST_GUESS, FROM_MODE_BEST_GUESS)
                .item(FROM_MODE_CUSTOM, FROM_MODE_CUSTOM, Parameter.parameter(locale).setName(FROM_CUSTOM_PATTERN).setType(ParameterType.STRING).setDefaultValue(EMPTY).setCanBeBlank(false).build(this))
                .defaultValue(FROM_MODE_BEST_GUESS)
                .build(this ));
        // @formatter:on

        parameters.addAll(getParametersForDatePattern(locale));
        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, singletonList(ActionsUtils.additionalColumn().withName(actionContext.getColumnName() + NEW_COLUMN_SUFFIX)));
        }
        if (actionContext.getActionStatus() == OK) {
            compileDatePattern(actionContext);

            // register the new pattern in column stats as most used pattern, to be able to process date action more
            // efficiently later
            final DatePattern newPattern = actionContext.get(COMPILED_DATE_PATTERN);
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final String columnId = actionContext.getColumnId();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            final Statistics statistics = column.getStatistics();

            final ColumnMetadata targetColumn = rowMetadata.getById(ActionsUtils.getTargetColumnId(actionContext));
            if (!Objects.equals(targetColumn.getId(), columnId)) {
                targetColumn.setStatistics(statistics);
            }

            actionContext.get(FROM_DATE_PATTERNS, p -> compileFromDatePattern(actionContext));

            final PatternFrequency newPatternFrequency = statistics.getPatternFrequencies().stream()
                    .filter(patternFrequency -> StringUtils.equals(patternFrequency.getPattern(), newPattern.getPattern()))
                    .findFirst().orElseGet(() -> {
                        final PatternFrequency newPatternFreq = new PatternFrequency(newPattern.getPattern(), 0);
                        statistics.getPatternFrequencies().add(newPatternFreq);
                        return newPatternFreq;
                    });

            long mostUsedPatternCount = getMostUsedPatternCount(column);
            newPatternFrequency.setOccurrences(mostUsedPatternCount + 1);
            rowMetadata.update(ActionsUtils.getTargetColumnId(actionContext), targetColumn);
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

        // Change the date pattern
        final String value = row.get(columnId);
        if (StringUtils.isBlank(value)) {
            return;
        }
        try {
            LocalDateTime date = Providers.get().parseDateFromPatterns(value, context.get(FROM_DATE_PATTERNS));

            if (date != null) {
                row.set(ActionsUtils.getTargetColumnId(context), newPattern.getFormatter().format(date));
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

}
