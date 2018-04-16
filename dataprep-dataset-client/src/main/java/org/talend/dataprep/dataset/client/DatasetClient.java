package org.talend.dataprep.dataset.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
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
import java.util.function.BiFunction;

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

    public Dataset getById(String datasetId) {
        try {
            URIBuilder uriBuilder = new URIBuilder(datasetApiUrl + "/datasets/" + datasetId);
            return execute(new HttpGet(uriBuilder.build()), Defaults.convertResponse(objectMapper, Dataset.class));
        } catch (URISyntaxException e) {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    // I would be happy to use GenericCommand if to was not so cluttered with dataprep specific autowired things.
    // This class is written to call dataprep services APIs.
    private <T> T execute(HttpRequestBase httpRequest, BiFunction<HttpRequestBase, HttpResponse, T> successResponseHandler) {
        HttpResponse execute;
        try {
            execute = httpClient.execute(httpRequest);
        } catch (IOException e) {
            throw new TalendRuntimeException(org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
        if (org.springframework.http.HttpStatus.valueOf(execute.getStatusLine().getStatusCode()).is2xxSuccessful()) {
            return successResponseHandler.apply(httpRequest, execute);
        } else {
            // handle dataset server errors

            throw new TalendRuntimeException(org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION);
        }
    }
}
