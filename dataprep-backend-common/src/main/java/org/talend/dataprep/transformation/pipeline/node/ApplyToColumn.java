package org.talend.dataprep.transformation.pipeline.node;

import java.util.List;

import org.talend.dataprep.api.dataset.ColumnMetadata;

/**
 * An interface to indicate implementation is able to tell on which columns it applies to. Usually used on a
 * {@link org.talend.dataprep.transformation.pipeline.Node}.
 * 
 * @see BasicNode
 */
public interface ApplyToColumn {

    /**
     * @return The list of column names this class applies to.
     * @see ColumnMetadata#getId()
     */
    List<String> getColumnNames();
}
