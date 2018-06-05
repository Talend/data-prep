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

import static org.talend.dataprep.api.filter.JSONFilterWalker.walk;

import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * An implementation of {@link FilterService} that reads a JSON-based language.
 *
 * @see JSONFilterWalker
 */
public class SimpleFilterService implements FilterService {

    @Override
    public Predicate<DataSetRow> build(String filterAsString, RowMetadata rowMetadata) {
        final PredicateCallback callback = new PredicateCallback();
        return walk(filterAsString, rowMetadata, callback);
    }
}
