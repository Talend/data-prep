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

package org.talend.dataprep.dataset.adapter.commands;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.command.Defaults.convertResponse;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

/**
 * Get a dataset by id using the legacy dataprep dataset API.
 */
@Component(value = "DataSetGetMetadata#legacy")
@Scope(SCOPE_PROTOTYPE)
public class DataSetGetMetadataLegacy extends GenericCommand<DataSetMetadata> {

    private final String dataSetId;

    @Autowired
    private DataSetContentLimit limit;

    /**
     * Private constructor to ensure the use of IoC
     *
     * @param dataSetId the dataset id to get.
     */
    private DataSetGetMetadataLegacy(final String dataSetId) {
        super(GenericCommand.DATASET_GROUP);
        this.dataSetId = dataSetId;

        onError(Defaults.passthrough());
        on(HttpStatus.NO_CONTENT).then(asNull());
    }

    @PostConstruct
    private void initConfiguration() {
        if (limit.limitContentSize()) {
            this.configureLimitedDataset(dataSetId);
        } else {
            this.configureSampleDataset(dataSetId);
        }
    }

    private void configureLimitedDataset(final String dataSetId) {
        URI build;
        try {
            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl);
            uriBuilder.setPath(uriBuilder.getPath() + "/datasets/" + dataSetId + "/metadata");
            build = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }
        execute(() -> new HttpGet(build));

        on(HttpStatus.OK).then((req, res) -> {
            try {
                final DataSet dataSet = objectMapper.readerFor(DataSet.class).readValue(res.getEntity().getContent());
                return dataSet.getMetadata();
            } catch (IOException e) {
                throw new TDPException(UNEXPECTED_EXCEPTION, e);
            } finally {
                req.releaseConnection();
            }
        });
    }

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
