package org.talend.dataprep.transformation.pipeline;

import java.io.Serializable;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * Links together {@link Node nodes}. Each implementation is responsible for handling correct
 * {@link RuntimeLink#emit(DataSetRow, RowMetadata)} and {@link RuntimeLink#signal(Signal)} methods.
 */
public interface Link extends Serializable {

    /**
     * Visit the implementation of the {@link Link}.
     *
     * @param visitor A {@link Visitor} to visit the whole pipeline structure.
     */
    void accept(Visitor visitor);

    RuntimeLink exec();

    default Node getTarget() {
        return null;
    }
}
