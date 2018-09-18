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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;

/**
 * A node implementation that cleans up transformation context when end of stream is reached.
 */
public class CleanUpNode extends BasicNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanUpNode.class);

    private final TransformationContext context;

    public CleanUpNode(TransformationContext context) {
        this.context = context;
    }

    @Override
    public void signal(Signal signal) {
        if (signal == Signal.END_OF_STREAM || signal == Signal.CANCEL || signal == Signal.STOP) {
            try {
                context.cleanup();
            } catch (Exception e) {
                LOGGER.error("Unable to clean context at {}.", signal, e);
            }
        }
        super.signal(signal);
    }

    @Override
    public Node copyShallow() {
        return new CleanUpNode(context);
    }
}
