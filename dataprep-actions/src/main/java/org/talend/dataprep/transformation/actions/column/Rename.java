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

package org.talend.dataprep.transformation.actions.column;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.actions.category.ActionScope.COLUMN_METADATA;
import static org.talend.dataprep.transformation.actions.category.ActionScope.HIDDEN_IN_ACTION_LIST;

import java.util.*;

import javax.annotation.Nonnull;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Rename a column.
 *
 * If the column to rename does not exist or the new name is already used, nothing happen.
 */
@Action(Rename.RENAME_ACTION_NAME)
public class Rename extends AbstractActionMetadata implements ColumnAction {

    /** Action name. */
    public static final String RENAME_ACTION_NAME = "rename_column"; //$NON-NLS-1$

    /** Name of the new column parameter. */
    public static final String NEW_COLUMN_NAME_PARAMETER_NAME = "new_column_name"; //$NON-NLS-1$

    public final String defaultName;

    /**
     * Default empty constructor that with no new column name.
     */
    public Rename() {
        this(EMPTY);
    }

    /**
     * Constructor with a new column name.
     *
     * @param defaultName the default new column name.
     */
    public Rename(final String defaultName) {
        this.defaultName = defaultName;
    }

    @Override
    public String getName() {
        return RENAME_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMN_METADATA.getDisplayName(locale);
    }

    @Override
    public List<String> getActionScope() {
        return Arrays.asList(COLUMN_METADATA.getDisplayName(), HIDDEN_IN_ACTION_LIST.getDisplayName());
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);
        parameters.add(Parameter.parameter(locale).setName(NEW_COLUMN_NAME_PARAMETER_NAME)
                .setType(ParameterType.STRING)
                .setDefaultValue(defaultName)
                .setCanBeBlank(false)
                .build(this));
        return parameters;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    /**
     * @param column A {@link ColumnMetadata column} information.
     * @return A rename action with <code>column</code> name as default name.
     */
    @Override
    public ActionDefinition adapt(ColumnMetadata column) {
        if (column == null) {
            return this;
        }
        return new Rename(column.getName());
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String newColumnName = context.getParameters().get(NEW_COLUMN_NAME_PARAMETER_NAME);
        final RowMetadata rowMetadata = context.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(context.getColumnId());
        column.setName(newColumnName);
        rowMetadata.update(context.getColumnId(), column);
        context.setActionStatus(ActionContext.ActionStatus.DONE);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CHANGE_NAME);
    }
}
