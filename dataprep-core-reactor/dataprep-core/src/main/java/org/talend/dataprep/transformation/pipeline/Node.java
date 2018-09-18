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

package org.talend.dataprep.transformation.pipeline;

import java.io.Serializable;

import org.slf4j.Logger;

/**
 * A node is a processing unit inside the transformation pipeline.
 */
public interface Node extends Serializable {

    /**
     * @return The {@link Link} to another Node. Never returns <code>null</code>.
     */
    Link getLink();

    /**
     * Changes the {@link Link link} for output processing of this node.
     *
     * @param link The {@link Link} to server as output for current node.
     */
    void setLink(Link link);

    /**
     * Visit the implementation of the {@link Node}.
     *
     * @param visitor A {@link Visitor} to visit the whole pipeline structure.
     */
    void accept(Visitor visitor);

    RuntimeNode exec();

    /**
     * Log pipeline to DEBUG level using provided message
     *
     * @param logger The logger to log into
     * @param message The message to append to the pipeline dump
     */
    default void logStatus(final Logger logger, final String message) {
        if (logger.isDebugEnabled()) {
            final StringBuilder builder = new StringBuilder();
            final PipelineConsoleDump visitor = new PipelineConsoleDump(builder);
            this.accept(visitor);
            logger.debug(message, builder.toString());
        }
    }

    Node copyShallow();
}
