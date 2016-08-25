// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.action;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Model a Data Prep action.
 */
public interface ActionDefinition extends Serializable {

    /**
     * @return A unique name used to identify action.
     */
    String getName();

    /**
     * @return A 'category' for the action used to group similar actions (eg. 'math', 'repair'...).
     */
    String getCategory();

    /**
     * @return The label of the action, translated in the user locale.
     */
    String getLabel();

    /**
     * @return The description of the action, translated in the user locale.
     */
    String getDescription();

    /**
     * @return The list of parameters required for this Action to be executed.
     **/
    List<Parameter> getParameters();

    /**
     * @return A set of {@link Behavior} that describes the expected behavior of the action. It helps Data Prep runtime to
     * optimize action executions.
     */
    Set<Behavior> getBehavior();

    /**
     * Builds up a {@link Function} that takes an input {@link GenericRecord record} and returns the <i>eventually modified</i>
     * record. The action can take as input the <code>parameters</code>.
     *
     * @param parameters The action parameters for construction, should be the expected ones from {@link #getParameters()}.
     * @return A {@link Function} that performs operations on a record.
     */
    Function<GenericRecord, GenericRecord> action(List<Parameter> parameters);

    /**
     * Return true if the action can be applied to the given scope.
     *
     * @param scope the scope to test
     * @return true if the action can be applied to the given scope.
     */
    boolean acceptScope(ScopeCategory scope);

    /**
     * <p>
     * Adapts the current action metadata to the field. This method may return <code>this</code> if no action specific
     * change should be done. It may return a different instance with information from field (like a default value
     * inferred from field's name).
     * </p>
     * <p>
     * Implementations are also expected to return <code>this</code> if {@link #acceptField(ColumnMetadata)} returns
     * <code>false</code>.
     * </p>
     *
     * @param column A {@link Schema.Field field} information.
     * @return <code>this</code> if any of the following is true:
     * <ul>
     * <li>no change is required.</li>
     * <li>column type is not {@link #acceptField(ColumnMetadata) accepted} for current action.</li>
     * </ul>
     * OR a new action metadata with information extracted from <code>column</code>.
     */
    ActionDefinition adapt(ColumnMetadata column);

    /**
     * <p>
     * Adapts the current action metadata to the scope. This method may return <code>this</code> if no action specific
     * change should be done. It may return a different instance with information from scope (like a different label).
     * </p>
     *
     * @param scope A {@link ScopeCategory scope}.
     * @return <code>this</code> if no change is required. OR a new action metadata with information extracted from
     * <code>scope</code>.
     */
    ActionDefinition adapt(ScopeCategory scope);

    /**
     * Return true if the action can be applied to the given field.
     *
     * @param column the column to transform.
     * @return true if the action can be applied to the given field.
     */
    boolean acceptField(ColumnMetadata column);

    /**
     * Called by transformation process <b>before</b> the first transformation occurs. This method allows action
     * implementation to compute reusable objects in actual transformation execution. Implementations may also indicate
     * that action is not applicable and should be discarded (
     * {@link org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus#CANCELED}.
     *
     * @param actionContext The action context that contains the parameters and allows compile step to change action
     * status.
     * @see ActionContext#setActionStatus(ActionContext.ActionStatus)
     */
    void compile(ActionContext actionContext);

    /**
     * @return <code>true</code> if there should be an implicit filtering before the action gets executed. Actions that
     * don't want to take care of filtering should return <code>true</code> (default). Implementations may override this
     * method and return <code>false</code> if they want to handle themselves filtering.
     */
    boolean implicitFilter();

    /**
     * Behaviors indicates to runner how action is expected to behave: this gives insight and allows optimizations when executing
     * actions.
     */
    enum Behavior {
        /**
         * Action changes all values in row (e.g. deleting a lines).
         */
        VALUES_ALL,
        /**
         * Action change only the type of the column (not its data) like changing type.
         */
        METADATA_CHANGE_TYPE,
        /**
         * Action change only the name of the column (not its data) like column renaming.
         */
        METADATA_CHANGE_NAME,
        /**
         * Action creates new columns (like splitting).
         */
        METADATA_CREATE_COLUMNS,
        /**
         * Action creates new columns & value but based on an original column.
         */
        METADATA_COPY_COLUMNS,
        /**
         * Action deletes column.
         */
        METADATA_DELETE_COLUMNS,
        /**
         * Action modifies values in this working column.
         */
        VALUES_COLUMN,
        /**
         * Action modifies values in this working column <b>and</b> in all columns used in the action's parameters.
         */
        VALUES_MULTIPLE_COLUMNS,
        /**
         * Action requires up-to-date statistics (for pattern) before it can be executed.
         */
        NEED_STATISTICS_PATTERN,
        /**
         * Action requires up-to-date statistics (for invalid) before it can be executed.
         */
        NEED_STATISTICS_INVALID
    }

}
