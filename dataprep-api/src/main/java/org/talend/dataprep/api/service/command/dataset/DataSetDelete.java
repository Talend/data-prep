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

package org.talend.dataprep.api.service.command.dataset;

import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.DATASET_STILL_IN_USE;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.command.preparation.CheckDatasetUsage;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;

/**
 * Delete the dataset if it's not used by any preparation.
 */
@Component
@Scope("request")
public class DataSetDelete extends GenericCommand<Void> {

    /**
     * This class' logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataSetDelete.class);

    /**
     * Default constructor.
     *
     * @param dataSetId The dataset id to delete.
     */
    private DataSetDelete(final String dataSetId) {
        super(GenericCommand.DATASET_GROUP);
        execute(() -> onExecute(dataSetId));
        onError(e -> new TDPException(
                APIErrorCodes.UNABLE_TO_DELETE_DATASET,
                e,
                ExceptionContext.build().put("dataSetId", dataSetId)
        ));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute(final String dataSetId) {
        final boolean isDatasetUsed = isDatasetUsed(dataSetId);

        // if the dataset is used by preparation(s), the deletion is forbidden
        if (isDatasetUsed) {
            LOG.debug("DataSet {} is used by {} preparation(s) and cannot be deleted", dataSetId);
            final ExceptionContext context = ExceptionContext.build().put("dataSetId", dataSetId);
            throw new TDPException(DATASET_STILL_IN_USE, context);
        }
        return new HttpDelete(datasetServiceUrl + "/datasets/" + dataSetId);
    }

    private boolean isDatasetUsed(final String dataSetId) {
        return context.getBean(CheckDatasetUsage.class, dataSetId).execute();
    }
}
