package org.talend.dataprep.transformation.api.action.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Transformation context used by ActionMetadata to store/access contextual values while running.
 *
 * The purpose of this class is to have a small memory footprint and not store the whole dataset. To prevent misuse of
 * this class in future / open developments, it's final.
 *
 * @see ActionMetadata#create(Map)
 */
public final class TransformationContext {

    /** Map of action context for each action instance within a transformation. */
    private final Map<ActionMetadata, ActionContext> contexts = new HashMap<>();

    /** The context itself. */
    private Map<String, Object> context;

    /**
     * Default empty constructor.
     */
    public TransformationContext() {
        context = new HashMap<>();
    }

    /**
     * Put the given value at the given key in the context.
     *
     * @param key where to put the value.
     * @param value the value to store.
     */
    public void put(String key, Object value) {
        context.put(key, value);
    }

    /**
     * Return the wanted value.
     *
     * @param key where to look for the value in the context.
     * @return the wanted value or null if not found.
     */
    public Object get(String key) {
        return context.get(key);
    }

    /**
     * Returns a transformation context specific to the current action. Use this to create columns (see
     * {@link ActionContext#column(String, RowMetadata, Function)} for more details.
     *
     * @param actionMetadata An instance of action used as key for finding context
     * @return An {@link ActionContext context} ready to be used.
     */
    public ActionContext in(ActionMetadata actionMetadata) {
        if (contexts.containsKey(actionMetadata)) {
            return contexts.get(actionMetadata);
        } else {
            ActionContext actionContext = new ActionContext(this);
            contexts.put(actionMetadata, actionContext);
            return actionContext;
        }
    }

    public void freezeActionContexts() {
        contexts.replaceAll((actionMetadata, actionContext) -> actionContext.asImmutable());
    }
}
