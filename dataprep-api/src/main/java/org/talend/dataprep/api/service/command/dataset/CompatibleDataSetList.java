package org.talend.dataprep.api.service.command.dataset;

import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;
import static org.talend.dataprep.util.SortAndOrderHelper.Order;
import static org.talend.dataprep.util.SortAndOrderHelper.Sort;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class CompatibleDataSetList extends GenericCommand<InputStream> {

    /**
     * Constructor.
     *
     * @param dataSetId the dataset id.
     * @param sort how to sort the datasets.
     * @param order the order to apply to the sort.
     */
    // private constructor to ensure the IoC
    private CompatibleDataSetList(String dataSetId, Sort sort, Order order) {
        super(GenericCommand.DATASET_GROUP);

        try {
            execute(() -> onExecute(dataSetId, sort, order));
            onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_LIST_DATASETS, e));
            on(HttpStatus.NO_CONTENT, HttpStatus.ACCEPTED).then(emptyStream());
            on(HttpStatus.OK).then(pipeStream());

        } catch (Exception e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    private HttpRequestBase onExecute(String dataSetId, Sort sort, Order order) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/datasets/" + dataSetId + "/compatibledatasets");
            uriBuilder.addParameter("dataSetId", dataSetId);
            uriBuilder.addParameter("sort", sort.camelName());
            uriBuilder.addParameter("order", order.camelName());

            return new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
