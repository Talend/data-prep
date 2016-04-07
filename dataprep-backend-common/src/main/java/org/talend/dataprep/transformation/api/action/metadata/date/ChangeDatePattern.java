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
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.parameters.Parameter;

/**
 * Change the date pattern on a 'date' column.
 */
@Component(ChangeDatePattern.ACTION_BEAN_PREFIX + ChangeDatePattern.ACTION_NAME)
public class ChangeDatePattern extends AbstractDate implements ColumnAction, DatePatternParamModel {

    /** Action name. */
    public static final String ACTION_NAME = "change_date_pattern"; //$NON-NLS-1$
    public static final Logger LOGGER = LoggerFactory.getLogger(ChangeDatePattern.class);

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = super.getParameters();
        parameters.addAll(getParametersForDatePattern());
        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            compileDatePattern(actionContext);

            //register the new pattern in column stats as most used pattern, to be able to process date action more efficiently later
            final DatePattern newPattern = actionContext.get(COMPILED_DATE_PATTERN);
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final ColumnMetadata column = rowMetadata.getById(actionContext.getColumnId());
            final Statistics statistics = column.getStatistics();

            final PatternFrequency newPatternFrequency = statistics.getPatternFrequencies()
                    .stream()
                    .filter(patternFrequency -> StringUtils.equals(patternFrequency.getPattern(), newPattern.getPattern()))
                    .findFirst()
                    .orElseGet(() -> {
                        final PatternFrequency newPatternFreq = new PatternFrequency(newPattern.getPattern(), 0);
                        statistics.getPatternFrequencies().add(newPatternFreq);
                        return newPatternFreq;
                    });

            long mostUsedPatternCount = getMostUsedPatternCount(column);
            newPatternFrequency.setOccurrences(mostUsedPatternCount + 1);
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
            final LocalDateTime date = dateParser.parse(value, context.getRowMetadata().getById(columnId));
            row.set(columnId, newPattern.getFormatter().format(date));
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
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
