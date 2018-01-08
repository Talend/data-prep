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

package org.talend.dataprep.transformation.api.action.context;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;

/**
 * Transformation context used by ActionMetadata to store/access contextual values while running.
 *
 * The purpose of this class is to have a small memory footprint and not store the whole dataset. To prevent misuse of
 * this class in future / open developments, it's final.
 */
public final class TransformationContext implements Serializable {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationContext.class);

    /** Map of action context for each action instance within a transformation. */
    private final transient Map<DataSetRowAction, ActionContext> contexts;

    /** The context itself. */
    private final transient Map<String, Object> context;

    /**
     * Default empty constructor.
     */
    public TransformationContext() {
        context = new HashMap<>();
        contexts = new HashMap<>();
    }

    /**
     * Put the given value at the given key in the context.
     *
     * @param key where to put the value.
     * @param value the value to store.
     */
    public void put(String key, Object value) {
        context.put(key, value);
        LOGGER.debug("adding {}->{} to the context", key, value);
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
     * @return all the action contexts.
     */
    private Collection<ActionContext> getAllActionsContexts() {
        return contexts.values();
    }

    public void freezeActionContext(ActionContext actionContext) {
        contexts.replaceAll((action, ac) -> {
            if (ac == actionContext) {
                return actionContext.asImmutable();
            } else {
                return ac;
            }
        });
    }

    /**
     * Cleanup transformation context.
     */
    public void cleanup() {
        final Collection<ActionContext> allActionsContexts = getAllActionsContexts();
        LOGGER.debug("cleaning up {} action context(s) ", allActionsContexts.size());
        for (ActionContext currentContext : allActionsContexts) {
            currentContext.getContextEntries().forEach(contextEntry -> {
                try {
                    try {
                        final Method destroy = ClassUtils.getPublicMethod(contextEntry.getClass(), "destroy", new Class[0]);
                        LOGGER.debug("destroy {}", contextEntry);
                        destroy.invoke(contextEntry);
                    } catch (NoSuchMethodException e) {
                        LOGGER.trace("Context entry {} does not need clean up.", contextEntry, e);
                    }
                } catch (Exception error) {
                    LOGGER.warn("error cleaning action context {}", contextEntry, error);
                }
            });
        }
    }

    /**
     * Returns a transformation context specific to the current action. Use this to create columns (see
     * {@link ActionContext#column(String, Function)} for more details.
     *
     * @param action The action to create the context for.
     * @param rowMetadata The {@link RowMetadata row description} when the action context is created.
     */
    public ActionContext create(DataSetRowAction action, RowMetadata rowMetadata) {
        if (contexts.containsKey(action)) {
            return contexts.get(action);
        } else {
            ActionContext actionContext = new ActionContext(this, rowMetadata);
            contexts.put(action, actionContext);
            LOGGER.debug("adding new ActionContext for {}", action);
            return actionContext;
        }
    }

    /**
     * Returns a transformation context specific to the current action. Use this to create columns (see
     * {@link ActionContext#column(String, Function)} for more details.
     *
     * @param action the action to get the context from.
     */
    public ActionContext in(DataSetRowAction action) {
        if (contexts.containsKey(action)) {
            return contexts.get(action);
        } else {
            throw new IllegalStateException("No action context found for '" + action + "'.");
        }
    }

}
