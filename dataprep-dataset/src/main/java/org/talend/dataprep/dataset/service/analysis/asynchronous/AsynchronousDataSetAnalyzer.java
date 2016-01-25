package org.talend.dataprep.dataset.service.analysis.asynchronous;

import org.talend.dataprep.dataset.service.analysis.DataSetAnalyzer;

public interface AsynchronousDataSetAnalyzer extends DataSetAnalyzer {

    /**
     * @return A String that contains a JMS queue name (the one this implementation is listening to).
     */
    String destination();
}
