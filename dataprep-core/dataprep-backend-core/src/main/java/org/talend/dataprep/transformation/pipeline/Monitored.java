package org.talend.dataprep.transformation.pipeline;

import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * To be implemented to indicate pipeline element can monitor performance.
 */
public interface Monitored {

    /**
     * @return The total time spent in the pipeline element (in milliseconds).
     */
    long getTotalTime();

    /**
     * @return The number of {@link DataSetRow row} processed by this pipeline element.
     */
    long getCount();
}
