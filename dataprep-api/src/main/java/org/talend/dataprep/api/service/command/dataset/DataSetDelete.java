package org.talend.dataprep.api.service.command.dataset;

import static org.talend.dataprep.api.service.command.common.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.DATASET_STILL_IN_USE;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.api.service.command.preparation.PreparationListForDataSet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Delete the dataset if it's not used by any preparation.
 */
@Component
@Scope("request")
public class DataSetDelete extends GenericCommand<Void> {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DataSetDelete.class);

    /** Dataset id. */
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
        execute(() -> onExecute(dataSetId));
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_DELETE_DATASET, e,
                ExceptionContext.build().put("dataSetId", dataSetId)));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(String dataSetId) {
        try {
            List<Preparation> preparations = getPreparationsForDataSet();
            // if the dataset is used by preparation(s), the deletion is forbidden
            if (preparations.size() > 0) {
                LOG.debug("DataSet {} is used by {} preparation(s) and cannot be deleted", dataSetId, preparations.size());
                final ExceptionContext context = ExceptionContext.build() //
                        .put("dataSetId", dataSetId) //
                        .put("preparations", preparations);
                throw new TDPException(DATASET_STILL_IN_USE, context);
            }
            return new HttpDelete(datasetServiceUrl + "/datasets/" + dataSetId);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * @return List of preparation(s) that use this dataset or en empty list if there's none.
     */
    private List<Preparation> getPreparationsForDataSet() throws IOException {
        // execute preparation api
        PreparationListForDataSet preparationsForDataSet = context.getBean(PreparationListForDataSet.class, client, dataSetId);
        InputStream jsonInput = preparationsForDataSet.execute();
        // parse and return the response
        ObjectMapper mapper = builder.build();
        return mapper.readValue(jsonInput, new TypeReference<List<Preparation>>() {
        });
    }

}
