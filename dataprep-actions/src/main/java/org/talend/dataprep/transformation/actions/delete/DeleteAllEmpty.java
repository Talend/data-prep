// ============================================================================
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.delete;

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.actions.category.ActionScope.EMPTY;

import java.util.*;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Delete all rows when they are empty.
 */
@Action(DeleteAllEmpty.DELETE_ALL_EMPTY_ACTION_NAME)
public class DeleteAllEmpty extends AbstractActionMetadata implements DataSetAction {

    /**
     * The action name.
     */
    public static final String DELETE_ALL_EMPTY_ACTION_NAME = "delete_all_empty";

    protected static final String ACTION_PARAMETER = "action_on_blank_lines";

    protected static final String DELETE = "delete";

    protected static final String KEEP = "keep";

    @Override
    public String getName() {
        return DELETE_ALL_EMPTY_ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);

        parameters.add(SelectParameter.selectParameter(locale)
                .name(ACTION_PARAMETER)
                .item(DELETE, DELETE)
                .item(KEEP, KEEP)
                .defaultValue(DELETE)
                .build(this));

        return parameters;
    }

    @Override
    public String getCategory(Locale locale) {
        return DATA_CLEANSING.getDisplayName(locale);
    }

    @Override
    public List<String> getActionScope() {
        return Collections.singletonList(EMPTY.getDisplayName());
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_DELETE_ROWS);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        if (!row.isDeleted()) {
            String mode = context.getParameters().get(ACTION_PARAMETER);
            row.setDeleted(toDelete(row, mode));
        }
    }

    public boolean toDelete(DataSetRow row, String mode) {
        Predicate<String> nonDeletableCriteria;
        if (KEEP.equals(mode)) {
            nonDeletableCriteria = s -> StringUtils.isNotEmpty(s);
        } else {
            nonDeletableCriteria = s -> StringUtils.isNotBlank(s);
        }
        for (ColumnMetadata column : row.getRowMetadata().getColumns()) {
            String value = row.get(column.getId());
            if (nonDeletableCriteria.test(value)) {
                return false;
            }
        }
        return true;
    }
}
