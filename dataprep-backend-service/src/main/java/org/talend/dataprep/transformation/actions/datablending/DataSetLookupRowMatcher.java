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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.dataset.adapter.DatasetClient;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.talend.dataprep.transformation.actions.datablending.Lookup.Parameters.LOOKUP_DS_ID;

@Component
@Scope(SCOPE_PROTOTYPE)
public class DataSetLookupRowMatcher implements DisposableBean, LookupRowMatcher {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetLookupRowMatcher.class);

    @Autowired
    private DatasetClient datasetClient;

    /** The dataset id to lookup. */
    private String datasetId;

    /** Lookup row iterator. */
    private Iterator<DataSetRow> lookupIterator;

    /** Default empty row for the parsed lookup dataset. */
    private Map<String, String> defaultEmptyRow;

    private DataSetRow defaultEmptyDatasetRow;

    /** Cache of dataset row. */
    private Map<String, Map<String, String>> cacheMatchingValues = new HashMap();

    /** Cache values with no match of dataset row. */
    private Set<String> cacheNoMatchingValues = new HashSet<>();

    private String joinOnColumn;

    private List<LookupSelectedColumnParameter> selectedColumns;

    private Stream<DataSetRow> records;

    DataSetLookupRowMatcher() {
    }

    /**
     * Default constructor.
     *
     * @param parameters parameters that contains the the dataset id to lookup.
     */
    public DataSetLookupRowMatcher(Map<String, String> parameters) {
        this.datasetId = parameters.get(LOOKUP_DS_ID.getKey());
        joinOnColumn = parameters.get(Lookup.Parameters.LOOKUP_JOIN_ON.getKey());
        selectedColumns = Lookup.getColsToAdd(parameters);
    }

    void setJoinOnColumn(String joinOnColumn) {
        this.joinOnColumn = joinOnColumn;
    }

    void setSelectedColumns(List<LookupSelectedColumnParameter> selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

    void setLookupIterator(Iterator<DataSetRow> lookupIterator) {
        this.lookupIterator = lookupIterator;
    }

    /**
     * Open the connection to get the dataset content and init the row iterator.
     */
    @PostConstruct
    private void init() {
        LOGGER.debug("Opening the dataset {}", datasetId);
        DataSet lookup = datasetClient.getDataSet(datasetId, true);
        records = lookup.getRecords();
        this.lookupIterator = records.iterator();
        initEmptyRow(lookup.getMetadata().getRowMetadata().getColumns());
    }

    /**
     * Gently close the input stream as well as the http client.
     */
    @Override
    public void destroy() {
        records.close();
        LOGGER.debug("connection to {} closed", datasetId);
    }

    /**
     * Return the matching row from the loaded dataset.
     *
     * @param joinOn the column id to join on.
     * @param joinValue the join value.
     * @return the matching row or an empty one based on the
     */
    @Override
    public Map<String, String> getMatchingRow(String joinOn, String joinValue) {

        if (joinValue == null || cacheNoMatchingValues.contains(joinValue)) {
            LOGGER.debug("join value is null or there is no matching values existing, returning empty row");
            return defaultEmptyRow;
        }

        // first, let's look in the cache not Empty Values
        if (cacheMatchingValues.containsKey(joinValue)) {
            return cacheMatchingValues.get(joinValue);
        }

        // if the value is not cached, let's update the cached matching Values
        List<ColumnMetadata> filteredColumns = null;
        while (lookupIterator.hasNext()) {
            DataSetRow nextRow = lookupIterator.next();
            final String nextRowJoinValue = nextRow.get(joinOn);
            LOGGER.debug("Search Match for row {} on joinOn {} : joinValue {} / nextRowJoinValue {}",
                    nextRow.getTdpId(), joinOn, joinValue, nextRowJoinValue);

            if (!cacheMatchingValues.containsKey(nextRowJoinValue)) {
                // update the cacheValues no matter what so that the next joinValue may be already cached !
                if (filteredColumns == null) {
                    final List<String> selectedColumnIds = selectedColumns
                            .stream() //
                            .map(LookupSelectedColumnParameter::getId) //
                            .collect(Collectors.toList());
                    filteredColumns =
                            nextRow
                                    .getRowMetadata() //
                                    .getColumns() //
                                    .stream() //
                                    .filter(c -> !joinOnColumn.equals(c.getId())
                                            && selectedColumnIds.contains(c.getId())) //
                                    .collect(Collectors.toList());
                }

                Map<String, String> values = nextRow.values().entrySet().stream().collect(
                        Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));

                LOGGER.trace("row found and cached for {} -> {}", nextRowJoinValue, nextRow.values());
                cacheMatchingValues.put(nextRowJoinValue, values);
            }

            // if matching row is found, let's stop here
            if (StringUtils.equals(joinValue, nextRowJoinValue)) {
                return cacheMatchingValues.get(nextRowJoinValue);
            }
        }

        // the join value was not found and the matching cached values map is fully updated, so let's update no matching
        // map values and return an empty row
        cacheNoMatchingValues.add(joinValue);
        LOGGER.trace("no row found for {}, returning an empty row", joinValue);
        return this.defaultEmptyRow;
    }

    @Override
    public RowMetadata getRowMetadata() {
        return defaultEmptyDatasetRow.getRowMetadata();
    }

    /**
     * Init an empty default row based on the given dataset metadata.
     *
     * @param columns the list of column metadata of rows within the data set
     */
    private void initEmptyRow(List<ColumnMetadata> columns) {
        RowMetadata rowMetadata = new RowMetadata(columns);
        defaultEmptyDatasetRow = new DataSetRow(rowMetadata);
        defaultEmptyRow = new HashMap<>();
        columns.forEach(column -> {
            defaultEmptyDatasetRow.set(column.getId(), EMPTY);
            defaultEmptyRow.put(column.getId(), EMPTY);
        });
    }

    @Override
    public String toString() {
        return "LookupRowMatcher{datasetId='" + datasetId + '}';
    }
}
