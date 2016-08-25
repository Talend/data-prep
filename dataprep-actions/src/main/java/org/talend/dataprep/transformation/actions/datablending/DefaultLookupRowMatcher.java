package org.talend.dataprep.transformation.actions.datablending;

import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;

/**
 * A default implementation of {@link LookupRowMatcher} that does not perform much at the moment.
 * TODO To be implemented (e.g. use content in parameters).
 */
public class DefaultLookupRowMatcher implements LookupRowMatcher {

    // Used in dynamic instantiation
    public DefaultLookupRowMatcher(Map<String, String> parameters) {
    }

    @Override
    public DataSetRow getMatchingRow(String joinOn, String joinValue) {
        throw new NotImplementedException();
    }

    @Override
    public RowMetadata getRowMetadata() {
        return new RowMetadata();
    }
}
