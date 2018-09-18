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

package org.talend.dataprep.transformation.pipeline.node;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Node;

/**
 * Node that limit input.
 * When the input limit received is reached, a callback can be triggered.
 */
public class LimitNode extends BasicNode {

    /** This class' logger. */
    private static final Logger LOGGER = getLogger(LimitNode.class);

    private int count = 0;

    private final long limit;

    private final Runnable callback;

    public LimitNode(final long limit) {
        this(limit, null);
        LOGGER.trace("Limit set to {}", limit);
    }

    public LimitNode(final long limit, final Runnable callback) {
        this.limit = limit;
        this.callback = callback;
    }

    @Override
    public void receive(final DataSetRow row, final RowMetadata metadata) {
        if (count >= limit) {
            return;
        }

        super.receive(row, metadata);
        increment();
    }

    @Override
    public void receive(final DataSetRow[] rows, final RowMetadata[] metadatas) {
        if (count >= limit) {
            return;
        }

        super.receive(rows, metadatas);
        increment();
    }

    private void increment() {
        if (++count == limit && callback != null) {
            LOGGER.debug("limit {} reached", limit);
            callback.run();
        }
    }

    @Override
    public Node copyShallow() {
        return new LimitNode(limit, callback);
    }
}
