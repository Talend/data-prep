package org.talend.dataprep.transformation.api.action.metadata.common;

import java.util.List;
import java.util.Map;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.DataSetMetadataAction;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

/**
 * Model an action to perform on a dataset.
 * <p>
 * An "action" is created for each row, see {@link ActionMetadata#create(Map)}.
 * <p>
 * The actions are called from the
 */
public interface ActionMetadata {

    String ACTION_BEAN_PREFIX = "action#"; //$NON-NLS-1$

    /**
     * Creates an {@link Action action} based on provided parameters.
     *
     * @param parameters Action-dependent parameters, can be empty.
     * @return An {@link Action action} that can implement {@link DataSetRowAction row action} and/or
     * {@link DataSetMetadataAction metadata action}.
     */
    Action create(Map<String, String> parameters);

    /**
     * <p>
     * Adapts the current action metadata to the column. This method may return <code>this</code> if no action specific
     * change should be done. It may return a different instance with information from column (like a default value
     * inferred from column's name).
     * </p>
     * <p>
     * Implementations are also expected to return <code>this</code> if {@link #acceptColumn(ColumnMetadata)} returns
     * <code>false</code>.
     * </p>
     *
     * @param column A {@link ColumnMetadata column} information.
     * @return <code>this</code> if any of the following is true:
     * <ul>
     *     <li>no change is required.</li>
     *     <li>column type is not {@link #acceptColumn(ColumnMetadata) accepted} for current action.</li>
     * </ul>
     * OR a new action metadata with information extracted from
     * <code>column</code>.
     */
    default ActionMetadata adapt(ColumnMetadata column) {
        return this;
    }

    // ------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------INFOS--------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------

    /**
     * @return A unique name used to identify action.
     */
    String getName();

    /**
     * @return The label of the parameter, translated in the user locale.
     * @see MessagesBundle
     */
    default String getLabel() {
        return MessagesBundle.getString("action." + getName() + ".label");
    }

    /**
     * @return The description of the parameter, translated in the user locale.
     * @see MessagesBundle
     */
    default String getDescription() {
        return MessagesBundle.getString("action." + getName() + ".desc");
    }

    /**
     * @return A 'category' for the action used to group similar actions (eg. 'math', 'repair'...).
     */
    String getCategory();

    // ------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------PARAMS-------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------

    /**
     * @return The list of parameters required for this Action to be executed.
     **/
    List<Parameter> getParameters();

    // ------------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------------CHECKERS------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------

    /**
     * @return True if the action is dynamic (i.e the parameters depends on the context
     * (dataset/preparation/previous_actions)
     */
    default boolean isDynamic() {
        return false;
    }

    /**
     * Return true if the action can be applied to the given column metadata.
     *
     * @param column the column metadata to transform.
     * @return true if the action can be applied to the given column metadata.
     */
    boolean acceptColumn(final ColumnMetadata column);

    /**
     * Return true if the action can be applied to the given scope.
     *
     * @param scope the scope to test
     * @return true if the action can be applied to the given scope.
     */
    boolean acceptScope(final ScopeCategory scope);

}
