package org.talend.dataprep.api.filter;

import java.util.Map;
import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.DataSetRow;

public interface FilterService {

    /**
     * Builds a {@link Predicate} to serve as filter for {@link DataSetRow rows}.
     * 
     * @param filterAsString A filter as string that follow conditions as defined in
     * <a href="https://in.talend.com/9082609">MDM wiki</a>
     * @return A {@link Predicate} to be used to filter rows in actions. Empty or <code>null</code> returns a
     * "match all" predicate (i.e. no filtering).
     * @see org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata#create(Map)
     */
    Predicate<DataSetRow> build(String filterAsString);
}
