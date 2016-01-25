package org.talend.dataprep.dataset.service.analysis;

import org.talend.dataprep.dataset.service.analysis.asynchronous.AsynchronousDataSetAnalyzer;
import org.talend.dataprep.dataset.service.analysis.synchronous.SynchronousDataSetAnalyzer;

/**
 * Represents a component to analyze data set content. Analyzers are split into 2 categories:
 * <ul>
 * <li>Synchronous: see {@link SynchronousDataSetAnalyzer}</li>
 * <li>Asynchronous: see {@link AsynchronousDataSetAnalyzer}</li>
 * </ul>
 * Synchronous analyzers are called during data set creation and executed sequentially. Asynchronous can execute
 * concurrently and should listen to a JMS queue.
 * 
 * @see SynchronousDataSetAnalyzer
 * @see AsynchronousDataSetAnalyzer
 * @see org.talend.dataprep.dataset.service.DataSetService#queueEvents(String, Class[])
 */
public interface DataSetAnalyzer {

    /**
     * Analyze data available in data set with <code>dataSetId</code> id.
     *
     * @param dataSetId A Data Set id. Implementations are responsible to check if data set still exists at the moment
     * it's called.
     */
    void analyze(String dataSetId);

}
