// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.command.dataset;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;

import org.apache.avro.Schema;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.RowMetadataUtils;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

@Component
@Scope("prototype")
public class DataSetGetSchema extends GenericCommand<RowMetadata> {

    private final String dataSetId;

    /**
     * Private constructor to ensure the use of IoC
     *
     * @param dataSetId the dataset id to get.
     */
    private DataSetGetSchema(final String dataSetId) {
        super(GenericCommand.DATASET_GROUP);
        this.dataSetId = dataSetId;

        onError(e -> new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_DATASET_METADATA, e));
        on(HttpStatus.NO_CONTENT).then(asNull());
    }

    @PostConstruct
    private void initConfiguration() {
        URI datasetURI;
        try {
            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl);
            datasetURI = uriBuilder //
                    .setPath(uriBuilder.getPath() + "/api/v1/dataset-sample/" + dataSetId) //
                    .addParameter("offset", "0") //
                    .addParameter("limit", "1") //
                    .build();
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
        execute(() -> new HttpGet(datasetURI));

        on(HttpStatus.OK).then((req, res) -> {
            try {
                Schema schema = new Schema.Parser().parse(res.getEntity().getContent());
                return RowMetadataUtils.toRowMetadata(schema);
            } catch (IOException e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            } finally {
                req.releaseConnection();
            }
        });
    }

}
