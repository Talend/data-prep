package org.talend.dataprep.transformation.api.action.metadata.datablending;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 */
@Component
@Scope("prototype")
public class LookupRowMatcher {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LookupRowMatcher.class);

    /** The http client to use. */
    @Autowired
    private CloseableHttpClient httpClient;

    /** The dataprep ready jackson builder. */
    @Autowired
    @Lazy // needed to prevent a circular dependency
    private Jackson2ObjectMapperBuilder builder;

    /** Where to look for the lookup dataset. */
    private String url;

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
     * @param url where to load the lookup content.
     */
    public LookupRowMatcher(String url) {
        this.url = url;
    }

    /**
     * Open the connection to get the dataset content and init the row iterator.
     */
    @PostConstruct
    private void init() {

        LOGGER.debug("opening {}", url);

        this.input = getLookupContent(url);
        final ObjectMapper mapper = builder.build();
        try {
            JsonParser jsonParser = mapper.getFactory().createParser(input);
            DataSet lookup = mapper.reader(DataSet.class).readValue(jsonParser);
            this.lookupIterator = lookup.getRecords().iterator();
            this.emptyRow = getEmptyRow(lookup.getColumns());
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_READ_LOOKUP_DATASET, e);
        }
    }

    /**
     * Gently close the input stream as well as the http client.
     */
    @PreDestroy
    private void destroy() {
        try {
            input.close();
            httpClient.close();
        } catch (IOException e) {
            LOGGER.warn("Error cleaning LookupRowMatcher", e);
        }
        LOGGER.debug("{} closed", url);
    }

    /**
     * Return the matching row from the loaded dataset.
     *
     * @param joinOn the column id to join on.
     * @param joinValue the join value.
     * @return the matching row or an empty one based on the
     */
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
                LOGGER.debug("row found and cached for {} -> {}", nextRowJoinValue, nextRow.values());
            }

            // if matching row is found, let's stop here
            if (StringUtils.equals(joinValue, nextRowJoinValue)) {
                return nextRow;
            }
        }

        // the join value was not found and the cache is fully updated, so let's cache an empty row and return it
        cache.put(joinValue, this.emptyRow);
        LOGGER.debug("no row found for {}, returning an empty row", joinValue);
        return this.emptyRow;
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

    /**
     * Return the lookup dataset content from the given url.
     *
     * @param url where to load the dataset lookup content.
     * @return the lookup dataset content as input stream.
     */
    private InputStream getLookupContent(String url) {
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new IOException("error reading dataset lookup " + url + " -> " + response.getStatusLine());
            }

            LOGGER.debug("Lookup dataset read from {} ", url);
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_READ_LOOKUP_DATASET, e);
        }
    }

}
