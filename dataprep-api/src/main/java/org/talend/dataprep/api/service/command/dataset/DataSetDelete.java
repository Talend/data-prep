package org.talend.dataprep.api.service.command.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.APIErrorCodes;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.DataPrepCommand;
import org.talend.dataprep.api.service.command.preparation.PreparationListForDataSet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.TDPExceptionContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;

import static org.talend.dataprep.api.APIErrorCodes.DATASET_STILL_IN_USE;

/**
 * Delete the dataset if it's not used by any preparation.
 */
@Component
@Scope("request")
public class DataSetDelete extends DataPrepCommand<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(DataSetDelete.class);

    private final String dataSetId;

    /**
     * Default constructor.
     *
     * @param client Http client for rest api calls.
     * @param dataSetId The dataset id to delete.
     */
    private DataSetDelete(HttpClient client, String dataSetId) {
        super(PreparationAPI.DATASET_GROUP, client);
        this.dataSetId = dataSetId;

    }

    /**
     * @see HystrixCommand#run()
     */
    @Override
    protected Void run() throws Exception {

        List<Preparation> preparations = getPreparationsForDataSet();

        // if the dataset is used by preparation(s), the deletion is forbidden
        if (preparations.size() > 0) {
            LOG.debug("DataSet {} is used by {} preparation(s) and cannot be deleted", dataSetId, preparations.size());
            throw new TDPException(DATASET_STILL_IN_USE, TDPExceptionContext.build()
                    .put("dataSetId", dataSetId).put("preparations", preparations));
        }

        return doDeleteDataSet();
    }

    /**
     * @return List of preparation(s) that use this dataset or en empty list if there's none.
     */
    private List<Preparation> getPreparationsForDataSet() throws IOException {

        // call preparation api
        PreparationListForDataSet preparationsForDataSet = context.getBean(PreparationListForDataSet.class, client, dataSetId);
        InputStream jsonInput = preparationsForDataSet.execute();

        // parse and return the response
        ObjectMapper mapper = builder.build();
        return mapper.readValue(jsonInput, new TypeReference<List<Preparation>>() {
        });
    }

    /**
     * Actual method that deletes the DataSet.
     * 
     * @throws IOException if an error occurs.
     */
    private Void doDeleteDataSet() throws IOException, TDPException {
        HttpDelete contentRetrieval = new HttpDelete(datasetServiceUrl + "/datasets/" + dataSetId);
        HttpResponse response = client.execute(contentRetrieval);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            return null;
        }
        throw new TDPException(APIErrorCodes.UNABLE_TO_DELETE_DATASET, TDPExceptionContext.build().put("dataSetId", dataSetId));
    }

}
