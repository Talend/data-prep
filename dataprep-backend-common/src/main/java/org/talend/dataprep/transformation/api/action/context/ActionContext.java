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

package org.talend.dataprep.transformation.api.action.context;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;

/**
 * Context for an action within a transformation. Hence, several instance of the same action can have their own context.
 */
public class ActionContext implements Serializable {

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionContext.class);

    public enum ActionStatus {
        /**
         * Indicates action has not yet been executed.
         */
        NOT_EXECUTED,
        /**
         * Indicate action is good for usage and transformation process should continue using this action.
         */
        OK,
        /**
         * Indicates an action no longer needs to be executed.
         */
        DONE,
        /**
         * Indicates action is "canceled": transformation process should discard action from execution and won't execute
         * it.
         */
        CANCELED
    }

    private static final String COLUMN_CONTEXT_PREFIX = "col#";

    /** Link to the transformation context. */
    private final TransformationContext parent;

    /** A map of object (used to reuse objects across row process). */
    private transient Map<String, Object> context = new HashMap<>();

    private RowMetadata rowMetadata;

    private Map<String, String> parameters = Collections.emptyMap();

    private ActionStatus actionStatus = ActionStatus.NOT_EXECUTED;

    private transient Predicate<DataSetRow> filter = r -> true;

    /**
     * Default constructor.
     *
     * @param parent the parent transformation context.
     */
    public ActionContext(TransformationContext parent) {
        this(parent, new RowMetadata());
    }

    public ActionContext(TransformationContext parent, RowMetadata rowMetadata) {
        this.parent = parent;
        this.rowMetadata = rowMetadata;
    }

    public Predicate<DataSetRow> getFilter() {
        return filter == null ? r -> true : filter;
    }

    public void setFilter(Predicate<DataSetRow> filter) {
        this.filter = filter;
    }

    /**
     * @return the parent context.
     */
    public TransformationContext getParent() {
        return parent;
    }

    /**
     * <p>
     * Returns the column with logical name <code>name</code> or creates a new one with provided {@link Supplier
     * supplier}.
     * </p>
     * <p>
     * <b>Note</b>It is up to the caller to insert column in a {@link org.talend.dataprep.api.dataset.RowMetadata row}.
     * </p>
     *
     * @param name A column name as string. All values are accepted, no collision with other action can occur.
     * @param create A {@link Function function} that provides a new {@link ColumnMetadata} in case no column with
     * <code>name</code> was previously created. Function is <b>not</b> allowed to return <code>null</code>.
     * @return A {@link ColumnMetadata column} id with name <code>name</code>.
     * @throws IllegalArgumentException In case the <code>supplier</code> returned a <code>null</code> instance.
     */
    public String column(String name, Function<RowMetadata, ColumnMetadata> create) {
        String key = getColumnKey(name);
        if (getContext().containsKey(key)) {
            return ((ColumnMetadata) getContext().get(key)).getId();
        } else {
            final ColumnMetadata columnMetadata = create.apply(rowMetadata);
            if (columnMetadata == null) {
                throw new IllegalArgumentException("Cannot use a null column for '" + name + "'");
            }
            getContext().put(key, columnMetadata);
            return columnMetadata.getId();
        }
    }

    /**
     * Fetch the column ID based on the column name.
     * <strong>WARNING: the column name is not mandatory unique!</strong>
     *
     * @param name the column name
     * @return the column ID
     */
    public String column(String name) {
        String key = getColumnKey(name);
        if (getContext().containsKey(key)) {
            return ((ColumnMetadata) getContext().get(key)).getId();
        } else {
            throw new IllegalArgumentException("Column '" + name + "' does not exist in action.");
        }
    }

    /**
     * Return the context key for the column.
     *
     * @param name the column original name.
     * @return the context key for the column.
     */
    String getColumnKey(String name) {
        return COLUMN_CONTEXT_PREFIX + name;
    }

    public boolean has(String key) {
        return getContext().containsKey(key);
    }

    /**
     * Return the object from the context or use the supplier to create it and cache it.
     *
     * @param key the object key.
     * @return the object (stored in the context).
     */
    public <T> T get(String key) {
        if (getContext().containsKey(key)) {
            return (T) getContext().get(key);
        }
        throw new IllegalArgumentException("Key '" + key + "' does not exist.");
    }

    public boolean contains(String key) {
        return getContext().containsKey(key);
    }

    /**
     * Return the object from the context or use the supplier to create it and cache it.
     *
     * @param key the object key.
     * @param supplier the supplier to use to create the object in case it is not found in the context.
     * @return the object (stored in the context).
     */
    public <T> T get(String key, Function<Map<String, String>, T> supplier) {
        T value = (T) getContext().get(key);
        if (value == null) {
            value = supplier.apply(parameters);
            getContext().put(key, value);
            LOGGER.debug("adding {}->{} in this context {}", key, value, this);
        }
        return value;
    }

    /**
     * @return the context entries.
     */
    public Collection<Object> getContextEntries() {
        return getContext().values();
    }

    /**
     * @return A new {@link ActionContext} instance that can not be modified: an immutable ActionContext will <b>not</b>
     * let action add a new column, but allows to get previously created ones.
     */
    public ActionContext asImmutable() {
        return new ImmutableActionContext(this);
    }

    public RowMetadata getRowMetadata() {
        return rowMetadata;
    }

    public void setRowMetadata(RowMetadata rowMetadata) {
        this.rowMetadata = rowMetadata;
        // Remove previous columns
        final List<String> toRemove = getContext().keySet().stream() //
                .filter(s -> s.startsWith(COLUMN_CONTEXT_PREFIX)) //
                .collect(Collectors.toList());
        toRemove.forEach(getContext()::remove);
        LOGGER.debug("Removed {} when new row was set.", toRemove);
    }

    private Map<String, Object> getContext() {
        if (context == null) {
            context = new HashMap<>();
        }

        return context;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * The selected column Id, privileged to apply the action.
     *
     * @return the selected column Id or null if no column is selected
     */
    public String getColumnId() {
        return parameters.get(ImplicitParameters.COLUMN_ID.getKey());
    }

    /**
     * The selected column name, privileged to apply the action.
     *
     * @return the selected column name or null if no column is selected
     */
    public String getColumnName() {
        return getRowMetadata().getById(getColumnId()).getName();
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Long getRowId() {
        return Long.parseLong(parameters.get(ImplicitParameters.ROW_ID.getKey()));
    }

    public ScopeCategory getScope() {
        final String scopeParameter = parameters.get(ImplicitParameters.SCOPE.getKey());
        if (scopeParameter != null) {
            return ScopeCategory.valueOf(scopeParameter.toUpperCase());
        }
        return null;
    }

    /**
     * @return The {@link ActionStatus status} of the action.
     */
    public ActionStatus getActionStatus() {
        return actionStatus;
    }

    /**
     * Changes the action status: implementation of actions may want to interrupt computation (no more changes to be
     * done).
     *
     * @param actionStatus The new action status, one of {@link ActionStatus}.
     */
    public void setActionStatus(ActionStatus actionStatus) {
        if (this.actionStatus == ActionStatus.CANCELED && actionStatus == ActionStatus.OK) {
            // Don't allow transition from CANCELLED to OK.
            return;
        }
        if (this.actionStatus != actionStatus && actionStatus == ActionStatus.DONE) {
            // When action is marked as DONE, prevent further modifications to context.
            parent.freezeActionContext(this);
        }
        this.actionStatus = actionStatus;
    }

    private static class ImmutableActionContext extends ActionContext {

        private final ActionContext delegate;

        private ImmutableActionContext(ActionContext delegate) {
            super(delegate.parent, delegate.getRowMetadata());
            this.delegate = delegate;
        }

        @Override
        public void setRowMetadata(RowMetadata rowMetadata) {
            // No op: unable to modify metadata once immutable.
        }

        @Override
        public void setParameters(Map<String, String> parameters) {
            // No op: unable to modify once immutable.
        }

        @Override
        public String getColumnId() {
            return delegate.getColumnId();
        }

        @Override
        public Map<String, String> getParameters() {
            return Collections.unmodifiableMap(delegate.getParameters());
        }

        @Override
        public Long getRowId() {
            return delegate.getRowId();
        }

        @Override
        public ScopeCategory getScope() {
            return delegate.getScope();
        }

        @Override
        public ActionStatus getActionStatus() {
            return delegate.getActionStatus();
        }

        @Override
        public void setActionStatus(ActionStatus actionStatus) {
            delegate.setActionStatus(actionStatus);
        }

        @Override
        public TransformationContext getParent() {
            return delegate.getParent();
        }

        @Override
        public String column(String name, Function<RowMetadata, ColumnMetadata> create) {
            if (!delegate.getContext().containsKey(getColumnKey(name))) {
                throw new UnsupportedOperationException();
            }
            return delegate.column(name, create);
        }

        @Override
        public <T> T get(String key) {
            return delegate.get(key);
        }

        @Override
        public <T> T get(String key, Function<Map<String, String>, T> supplier) {
            return delegate.get(key, supplier);
        }

        @Override
        public Collection<Object> getContextEntries() {
            return Collections.unmodifiableCollection(delegate.getContextEntries());
        }

        @Override
        public ActionContext asImmutable() {
            return this;
        }

        @Override
        public RowMetadata getRowMetadata() {
            return delegate.getRowMetadata();
        }
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "ActionContext{" + "parent=#" + parent + ", context=" + getContext() + ", parameters=" + parameters + ", actionStatus="
                + actionStatus + '}';
    }
}
