package org.talend.dataprep.transformation.actions.datablending;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * An interface for lookup operations.
 */
public interface LookupRowMatcher {

    /**
     * Return the matching row from the loaded dataset.
     *
     * @param joinOn the column id to join on.
     * @param joinValue the join value.
     * @return the matching row or an empty one based on the {@link #getRowMetadata()}.
     */
    DataSetRow getMatchingRow(String joinOn, String joinValue);

    /**
     * @return The {@link RowMetadata row} for returning empty row.
     */
    RowMetadata getRowMetadata();
}
