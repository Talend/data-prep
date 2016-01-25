package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.api.service.command.common.Defaults.pipeStream;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.PreparationCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.PreparationErrorCodes;

/**
 * Command used to retrieve the preparation content.
 */
@Component
@Scope("request")
public class PreparationGetContent extends PreparationCommand<InputStream> {

    /** The preparation id. */
    private final String id;

    /** The preparation version. */
    private final String version;

    /** Optional sample size (if null or <=0, the full preparation content is returned). */
    private Long sample;

    /**
     * Private constructor to ensure the IoC.
     *
     * @param client the http client to use.
     * @param id the preparation id.
     * @param version the preparation version.
     */
    private PreparationGetContent(HttpClient client, String id, String version) {
        this(client, id, version, null);
    }

    /**
     * Constructor with sample size specified.
     *
     * @param client the http client to use.
     * @param id the preparation id.
     * @param version the preparation version.
     */
    private PreparationGetContent(HttpClient client, String id, String version, Long sample) {
        super(APIService.PREPARATION_GROUP, client);
        this.id = id;
        this.version = version;
        this.sample = sample;
        execute(this::onExecute);
        on(HttpStatus.OK).then(pipeStream());
    }

    private HttpRequestBase onExecute() {

        // get the preparation to extract the dataset id (DataSetId should be sent from the frontend instead)
        final Preparation preparation;
        try {
            preparation = getPreparation(id);
        } catch (IOException e) {
            throw new TDPException( //
                    PreparationErrorCodes.PREPARATION_DOES_NOT_EXIST, //
                    ExceptionContext.build().put("id", id));
        }
        String datasetId = preparation.getDataSetId();

        String uri = transformationServiceUrl + "/apply/preparation/" + id + "/dataset/" + datasetId + "/JSON";
        uri += "?stepId=" + version;
        if (sample != null) {
            uri += "&sample=" + sample;
        }
        return new HttpGet(uri);
    }

}
