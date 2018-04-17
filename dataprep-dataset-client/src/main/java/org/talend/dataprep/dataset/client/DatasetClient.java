package org.talend.dataprep.dataset.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.valueOf;

// mimicing spring Crudrepository
@Service
public class DatasetClient {

    @Value("${dataset.api.url}")
    private String datasetApiUrl;

    @Autowired
    private HttpClient httpClient;

    /** Mapper to read dataset server responses bodies. */
    // We are not here to share the dataset specific mapper config with dataprep HTTP APIs.
    private ObjectMapper objectMapper = new ObjectMapper();

    public DatasetClient() {
        // For compatibility with future dataset models:
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }


    public Dataset findOne(String datasetId) {
        try {
            URIBuilder uriBuilder = new URIBuilder(datasetApiUrl + "/datasets/" + datasetId);
            return execute(new HttpGet(uriBuilder.build()), Defaults.convertResponse(objectMapper, Dataset.class));
        } catch (URISyntaxException e) {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    public List<Dataset> findAll() {
        try {
            URIBuilder uriBuilder = new URIBuilder(datasetApiUrl + "/datasets");
            return Arrays.asList(execute(new HttpGet(uriBuilder.build()), Defaults.convertResponse(objectMapper, Dataset[].class)));
        } catch (URISyntaxException e) {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    public boolean exists(String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    long count() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

	void delete(String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

	void delete(Dataset entity) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // I would be happy to use GenericCommand if to was not so cluttered with dataprep specific autowired things.
    // This class is written to call dataprep services APIs.

    // We must handle sthis cases :
    //
    // HTTP Response Code	    Description
    // 200 (Ok)	the request     has been succesfully executed
    // 401 (Unauthorized)	    the user is not authenticated
    // 403 (Forbidden)	        the user is not entitled
    // 404 (Not found)	        the dataset was not found
    //
    // See https://github.com/Talend/dataset/blob/master/docs/api/dataset-api.md#411-get-apiv1datasetsdatasetid
    private <T> T execute(HttpRequestBase httpRequest, BiFunction<HttpRequestBase, HttpResponse, T> successResponseHandler) {
        HttpResponse execute;
        try {
            execute = httpClient.execute(httpRequest);
        } catch (IOException e) {
            throw new TalendRuntimeException(org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
        StatusLine statusLine = execute.getStatusLine();
        if (valueOf(statusLine.getStatusCode()).is2xxSuccessful()) {
            return successResponseHandler.apply(httpRequest, execute);
        } else if  (valueOf(statusLine.getStatusCode()).is4xxClientError()) {
            // business error, handle
            switch (statusLine.getStatusCode()) {
            case HttpStatus.SC_UNAUTHORIZED:
            case HttpStatus.SC_FORBIDDEN:
            case HttpStatus.SC_NOT_FOUND:
            case HttpStatus.SC_CONFLICT:
                // handle dataset server errors
            default:
                throw new TalendRuntimeException(org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION);
            }
        } else {
            // unexpected server error
            throw new TalendRuntimeException(org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION);
        }
    }
}
