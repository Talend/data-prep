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

import static java.util.Collections.singletonList;
import static org.talend.dataprep.transformation.actions.category.ActionScope.COLUMN_METADATA;
import static org.talend.dataprep.transformation.actions.category.ActionScope.HIDDEN_IN_ACTION_LIST;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.additionalColumn;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.createNewColumn;

import java.util.*;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * duplicate a column
 */
@Action(CopyColumnMetadata.COPY_ACTION_NAME)
public class CopyColumnMetadata extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String COPY_ACTION_NAME = "copy"; //$NON-NLS-1$

    /**
     * The split column appendix.
     */
    public static final String COPY_APPENDIX = "_copy"; //$NON-NLS-1$

    @Override
    public String getName() {
        return COPY_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.COLUMN_METADATA.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public List<String> getActionScope() {
        return Arrays.asList(COLUMN_METADATA.getDisplayName(), HIDDEN_IN_ACTION_LIST.getDisplayName());
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        final RowMetadata rowMetadata = context.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(context.getColumnId());

        createNewColumn( //
                context, //
                singletonList( //
                        additionalColumn() //
                                .withName(context.getColumnName() + COPY_APPENDIX) //
                                .withCopyMetadataFromId(column.getId()) //
                ) //
        );
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        row.set(ActionsUtils.getTargetColumnId(context), row.get(columnId));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_COPY_COLUMNS);
    }

}
