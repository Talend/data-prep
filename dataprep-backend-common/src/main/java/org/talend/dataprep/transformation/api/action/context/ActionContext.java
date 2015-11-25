package org.talend.dataprep.transformation.api.action.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;

/**
 * Context for an action within a transformation. Hence, several instance of the same action can have their own context.
 */
public class ActionContext {

    /** Link to the transformation context. */
    private final TransformationContext parent;

    /** A map of object (used to reuse objects across row process). */
    private Map<String, Object> context = new HashMap<>();

    /**
     * Default constructor.
     * 
     * @param parent the parent transformation context.
     */
    public ActionContext(TransformationContext parent) {
        this.parent = parent;
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
     * @param create A {@link Function function} that provides a new {@link ColumnMetadata} in case no column with
     * <code>name</code> was previously created. Function is <b>not</b> allowed to return <code>null</code>.
     * @param rowMetadata The row metadata use to create column if missing.
     * @param name A column name as string. All values are accepted, no collision with other action can occur.
     * @return A {@link ColumnMetadata column} id with name <code>name</code>.
     * @throws IllegalArgumentException In case the <code>supplier</code> returned a <code>null</code> instance.
     */
    public String column(String name, RowMetadata rowMetadata, Function<RowMetadata, ColumnMetadata> create) {
        String key = getColumnKey(name);
        if (context.containsKey(key)) {
            return ((ColumnMetadata) context.get(key)).getId();
        } else {
            final ColumnMetadata columnMetadata = create.apply(rowMetadata);
            if (columnMetadata == null) {
                throw new IllegalArgumentException("Cannot use a null column for '" + name + "'");
            }
            context.put(key, columnMetadata);
            return columnMetadata.getId();
        }
    }

    /**
     * Return the context key for the column.
     *
     * @param name the column original name.
     * @return the context key for the column.
     */
    private String getColumnKey(String name) {
        return "col#" + name;
    }

    /**
     * Return the object from the context or use the supplier to create it and cache it.
     *
     * @param key the object key.
     * @param parameters the parameters used by the supplier.
     * @param supplier the supplier to use to create the object in case it is not found in the context.
     * @return the object (stored in the context).
     */
    public Object get(String key, Map<String, String> parameters, Function<Map<String, String>, Object> supplier) {
        if (context.containsKey(key)) {
            return context.get(key);
        }

        final Object value = supplier.apply(parameters);
        context.put(key, value);
        return value;
    }

    /**
     * @return A new {@link ActionContext} instance that can not be modified: an immutable ActionContext will <b>not</b>
     * let action add a new column, but allows to get previously created ones.
     */
    public ActionContext asImmutable() {
        final ActionContext actionContext = new ActionContext(parent);
        actionContext.context = Collections.unmodifiableMap(this.context);
        return actionContext;
    }
}
