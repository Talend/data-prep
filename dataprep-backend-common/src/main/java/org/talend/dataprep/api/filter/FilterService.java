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

package org.talend.dataprep.api.filter;

import java.util.Map;
import java.util.function.Predicate;

import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.ActionFactory;

@FunctionalInterface
public interface FilterService {

    /**
     * Builds a {@link Predicate} to serve as filter for {@link DataSetRow rows}.
     *
     * @param filterAsString A filter as string that follow conditions as defined in
     * <a href="https://in.talend.com/9082609">MDM wiki</a>
     * @param rowMetadata Row metadata to used to obtain information (valid/invalid, types...)
     * @return A {@link Predicate} to be used to filter rows in actions. Empty or <code>null</code> returns a
     * "match all" predicate (i.e. no filtering).
     * @see ActionFactory#create(ActionDefinition, Map)
     */
    Predicate<DataSetRow> build(String filterAsString, RowMetadata rowMetadata);
}
