package org.talend.dataprep.api.service.command.dataset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.APIErrorCodes;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.CloneInputStream;
import org.talend.dataprep.api.service.command.ReleasableInputStream;
import org.talend.dataprep.api.service.command.common.DataPrepCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.TDPExceptionContext;
import org.talend.dataprep.exception.json.JsonErrorCode;
import org.talend.dataprep.preparation.store.ContentCache;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Scope("request")
public class DataSetGet extends DataPrepCommand<InputStream> {

    @Autowired
    ContentCache contentCache;

    private final String dataSetId;

    private final boolean metadata;

    private final boolean columns;

    public DataSetGet(HttpClient client, String dataSetId, boolean metadata, boolean columns) {
        super(PreparationAPI.TRANSFORM_GROUP, client);
        this.dataSetId = dataSetId;
        this.metadata = metadata;
        this.columns = columns;
    }

    @Override
    protected InputStream run() throws Exception {

        final HttpGet contentRetrieval = new HttpGet(datasetServiceUrl + "/datasets/" + dataSetId + "/content/?metadata="
                + metadata + "&columns=" + columns);
        final HttpResponse response = client.execute(contentRetrieval);
        return handleResponse(response, contentRetrieval);
    }

    private InputStream handleResponse(final HttpResponse response, final HttpGet contentRetrieval) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        // Look if initial data set content was previously cached
        final Preparation preparation = Preparation.defaultPreparation(dataSetId);
        if (contentCache.has(preparation.id(), Step.ROOT_STEP.id())) {
            return contentCache.get(preparation.id(), Step.ROOT_STEP.id());
        }
        // No cache, query the data set service for content
        if (statusCode >= 200 && statusCode < 400) {
            if (statusCode == HttpStatus.SC_NO_CONTENT) {
                // Immediately release connection
                contentRetrieval.releaseConnection();
                return new ByteArrayInputStream(new byte[0]);
            } else if (statusCode == HttpStatus.SC_OK) {
                final OutputStream cacheEntry = contentCache.put(preparation.id(), Step.ROOT_STEP.id(), ContentCache.TimeToLive.DEFAULT);
                final InputStream content = response.getEntity().getContent();
                final InputStream dataSetInput = new ReleasableInputStream(content, contentRetrieval::releaseConnection);
                return new CloneInputStream(dataSetInput, cacheEntry);
            }
        } else if (statusCode >= 400) { // Error (4xx & 5xx codes)
            final ObjectMapper build = builder.build();
            final JsonErrorCode errorCode = build.reader(JsonErrorCode.class).readValue(response.getEntity().getContent());
            errorCode.setHttpStatus(statusCode);
            throw new TDPException(errorCode);
        }
        Exception cause = new Exception(response.getStatusLine().getStatusCode() + response.getStatusLine().getReasonPhrase());
        throw new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_DATASET_CONTENT, cause, TDPExceptionContext.build().put("id",
                dataSetId));
    }
}
