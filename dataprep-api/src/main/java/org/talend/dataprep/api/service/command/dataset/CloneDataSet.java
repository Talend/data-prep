package org.talend.dataprep.api.service.command.dataset;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import static org.talend.dataprep.api.service.command.common.Defaults.*;

/**
 * Command to clone a dataset.
 */
@Component
@Scope("request")
public class CloneDataSet extends GenericCommand<String> {

    /**
     * Constructor.
     *
     * @param client the http client to use.
     * @param dataSetId the requested dataset id.
     * @param name optional
     */
    public CloneDataSet(HttpClient client, String dataSetId, String name) {
        super(PreparationAPI.DATASET_GROUP, client);
        execute(() -> {
            try {
                URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/datasets/clone/" + dataSetId);
                if (name != null) {
                    uriBuilder.addParameter("name", name);
                }
                return new HttpGet(uriBuilder.build());
            } catch (URISyntaxException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        });
        onError((e) -> new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_DATASET_CONTENT, e,
                ExceptionContext.build().put("id", dataSetId)));
        on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyString());
        on(HttpStatus.OK).then(asString());
    }

}
