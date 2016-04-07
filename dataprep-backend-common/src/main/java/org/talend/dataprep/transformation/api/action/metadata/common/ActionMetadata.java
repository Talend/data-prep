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

package org.talend.dataprep.transformation.api.action.metadata.common;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionScope;
import org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.util.MessagesBundleContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Model an action to perform on a dataset.
 * <p>
 * An "action" is created for each row, see
 * {@link org.talend.dataprep.transformation.api.action.metadata.common.ActionFactory#create(ActionMetadata, Map)}.
 * <p>
 * The actions are called from the
 */
public abstract class ActionMetadata {

    public static final String ACTION_BEAN_PREFIX = "action#"; //$NON-NLS-1$

    @Autowired
    private MessagesBundle messagesBundle;

    public enum Behavior {
        VALUES_ALL,
        METADATA_CHANGE_TYPE,
        METADATA_CHANGE_NAME,
        METADATA_CREATE_COLUMNS,
        METADATA_COPY_COLUMNS,
        METADATA_DELETE_COLUMNS,
        VALUES_COLUMN,
        NEED_STATISTICS
    }

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
     * <li>no change is required.</li>
     * <li>column type is not {@link #acceptColumn(ColumnMetadata) accepted} for current action.</li>
     * </ul>
     * OR a new action metadata with information extracted from <code>column</code>.
     */
    public ActionMetadata adapt(ColumnMetadata column) {
        return this;
    }

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
    public ActionMetadata adapt(final ScopeCategory scope) {
        return this;
    }

    /**
     * @return A unique name used to identify action.
     */
    public abstract String getName();

    /**
     * @return A 'category' for the action used to group similar actions (eg. 'math', 'repair'...).
     * @see org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory
     */
    public abstract String getCategory();

    /**
     * Return true if the action can be applied to the given column metadata.
     *
     * @param column the column metadata to transform.
     * @return true if the action can be applied to the given column metadata.
     */
    public abstract boolean acceptColumn(final ColumnMetadata column);

    /**
     * @return The label of the action, translated in the user locale.
     * @see MessagesBundle
     */
    public String getLabel() {
        return getMessagesBundle().getString("action." + getName() + ".label");
    }

    /**
     * @return The description of the action, translated in the user locale.
     * @see MessagesBundle
     */
    public String getDescription() {
        return getMessagesBundle().getString("action." + getName() + ".desc");
    }

    /**
     * @return The url of the optionnal help page.
     * @see MessagesBundle
     */
    public String getDocUrl() {
        return getMessagesBundle().getString("action." + getName() + ".url", StringUtils.EMPTY);
    }

    /**
     * Defines the list of scopes this action belong to.
     *
     * Scope scope is a concept that allow us to describe on which scope(s) each action can be applied.
     *
     * @return list of scopes of this action
     * @see ActionScope
     */
    public List<String> getActionScope() {
        return new ArrayList<>();
    }

    /**
     * TODO Only here for JSON serialization purposes.
     *
     * @return True if the action is dynamic (i.e the parameters depends on the context
     * (dataset/preparation/previous_actions)
     */
    public boolean isDynamic() {
        return false;
    }

    /**
     * Return true if the action can be applied to the given scope.
     *
     * @param scope the scope to test
     * @return true if the action can be applied to the given scope.
     */
    public final boolean acceptScope(final ScopeCategory scope) {
        switch (scope) {
        case CELL:
            return this instanceof CellAction;
        case LINE:
            return this instanceof RowAction;
        case COLUMN:
            return this instanceof ColumnAction;
        case DATASET:
            return this instanceof DataSetAction;
        default:
            return false;
        }
    }

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
    public void compile(ActionContext actionContext) {
        final RowMetadata input = actionContext.getRowMetadata();
        final ScopeCategory scope = actionContext.getScope();
        if (scope != null) {
            switch (scope) {
            case CELL:
            case COLUMN:
                // Stop action if: there's actually column information in input AND column is not found
                if (input != null && !input.getColumns().isEmpty() && input.getById(actionContext.getColumnId()) == null) {
                    actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
                    return;
                }
                break;
            case LINE:
            case DATASET:
            default:
                break;
            }
        }
        actionContext.setActionStatus(ActionContext.ActionStatus.OK);
    }

    /**
     * @return <code>true</code> if there should be an implicit filtering before the action gets executed. Actions that
     * don't want to take care of filtering should return <code>true</code> (default). Implementations may override this
     * method and return <code>false</code> if they want to handle themselves filtering.
     */
    protected boolean implicitFilter() {
        return true;
    }

    /**
     * @return The list of parameters required for this Action to be executed.
     **/
    public List<Parameter> getParameters() {
        return ImplicitParameters.getParameters();
    }

    @JsonIgnore
    public Set<Behavior> getBehavior() {
        // Safe strategy: use all behaviors to disable all optimizations. Each implementation of action must explicitly
        // declare its behavior(s).
        return EnumSet.allOf(Behavior.class);
    }

    @JsonIgnore
    protected MessagesBundle getMessagesBundle() {
        if (this.messagesBundle == null){
            this.messagesBundle = MessagesBundleContext.get();
        }
        return this.messagesBundle;
    }
}
