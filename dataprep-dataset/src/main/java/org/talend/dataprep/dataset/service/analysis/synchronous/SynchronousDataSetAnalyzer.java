package org.talend.dataprep.dataset.service.analysis.synchronous;

import org.talend.dataprep.dataset.service.analysis.DataSetAnalyzer;

/**
 * Synchronous analyzers are performed synchronously while the dataset creation. Hence their execution time must be as
 * fast as possible.
 */
public interface SynchronousDataSetAnalyzer extends DataSetAnalyzer {

    /**
     * @return A arbitrary number to order synchronous analysis (one synchronous analysis may depend on other
     * implementation results).
     */
    int order();
}
