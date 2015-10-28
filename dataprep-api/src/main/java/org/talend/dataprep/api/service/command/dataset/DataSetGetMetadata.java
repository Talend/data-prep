package org.talend.dataprep.api.service.command.dataset;

import static org.talend.dataprep.api.service.command.common.Defaults.asNull;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.service.PreparationAPI;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Scope("request")
public class DataSetGetMetadata extends GenericCommand<DataSetMetadata> {

    private DataSetGetMetadata(HttpClient client, String dataSetId) {
        super(PreparationAPI.DATASET_GROUP, client);
        execute(() -> new HttpGet(datasetServiceUrl + "/datasets/" + dataSetId + "/metadata"));
        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_DATASET_METADATA, e));
        on(HttpStatus.NO_CONTENT).then(asNull());
        on(HttpStatus.OK).then((req, res) -> {
            try {
                ObjectMapper mapper = builder.build();
                final DataSet dataSet = mapper.reader(DataSet.class).readValue(res.getEntity().getContent());
                final DataSetMetadata metadata = dataSet.getMetadata();
                metadata.setRowMetadata(new RowMetadata(dataSet.getColumns()));
                return metadata;
            } catch (IOException e) {
                throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
            }
        });
    }

}
