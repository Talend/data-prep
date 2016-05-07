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

import static org.talend.dataprep.api.type.Type.DATE;
import static org.talend.dataprep.transformation.api.action.metadata.date.DatePatternParamModel.COMPILED_DATE_PATTERN;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.PatternFrequency;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.datepattern.DatePattern;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractDate extends ActionMetadata {

    /** Component that parses dates. */
    @Autowired
    protected DateParser dateParser;

    private Logger logger = LoggerFactory.getLogger( getClass() );

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
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        final String domain = column.getDomain().toUpperCase();
        return DATE.equals(Type.get(column.getType())) || SemanticCategoryEnum.DATE.name().equals(domain);
    }


    /**
     * Return the count of the most used pattern.
     *
     * @param column the column to work on.
     * @return the count of the most used pattern.
     */
    long getMostUsedPatternCount(ColumnMetadata column) {
        final List<PatternFrequency> patternFrequencies = column.getStatistics().getPatternFrequencies();
        if (patternFrequencies.isEmpty()) {
            return 1;
        }
        patternFrequencies.sort((p1, p2) -> Long.compare(p2.getOccurrences(), p1.getOccurrences()));
        return patternFrequencies.get(0).getOccurrences();
    }

    void changeDatePattern( ActionContext actionContext) {
        //register the new pattern in column stats as most used pattern, to be able to process date action more efficiently later
        final DatePattern newPattern = actionContext.get( COMPILED_DATE_PATTERN);
        final RowMetadata rowMetadata = actionContext.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(actionContext.getColumnId());
        final Statistics statistics = column.getStatistics();

        final PatternFrequency newPatternFrequency = statistics.getPatternFrequencies()
            .stream()
            .filter(patternFrequency -> StringUtils.equals( patternFrequency.getPattern(), newPattern.getPattern()))
            .findFirst()
            .orElseGet(() -> {
                final PatternFrequency newPatternFreq = new PatternFrequency(newPattern.getPattern(), 0);
                statistics.getPatternFrequencies().add(newPatternFreq);
                return newPatternFreq;
            });

        long mostUsedPatternCount = getMostUsedPatternCount(column);
        newPatternFrequency.setOccurrences(mostUsedPatternCount + 1);
    }

    void changeDateValue( ActionContext context, DataSetRow row) {
        final String columnId = context.getColumnId();
        final DatePattern newPattern = context.get(COMPILED_DATE_PATTERN);

        // Change the date pattern
        final String value = row.get(columnId);
        if (StringUtils.isBlank(value)) {
            return;
        }
        try {
            final LocalDateTime date = dateParser.parse( value, context.getRowMetadata().getById( columnId));
            row.set(columnId, newPattern.getFormatter().format(date));
        } catch (DateTimeException e) {
            // cannot parse the date, let's leave it as is
            logger.debug("Unable to parse date {}.", value, e);
        }
    }

}
