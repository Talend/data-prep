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

import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.command.Defaults.convertResponse;
import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.command.Defaults;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.exception.TDPException;

@Component
@Scope("prototype")
public class DataSetGetMetadata extends GenericCommand<DataSetMetadata> {

    private final String dataSetId;

    @Autowired
    private DataSetContentLimit limit;

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
        if (limit.limitContentSize()) {
            this.configureLimitedDataset(dataSetId);
        } else {
            this.configureSampleDataset(dataSetId);
        }
    }

    /**
     * Private constructor to ensure the use of IoC
     *
     * @param relativePath to get the sample or not
     */
    private void configureDataset(String relativePath) {
        URI builtUri;
        try {
            final URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl);
            uriBuilder.setPath(uriBuilder.getPath() + relativePath);
            builtUri = uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        }

        execute(() -> new HttpGet(builtUri));
        on(HttpStatus.OK).then(convertResponse(objectMapper, DataSetMetadata.class));
    }

    private void configureLimitedDataset(final String dataSetId) {
        configureDataset("/datasets/" + dataSetId + "/metadata");
    }

    private void configureSampleDataset(String dataSetId) {
        configureDataset("/datasets/" + dataSetId + "/sample/metadata");
    }

}
