package org.talend.dataprep.transformation.api.transformer;

import org.talend.dataprep.transformation.pipeline.Signal;

/**
 * Interface only used to be able to interact with a Transformer without exposing the underneath pipeline.
 */
public interface ExecutableTransformer {

    /**
     * Execute the transformer.
     */
    void execute();

    /**
     * Send the given signal to the running transformer.
     *
     * @param signal the signal to send.
     */
    void signal(Signal signal);

}
