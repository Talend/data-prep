// ============================================================================
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

package org.talend.dataprep.transformation.actions.common;

import static org.talend.dataprep.parameters.ParameterType.BOOLEAN;

import java.util.*;
import java.util.function.Function;

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.i18n.DocumentationLinkGenerator;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.category.ActionScope;
import org.talend.dataprep.transformation.actions.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Adapter for {@link ActionDefinition} to have default implementation and behavior for actions. Every dataprep actions
 * derive from it but it is not an obligation.
 */
public abstract class AbstractActionMetadata implements InternalActionDefinition {

    public static final String ACTION_BEAN_PREFIX = "action#"; //$NON-NLS-1$

    /**
     * Key for the "Create new column" parameter.
     */
    public static final String CREATE_NEW_COLUMN = "create_new_column";

    /**
     * Key for the context map to retrieve column created by "Create new comumn" parameter.
     */
    private static final String TARGET_COLUMN = "target";

    @Override
    public ActionDefinition adapt(ColumnMetadata column) {
        return this;
    }

    /**
     * <p>
     * Adapts the current action metadata to the scope. This method may return <code>this</code> if no action specific change
     * should be done. It may return a different instance with information from scope (like a different label).
     * </p>
     *
     * @param scope A {@link ScopeCategory scope}.
     * @return <code>this</code> if no change is required. OR a new action metadata with information extracted from
     * <code>scope</code>.
     */
    @Override
    public ActionDefinition adapt(final ScopeCategory scope) {
        return this;
    }

    /**
     * @return A unique name used to identify action.
     */
    @Override
    public abstract String getName();

    /**
     * @return A 'category' for the action used to group similar actions (eg. 'math', 'repair'...).
     * @see ActionCategory
     */
    @Override
    public abstract String getCategory(Locale locale);

    /**
     * Return true if the action can be applied to the given column metadata.
     *
     * @param column the column metadata to transform.
     * @return true if the action can be applied to the given column metadata.
     */
    @Override
    public abstract boolean acceptField(final ColumnMetadata column);

    /**
     * @return The label of the action, translated in the user locale.
     * @see MessagesBundle
     * @param locale
     */
    @Override
    public String getLabel(Locale locale) {
        return ActionsBundle.actionLabel(this, locale, getName());
    }

    /**
     * @return The description of the action, translated in the user locale.
     * @see MessagesBundle
     * @param locale
     */
    @Override
    public String getDescription(Locale locale) {
        return ActionsBundle.actionDescription(this, locale, getName());
    }

    @Override
    public String getDocUrl(Locale locale) {
        String actionDocUrl = ActionsBundle.actionDocUrl(this, locale, getName());
        return DocumentationLinkGenerator
                .builder() //
                .url(actionDocUrl) //
                .locale(locale) //
                .addAfsLanguageParameter(true)
                .build();
    }

    /**
     * Defines the list of scopes this action belong to.
     * <p>
     * Scope scope is a concept that allow us to describe on which scope(s) each action can be applied.
     *
     * @return list of scopes of this action
     * @see ActionScope
     */
    @Override
    public List<String> getActionScope() {
        return new ArrayList<>();
    }

    /**
     * TODO Only here for JSON serialization purposes.
     *
     * @return True if the action is dynamic (i.e the parameters depends on the context
     * (dataset/preparation/previous_actions)
     */
    @Override
    public boolean isDynamic() {
        return false;
    }

