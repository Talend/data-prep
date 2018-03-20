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

package org.talend.dataprep.transformation.actions.datablending;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.actions.datablending.Lookup.Parameters.LOOKUP_DS_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.LightweightExportableDataSet;
import org.talend.dataprep.transformation.actions.PrototypeScope;

/**
 * A default implementation of {@link LookupRowMatcher}.
 */
@PrototypeScope
public class DefaultLookupRowMatcher implements LookupRowMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLookupRowMatcher.class);

    private final LightweightExportableDataSet cache;

    private final String datasetId;

    private DataSetRow emptyRow;

    // Used in dynamic instantiation
    public DefaultLookupRowMatcher(HashMap<String, String> parameters) {
        this.datasetId = parameters.get(LOOKUP_DS_ID.getKey());
        this.cache = LookupDatasetsManager.get(datasetId);

        if (cache == null) {
            throw new IllegalArgumentException("The lookup data set could not be found");
        }

        if (cache.getMetadata() != null) {
            emptyRow = getEmptyRow(cache.getMetadata().getColumns());
        } else {
            LOGGER.warn("The data set with id '" + datasetId + "' has no metadata");
            Optional<Map<String, String>> optionalFirstRow = cache.getRecords().values().stream().findFirst();
            if (optionalFirstRow.isPresent()) {
                Map<String, String> firstRow = optionalFirstRow.get();
                List<ColumnMetadata> columns = IntStream.range(0, firstRow.size()).mapToObj(i -> {
                    ColumnMetadata columnMetadata = new ColumnMetadata();
                    columnMetadata.setName("COL" + i);
                    return columnMetadata;
                }).collect(Collectors.toList());

                emptyRow = getEmptyRow(columns);
            } else {
                LOGGER.warn("The data set with id '" + datasetId + "' has no records");
            }
        }
        if (cache.getRecords().isEmpty()) {
            LOGGER.warn("The  lookup data set identified with");
        }

    }

    @Override
    public DataSetRow getMatchingRow(String joinOn, String joinValue) {
        Map<String, String> values = cache.getRecords().get(joinValue);
        if (values != null) {
            LOGGER.debug("Looking for value" + joinValue + " and found " + values.values());
            return new DataSetRow(cache.getMetadata(), values);
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
