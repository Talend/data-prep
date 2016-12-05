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

package org.talend.dataprep.transformation.actions.datablending;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.actions.datablending.Lookup.Parameters.LOOKUP_DS_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * A default implementation of {@link LookupRowMatcher}.
 *
 */
public class DefaultLookupRowMatcher implements LookupRowMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLookupRowMatcher.class);

    private final Map<String, DataSetRow> cache;

    private final String datasetId;

    private DataSetRow emptyRow;

    // Used in dynamic instantiation
    public DefaultLookupRowMatcher(HashMap<String, String> parameters) {
        this.datasetId = parameters.get(LOOKUP_DS_ID.getKey());
        this.cache = LookupDatasetsManager.get(datasetId);


        Optional<DataSetRow> optionalFirstRow = cache.values().stream().findFirst();
        if (optionalFirstRow.isPresent()){
            DataSetRow firstRow = optionalFirstRow.get();
            if (firstRow != null && firstRow.getRowMetadata() != null && firstRow.getRowMetadata().getColumns() != null) {
                emptyRow = getEmptyRow(firstRow.getRowMetadata().getColumns());
            }
        }else{
            LOGGER.warn("The dataset with id '"+datasetId+"' has no row");
        }
    }

    @Override
    public DataSetRow getMatchingRow(String joinOn, String joinValue) {
        DataSetRow result = cache.get(joinValue);
        if (result != null) {
            LOGGER.debug("Looking for value" + joinValue + " and found " + result.values());
            return result;
        } else {
            LOGGER.debug("Looking for value" + joinValue + " and found: null");
            return emptyRow;
        }
    }

    /**
     * Returns an empty default row based on the given dataset metadata.
     *
     * @param columns the list of column metadata of rows within the data set
     * @return an empty default row based on the given list of column metadata.
     */
    private DataSetRow getEmptyRow(List<ColumnMetadata> columns) {
        RowMetadata rowMetadata = new RowMetadata(columns);
        DataSetRow defaultRow = new DataSetRow(rowMetadata);
        columns.forEach(column -> defaultRow.set(column.getId(), EMPTY));
        return defaultRow;
    }

    @Override
    public RowMetadata getRowMetadata() {
        return emptyRow.getRowMetadata();
    }
}
