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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.command.dataset.DataSetGet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 */
@Component
@Scope("prototype")
public class DataSetLookupRowMatcher implements DisposableBean, LookupRowMatcher {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetLookupRowMatcher.class);

    /** The dataprep ready jackson builder. */
    @Autowired
    @Lazy // needed to prevent a circular dependency
    private ObjectMapper mapper;

    /** The Spring application context. */
    @Autowired
    private ApplicationContext context;

    /** The dataset id to lookup. */
    private String datasetId;

    /** The dataset lookup input stream. */
    private InputStream input;

    /** Lookup row iterator. */
    private Iterator<DataSetRow> lookupIterator;

    /** Default empty row for the parsed lookup dataset. */
    private DataSetRow emptyRow;

    /** Cache of dataset row. */
    private Map<String, DataSetRow> cache = new HashMap<>();

    /**
     * Default constructor.
     *
     * @param parameters parameters that contains the the dataset id to lookup.
     */
    public DataSetLookupRowMatcher(Map<String, String> parameters) {
        this.datasetId = parameters.get(LOOKUP_DS_ID.getKey());
    }

    /**
     * Open the connection to get the dataset content and init the row iterator.
     */
    @PostConstruct
    private void init() {

        final DataSetGet dataSetGet = context.getBean(DataSetGet.class, datasetId, true, true);

        LOGGER.debug("opening {}", datasetId);

        this.input = dataSetGet.execute();
        try {
            JsonParser jsonParser = mapper.getFactory().createParser(input);
            DataSet lookup = mapper.readerFor(DataSet.class).readValue(jsonParser);
            this.lookupIterator = lookup.getRecords().iterator();
            this.emptyRow = getEmptyRow(lookup.getMetadata().getRowMetadata().getColumns());
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_READ_LOOKUP_DATASET, e);
        }
    }

    /**
     * Gently close the input stream as well as the http client.
     */
    @Override
    public void destroy() {
        try {
            input.close();
        } catch (IOException e) {
            LOGGER.warn("Error cleaning LookupRowMatcher", e);
        }
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
    public DataSetRow getMatchingRow(String joinOn, String joinValue) {

        if (joinValue == null) {
            LOGGER.debug("join value is null, returning empty row");
            return emptyRow;
        }

        // first, let's look in the cache
        if (cache.containsKey(joinValue)) {
            return cache.get(joinValue);
        }

        // if the value is not cached, let's update the cache
        while (lookupIterator.hasNext()) {
            DataSetRow nextRow = lookupIterator.next();
            final String nextRowJoinValue = nextRow.get(joinOn);

            // update the cache no matter what so that the next joinValue may be already cached !
            if (!cache.containsKey(nextRowJoinValue)) {
                cache.put(nextRowJoinValue, nextRow.clone());
                LOGGER.trace("row found and cached for {} -> {}", nextRowJoinValue, nextRow.values());
            }

            // if matching row is found, let's stop here
            if (StringUtils.equals(joinValue, nextRowJoinValue)) {
                return nextRow;
            }
        }

        // the join value was not found and the cache is fully updated, so let's cache an empty row and return it
        cache.put(joinValue, this.emptyRow);
        LOGGER.trace("no row found for {}, returning an empty row", joinValue);
        return this.emptyRow;
    }

    @Override
    public RowMetadata getRowMetadata() {
        return emptyRow.getRowMetadata();
    }

    /**
     * Return an empty default row based on the given dataset metadata.
     *
     * @param columns the dataset to get build the row from.
     * @return an empty default row based on the given dataset metadata.
     */
    private DataSetRow getEmptyRow(List<ColumnMetadata> columns) {
        RowMetadata rowMetadata = new RowMetadata(columns);
        DataSetRow defaultRow = new DataSetRow(rowMetadata);
        columns.forEach(column -> defaultRow.set(column.getId(), EMPTY));
        return defaultRow;
    }

    @Override
    public String toString() {
        return "LookupRowMatcher{datasetId='" + datasetId + '}';
    }
}
