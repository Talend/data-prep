//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
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

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationContext.class);

    /** Map of action context for each action instance within a transformation. */
    private final Map<DataSetRowAction, ActionContext> contexts = new HashMap<>();

    /** The context itself. */
    private Map<String, Object> context;

    private DataSetRow previousRow = new DataSetRow(Collections.emptyMap());

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
     * @return all the action contexts.
     */
    public Collection<ActionContext> getAllActionsContexts() {
        return contexts.values();
    }

    public void freezeActionContexts() {
        contexts.replaceAll((action, actionContext) -> actionContext.asImmutable());
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
        for (ActionContext context : getAllActionsContexts()) {
            context.getContextEntries().stream().filter(contextEntry -> contextEntry instanceof DisposableBean)
                    .forEach(contextEntry -> {
                        try {
                            ((DisposableBean) contextEntry).destroy();
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
     * @param action
     */
    public ActionContext create(DataSetRowAction action) {
        if (contexts.containsKey(action)) {
            return contexts.get(action);
        } else {
            ActionContext actionContext = new ActionContext(this);
            contexts.put(action, actionContext);
            return actionContext;
        }
    }

    /**
     * Returns a transformation context specific to the current action. Use this to create columns (see
     * {@link ActionContext#column(String, Function)} for more details.
     *
     * @param action
     */
    public ActionContext in(DataSetRowAction action) {
        if (contexts.containsKey(action)) {
            return contexts.get(action);
        } else {
            throw new IllegalStateException("No action context found for '" + action + "'.");
        }
    }

    public void setPreviousRow(DataSetRow previousRow) {
        this.previousRow = previousRow;
    }

    public DataSetRow getPreviousRow() {
        return previousRow;
    }
}
