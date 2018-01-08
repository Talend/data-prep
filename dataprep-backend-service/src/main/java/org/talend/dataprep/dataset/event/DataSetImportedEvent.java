// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.dataset.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event sent when a DataSet was just imported (good starting point to start asynchronous analysis).
 */
public class DataSetImportedEvent extends ApplicationEvent {

    /** For the Serialization interface. */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     *
     * @param datasetId the imported dataset id.
     */
    public DataSetImportedEvent(String datasetId) {
        super(datasetId);
    }

    /**
     * @return the DatasetId
     */
    public String getSource() {
        return (String) source;
    }

}
