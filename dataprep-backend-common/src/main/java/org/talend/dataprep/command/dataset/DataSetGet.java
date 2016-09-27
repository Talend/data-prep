// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.command.Defaults.emptyStream;
import static org.talend.dataprep.command.Defaults.pipeStream;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_RETRIEVE_DATASET_CONTENT;
import static org.talend.dataprep.exception.error.DataSetErrorCodes.DATASET_DOES_NOT_EXIST;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.dataset.store.content.DataSetContentLimit;
import org.talend.dataprep.exception.TDPException;

/**
 * Command to get a dataset.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class DataSetGet extends GenericCommand<InputStream> {

    private final boolean fullContent;

    private final String dataSetId;

    private final boolean includeInternalContent;

    @Autowired
    private DataSetContentLimit limit;

    /**
     * Constructor.
     *
     * @param dataSetId the requested dataset id.
     */
    public DataSetGet(final String dataSetId, final boolean fullContent, final boolean includeInternalContent) {
        super(DATASET_GROUP);
        this.fullContent = fullContent;
        this.dataSetId = dataSetId;
        this.includeInternalContent = includeInternalContent;

        on(HttpStatus.NOT_FOUND).then((req, res) -> {
            throw new TDPException(DATASET_DOES_NOT_EXIST, build().put("id", dataSetId));
        });
        on(HttpStatus.NO_CONTENT).then(emptyStream());
        on(HttpStatus.OK).then(pipeStream());
        onError(e -> new TDPException(UNABLE_TO_RETRIEVE_DATASET_CONTENT, e, build().put("id", dataSetId)));
    }

    @PostConstruct
    private void initConfiguration() {
        if (limit.limitContentSize() || fullContent) {
            this.configureLimitedDataset(dataSetId);
        } else {
            this.configureSampleDataset(dataSetId);
        }
    }

    private void configureLimitedDataset(final String dataSetId) {
        execute(() -> {
            final String url = datasetServiceUrl + "/datasets/" + dataSetId + "/content?metadata=true&includeInternalContent=" + includeInternalContent;
            return new HttpGet(url);
        });
    }

    private void configureSampleDataset(final String dataSetId) {
        execute(() -> new HttpGet(datasetServiceUrl + "/datasets/" + dataSetId + "/sample"));
    }
}
