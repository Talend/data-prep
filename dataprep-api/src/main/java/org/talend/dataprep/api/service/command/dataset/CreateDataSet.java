package org.talend.dataprep.api.service.command.dataset;

import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.APIErrorCodes;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.DataPrepCommand;
import org.talend.dataprep.exception.TDPException;

import com.netflix.hystrix.HystrixCommand;

/**
 * Command used to create a dataset. Basically pass through all data to the DataSet low level API.
 */
@Component
@Scope("request")
public class CreateDataSet extends DataPrepCommand<String> {

    /** The dataset name. */
    private final String name;

    /** The dataset content or import parameters in json for remote datasets. */
    private final InputStream dataSetContent;

    /** The dataset content type. */
    private final String contentType;

    /**
     * Default constructor.
     *
     * @param client http client.
     * @param name name of the dataset.
     * @param contentType content-type of the dataset.
     * @param dataSetContent Dataset content or import parameters in json for remote datasets.
     */
    private CreateDataSet(HttpClient client, String name, String contentType, InputStream dataSetContent) {
        super(PreparationAPI.DATASET_GROUP, client);
        this.name = name;
        this.contentType = contentType;
        this.dataSetContent = dataSetContent;
    }

    /**
     * @see HystrixCommand#run()
     */
    @Override
    protected String run() throws Exception {

        HttpPost contentCreation = new HttpPost(datasetServiceUrl + "/datasets/?name=" + URLEncoder.encode(name, "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
        contentCreation.addHeader("Content-Type", contentType); //$NON-NLS-1$

        try {
            contentCreation.setEntity(new InputStreamEntity(dataSetContent));
            HttpResponse response = client.execute(contentCreation);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200) {
                if (statusCode == HttpStatus.SC_NO_CONTENT) {
                    return StringUtils.EMPTY;
                } else if (statusCode == HttpStatus.SC_OK) {
                    return IOUtils.toString(response.getEntity().getContent());
                }
            }
        } finally {
            contentCreation.releaseConnection();
        }
        throw new TDPException(APIErrorCodes.UNABLE_TO_CREATE_DATASET);
    }
}
