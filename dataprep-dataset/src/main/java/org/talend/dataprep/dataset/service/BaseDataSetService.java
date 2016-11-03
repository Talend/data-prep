package org.talend.dataprep.dataset.service;

import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.DataSetErrorCodes;

public abstract class BaseDataSetService {

    static void assertDataSetMetadata(DataSetMetadata dataSetMetadata, String dataSetId) {
        if (dataSetMetadata == null) {
            throw new TDPException(DataSetErrorCodes.DATASET_DOES_NOT_EXIST, ExceptionContext.build().put("id", dataSetId));
        }
        if (dataSetMetadata.getLifecycle().importing()) {
            // Data set is being imported, this is an error since user should not have an id to a being-created
            // data set (create() operation is a blocking operation).
            final ExceptionContext context = ExceptionContext.build().put("id", dataSetId); //$NON-NLS-1$
            throw new TDPException(DataSetErrorCodes.UNABLE_TO_SERVE_DATASET_CONTENT, context);
        }
    }
}
