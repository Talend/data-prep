// ============================================================================
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

package org.talend.dataprep.api.filter;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.date.DateParser;

import java.util.function.Predicate;

import static org.talend.dataprep.api.filter.JSONFilterWalker.walk;

/**
 * An implementation of {@link FilterService} that reads a JSON-based language.
 *
 * @see JSONFilterWalker
 */
public class SimpleFilterService implements FilterService {

    private DateParser dateParser;

    @Override
    public Predicate<DataSetRow> build(String filterAsString, RowMetadata rowMetadata) {
        final PredicateCallback callback = new PredicateCallback();
        if (dateParser != null) {
            callback.setDateParser(dateParser);
        }
        return walk(filterAsString, rowMetadata, callback);
    }

    void setDateParser(DateParser dateParser) {
        this.dateParser = dateParser;
    }
}
