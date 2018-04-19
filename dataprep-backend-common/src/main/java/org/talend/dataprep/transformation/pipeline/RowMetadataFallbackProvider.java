// ============================================================================
//
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

package org.talend.dataprep.transformation.pipeline;

import org.talend.dataprep.api.dataset.RowMetadata;

/**
 * This class provide a fallback of the row metadata when we write the result without records (due to a filter). Because
 * the writer Node use the
 * last row metadata of the last record received, and a fallback when no record are received.
 * Statistics Nodes (@link org.talend.dataprep.transformation.pipeline.node.StatisticsNode) supply the fallback for the
 * row metadata after statistics are proceed (on signals END_OF_STREAM and STOP).
 * Writer Node (@link org.talend.dataprep.transformation.pipeline.model.WriterNode) get this fallback when it received
 * the END_OF_STREAM signal.
 */
public class RowMetadataFallbackProvider {

    private RowMetadata fallback;

    public RowMetadataFallbackProvider(RowMetadata fallback) {
        this.fallback = fallback;
    }

    public RowMetadata getFallback() {
        return fallback;
    }

    public void setFallback(RowMetadata fallback) {
        this.fallback = fallback;
    }
}
