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
import org.talend.dataprep.api.dataset.DataSetMetadata;

/**
 * An event to indicate a data set metadata has been updated (and update has completed).
 */
public class DataSetMetadataBeforeUpdateEvent extends ApplicationEvent {

    public DataSetMetadataBeforeUpdateEvent(DataSetMetadata source) {
        super(source);
    }

    @Override
    public DataSetMetadata getSource() {
        return (DataSetMetadata) super.getSource();
    }
}
