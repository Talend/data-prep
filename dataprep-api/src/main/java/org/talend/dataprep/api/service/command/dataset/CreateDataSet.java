package org.talend.dataprep.api.service.command.dataset;

import static org.talend.dataprep.api.service.command.common.Defaults.*;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.DataSetErrorCodes;

/**
 * Command used to create a dataset. Basically pass through all data to the DataSet low level API.
 */
@Component
@Scope("request")
public class CreateDataSet extends GenericCommand<String> {

    /**
     * Default constructor.
     *
     * @param client http client.
     * @param name name of the dataset.
     * @param contentType content-type of the dataset.
     * @param dataSetContent Dataset content or import parameters in json for remote datasets.
     */
    private CreateDataSet(HttpClient client, String name, String contentType, InputStream dataSetContent, String folderPath) {
        super(PreparationAPI.DATASET_GROUP, client);
        execute(() -> onExecute(name, contentType, dataSetContent, folderPath));
        onError(e -> {
            if (e instanceof TDPException) {
                // Go for a pass-through for "UNSUPPORTED CONTENT"
                final TDPException tdpException = (TDPException) e;
                final ErrorCode errorCode = tdpException.getCode();
                if (errorCode.getCode().equals(DataSetErrorCodes.UNSUPPORTED_CONTENT.getCode())) {
                    return passthrough().apply(e);
                }
            }
            return new TDPException(APIErrorCodes.UNABLE_TO_CREATE_DATASET, e);
        });
        on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyString());
        on(HttpStatus.OK).then(asString());
    }

    private HttpRequestBase onExecute(String name, String contentType, InputStream dataSetContent, String folderPath) {
        try {
            URIBuilder uriBuilder = new URIBuilder( datasetServiceUrl + "/datasets"  );
            uriBuilder.addParameter( "name", name);
            uriBuilder.addParameter( "folderPath", folderPath);
            final HttpPost post = new HttpPost(uriBuilder.build());
            post.addHeader("Content-Type", contentType); //$NON-NLS-1$
            post.setEntity(new InputStreamEntity(dataSetContent));
            return post;
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
