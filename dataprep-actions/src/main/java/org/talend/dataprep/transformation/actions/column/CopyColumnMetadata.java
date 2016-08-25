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

package org.talend.dataprep.transformation.actions.column;

import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.category.ActionScope.COLUMN_METADATA;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * duplicate a column
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + CopyColumnMetadata.COPY_ACTION_NAME)
public class CopyColumnMetadata extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String COPY_ACTION_NAME = "copy"; //$NON-NLS-1$

    /**
     * The split column appendix.
     */
    public static final String COPY_APPENDIX = "_copy"; //$NON-NLS-1$

    private static final String TARGET_COLUMN_ID_KEY = "TARGET_COLUMN_ID_KEY";

    @Override
    public String getName() {
        return COPY_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.COLUMN_METADATA.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public List<String> getActionScope() {
        return Collections.singletonList(COLUMN_METADATA.getDisplayName());
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final String columnId = actionContext.getColumnId();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            String copyColumnId = actionContext.column(column.getName() + COPY_APPENDIX, r -> {
                final ColumnMetadata newColumn = column() //
                        .copy(column) //
                        .computedId(StringUtils.EMPTY) //
                        .name(column.getName() + COPY_APPENDIX) //
                        .build();
                rowMetadata.insertAfter(columnId, newColumn);
                return newColumn;
            });
            actionContext.get(TARGET_COLUMN_ID_KEY, m -> copyColumnId);
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String copyColumn = context.get(TARGET_COLUMN_ID_KEY);
        final String columnId = context.getColumnId();
        row.set(copyColumn, row.get(columnId));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_COPY_COLUMNS);
    }

}
