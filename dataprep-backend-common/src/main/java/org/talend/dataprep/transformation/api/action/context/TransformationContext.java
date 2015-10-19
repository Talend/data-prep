package org.talend.dataprep.transformation.api.action.context;

import java.util.HashMap;
import java.util.Map;

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

}
