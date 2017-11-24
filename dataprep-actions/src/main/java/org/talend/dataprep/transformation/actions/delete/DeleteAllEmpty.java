package org.talend.dataprep.transformation.actions.delete;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
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

    @Override
    public String getName() {
        return DELETE_ALL_EMPTY_ACTION_NAME;
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
        if(!row.isDeleted()) {
            if (checkEmptyRow(row)) {
                row.setDeleted(true);
            }
        }
    }

    /**
     * Check if the row is empty or not. Return true if the row is empty, else false.
     *
     * @param row to test.
     * @return boolean
     */
    protected boolean checkEmptyRow(DataSetRow row) {
        for (ColumnMetadata column : row.getRowMetadata().getColumns()) {
            String value = row.get(column.getId());
            if (!StringUtils.isEmpty(value)) {
                return false;
            }
        }
        return true;
    }
}
