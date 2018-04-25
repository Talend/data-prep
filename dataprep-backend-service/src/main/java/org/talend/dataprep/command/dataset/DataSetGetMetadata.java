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

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.dataset.DataSetMetadataBuilder;
import org.talend.dataprep.dataset.adapter.ApiDatasetClient;
import org.talend.dataprep.dataset.adapter.Dataset;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

@Component
@Scope(SCOPE_PROTOTYPE)
public class DataSetGetMetadata extends GenericCommand<DataSetMetadata> {

    private final String dataSetId;

    @Autowired
    private DataSetContentLimit limit;

    @Autowired
    private DataSetMetadataBuilder dataSetMetadataBuilder;

    @Autowired
    private ApiDatasetClient datasetClient;

    /**
     * Private constructor to ensure the use of IoC
     *
     * @param dataSetId the dataset id to get.
     */
    private DataSetGetMetadata(final String dataSetId) {
        super(GenericCommand.DATASET_GROUP);
        this.dataSetId = dataSetId;

        onError(Defaults.passthrough());
        on(HttpStatus.NO_CONTENT).then(asNull());
    }

    @PostConstruct
    private void initConfiguration() {
        URI build;
        try {
            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl);
            uriBuilder.setPath(uriBuilder.getPath() + "/api/v1/datasets/" + dataSetId);
            build = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
        execute(() -> new HttpGet(build));

        on(HttpStatus.OK).then((req, res) -> {
            try {
                Dataset dataset = objectMapper.readValue(res.getEntity().getContent(), Dataset.class);

                RowMetadata rowMetadata = datasetClient.getDataSetRowMetadata(dataSetId);

                DataSetMetadata metadata = dataSetMetadataBuilder.metadata() //
                        .id(dataset.getId()) //
                        .name(dataset.getLabel()) //
                        .created(dataset.getCreated()) //
                        .modified(dataset.getUpdated()) //
                        .build();
                metadata.setRowMetadata(rowMetadata);
                return metadata;
            } catch (IOException e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            } finally {
                req.releaseConnection();
            }
        });

    private void configureSampleDataset(String dataSetId) {
        URI uri;
        try {
            final URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl);
            uriBuilder.setPath(uriBuilder.getPath() + "/datasets/" + dataSetId + "/sample/metadata");
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }

        execute(() -> new HttpGet(uri));
        on(HttpStatus.OK).then(convertResponse(objectMapper, DataSetMetadata.class));
    }

}
