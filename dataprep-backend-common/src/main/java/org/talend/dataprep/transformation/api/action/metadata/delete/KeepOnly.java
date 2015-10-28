package org.talend.dataprep.transformation.api.action.metadata.delete;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.FILTERED;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

@Component(KeepOnly.ACTION_BEAN_PREFIX + KeepOnly.KEEP_ONLY_ACTION_NAME)
public class KeepOnly extends ActionMetadata implements ColumnAction {

    public static final String KEEP_ONLY_ACTION_NAME = "keep_only";

    @Override
    public String getName() {
        return KEEP_ONLY_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return FILTERED.getDisplayName();
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    protected boolean implicitFilter() {
        return false;
    }

    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        if (!getFilter(parameters).test(row)) {
            row.setDeleted(true);
        }
    }
}