    /**
     * Return true if the action can be applied to the given scope.
     *
     * @param scope the scope to test
     * @return true if the action can be applied to the given scope.
     */
    @Override
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
     * that action is not applicable and should be discarded ( {@link ActionContext.ActionStatus#CANCELED}.
     *
     * @param actionContext The action context that contains the parameters and allows compile step to change action
     * status.
     * @see ActionContext#setActionStatus(ActionContext.ActionStatus)
     */
    @Override
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
                createNewColumn(actionContext);
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
    @Override
    public boolean implicitFilter() {
        return true;
    }

    /**
     * @return The list of parameters required for this Action to be executed.
     *
     * @param locale
     */
    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = ImplicitParameters.getParameters(locale);

        // For TDP-TDP-3798, add a checkbox for most actions to allow the user to choose if action is applied in place or if it
        // creates a new column:
        if (createNewColumnParamVisible()) {
            parameters.add(Parameter.parameter(locale).setName(CREATE_NEW_COLUMN)
                    .setType(BOOLEAN)
                    .setDefaultValue("" + getCreateNewColumnDefaultValue())
                    .setCanBeBlank(false)
                    .setImplicit(false)
                    .build(this));
        }

        return parameters;
    }

    @JsonIgnore
    @Override
    public abstract Set<ActionDefinition.Behavior> getBehavior();

    @Override
    public Function<GenericRecord, GenericRecord> action(List<Parameter> parameters) {
        return r -> r;
    }

    /**
     * For TDP-TDP-3798, add a checkbox for most actions to allow the user to choose if action is applied in place or if it
     * creates a new column.
     * This method will be use by framework to define if the parameter is visible for this action or not.
     * For most actions, checkbox is visible, but other actions (like 'mask data' that is always 'in place' or 'split' that
     * always creates new columns) the checkbox will not be visible. In this case, these actions should override this method.
     *
     * @return 'true' if the 'create new column' checkbox is visible, 'false' otherwise
     */
    protected boolean createNewColumnParamVisible() {
        return true;
    }

    /**
     * For TDP-TDP-3798, add a checkbox for most actions to allow the user to choose if action is applied in place or if it
     * creates a new column.
     * This method will be use by framework to define:
     * - the default value of the checkbox, if it's visible
     * - the value of the parameter if the checkbox is not visible
     *
     * For most actions, default will be 'false', but for some actions (like 'compare numbers') it will be 'true'. In this case,
     * these actions should override this method.
     *
     * @return 'true' if the 'create new column' is checked by default, 'false' otherwise
     */
    public boolean getCreateNewColumnDefaultValue() {
        return false;
    }

    /**
     * For TDP-TDP-3798, add a checkbox for most actions to allow the user to choose if action is applied in place or if it
     * creates a new column.
     * This method is used by framework to evaluate if this step (action+parameters) creates a new column or is applied in place.
     *
     * For most actions, the default implementation is ok, but some actions (like 'split' that always creates new column) may
     * override it. In this case, no need to override createNewColumnParamVisible() and getCreateNewColumnDefaultValue().
     *
     * @param parameters
     * @return 'true' if this step (action+parameters) creates a new column, 'false' if it's applied in-place.
     */
    public boolean doesCreateNewColumn(Map<String, String> parameters) {
        if (parameters.containsKey(AbstractActionMetadata.CREATE_NEW_COLUMN)) {
            return Boolean.parseBoolean(parameters.get(CREATE_NEW_COLUMN));
        }
        return getCreateNewColumnDefaultValue();
    }

    /**
     * Used by compile(ActionContext actionContext), evaluate if a new column needs to be created, if yes creates one.
     *
     * Actions that creates more than one column ('split', 'extract email parts', etc...) should manage this on their own.
     */
    final private void createNewColumn(ActionContext context) {
        if (doesCreateNewColumn(context.getParameters())) {
            String columnId = context.getColumnId();
            RowMetadata rowMetadata = context.getRowMetadata();

            context.get(TARGET_COLUMN, r -> {
                final Map<String, String> cols = new HashMap<String, String>();

                String nextId = columnId; // id of the column to put the new one after, initially the current column

                    for (AdditionalColumn additionalColumn : getAdditionalColumns(context)) {
                        ColumnMetadata.Builder c = ColumnMetadata.Builder.column();

                        if (additionalColumn.getCopyFrom() != null) {
                            c.copy(additionalColumn.getCopyFrom())//
                             .computedId(StringUtils.EMPTY);
                        }
                        c.name(additionalColumn.getName()) //
                         .type(additionalColumn.getType()); //

                    ColumnMetadata columnMetadata = c.build();
                    rowMetadata.insertAfter(nextId, columnMetadata);
                    nextId = columnMetadata.getId(); // the new column to put next one after, is the fresh new one
                    cols.put(additionalColumn.getKey(), columnMetadata.getId());
                }

                return cols;
            });
        }
    }

    /**
     * Helper to retrieve the target column Id stored in the context.
     *
     * It can be the current column id if the function applies in place or id of the new column if the function creates one.
     * Must not be used for function that creates many columns. Use getTargetColumnIds(ActionContext context) instead in this case.
     *
     * @param context the action context
     * @return the target column ID
     */
    public String getTargetColumnId(ActionContext context) {
        if (doesCreateNewColumn(context.getParameters())) {
            final Map<String, String> newColumns = context.get(TARGET_COLUMN);
            return newColumns.values().iterator().next();
        } else {
            return context.getColumnId();
        }
    }

    /**
     * Returns new columns created by the function in case of it creates multiple ones. Like 'Split' or 'ExtractDateTokens'.
     *
     * @return a map in which keys are the 'key' from AdditionnalColumn bean, and values columns ids.
     */
    public Map<String, String> getTargetColumnIds(ActionContext context) {
        return context.get(TARGET_COLUMN);
    }

    protected List<AdditionalColumn> getAdditionalColumns(ActionContext context) {
        final List<AdditionalColumn> additionalColumns = new ArrayList<>();

        additionalColumns.add(new AdditionalColumn(getColumnType(context), getCreatedColumnName(context)));

        return additionalColumns;
    }

    /**
     * Used by createNewColumn(ActionContext context) to know which column Type to use when creating a new column.
     *
     * Default implementation is STRING, actions that creates a column of a different type should override this method.
     *
     * @return The Type of the new column
     */
    protected Type getColumnType(ActionContext context) {
        return Type.STRING;
    }

    /**
     * Used by createNewColumn(ActionContext context) to know which name to use when creating a new column.
     *
     * @return The name of the new column
     */
    protected String getCreatedColumnName(ActionContext context) {
        return null; // Must be implemented for all actions but those which always applies in place
    }

    /**
     * Bean used to described all columns that can be created by a function.
     *
     * This will be used by createNewColumn(ActionContext context) to create all the new columns.
     */
    protected class AdditionalColumn {

        private String key;

        private String name;

        private Type type = Type.STRING;

        private ColumnMetadata copyFrom;

        public AdditionalColumn(String name) {
            this(name, name);
        }

        public AdditionalColumn(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public AdditionalColumn(String key, Type type, String name) {
            this(key, name);
            this.type = type;
        }

        public AdditionalColumn(Type type, String name) {
            this(name);
            this.type = type;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public ColumnMetadata getCopyFrom() {
            return copyFrom;
        }

        public void setCopyFrom(ColumnMetadata from) {
            this.copyFrom = from;
        }
    }

}
