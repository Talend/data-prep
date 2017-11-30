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

package org.talend.dataprep.transformation.actions.delete;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.util.*;

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;
import static org.talend.dataprep.transformation.actions.category.ActionScope.EMPTY;

/**
 * Delete all rows when they are empty.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + DeleteAllEmpty.DELETE_ALL_EMPTY_ACTION_NAME)
public class DeleteAllEmpty extends AbstractActionMetadata implements DataSetAction {

    /**
     * The action name.
     */
    public static final String DELETE_ALL_EMPTY_ACTION_NAME = "delete_all_empty";

    protected static final String NON_PRINTING_PARAMETER = "non_printing";

    protected static final String DELETE = "delete";

    protected static final String KEEP = "keep";

    @Override
    public String getName() {
        return DELETE_ALL_EMPTY_ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        List<Parameter> parameters = super.getParameters(locale);

        parameters.add(SelectParameter.selectParameter(locale) //
                .name(NON_PRINTING_PARAMETER) //
                .item(DELETE, DELETE)//
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
        return EnumSet.of(Behavior.FORBID_DISTRIBUTED, Behavior.VALUES_DELETE_ROWS);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        if (!row.isDeleted()) {
            Map<String, String> parameters = context.getParameters();
            String mode = parameters.get(NON_PRINTING_PARAMETER);
            for (ColumnMetadata column : row.getRowMetadata().getColumns()) {
                String value = row.get(column.getId());
                switch (mode) {
                    case DELETE:
                        if (StringUtils.isNotBlank(value)) {
                            return;
                        }
                    case KEEP:
                        if (StringUtils.isNotEmpty(value)) {
                            return;
                        }
                }
            }
            row.setDeleted(true);
        }
    }
}
