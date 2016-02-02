//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.line;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.RowAction;

/**
 * This action do two things:
 * <ul>
 * <li>Take the value in each column of this row, and use them as column names</li>
 * <li>Delete this row</li>
 * </ul>
 */
@Component(MakeLineHeader.ACTION_BEAN_PREFIX + MakeLineHeader.ACTION_NAME)
public class MakeLineHeader extends ActionMetadata implements RowAction {

    public static final String ACTION_NAME = "make_line_header";

    private static final Logger LOGGER = LoggerFactory.getLogger(MakeLineHeader.class);

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
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
    public void applyOnLine(DataSetRow row, ActionContext context) {
        if (getFilter(context.getParameters()).test(row)) {
            LOGGER.debug("Make line header for rowId {} with parameters {} ", context.getRowId(), context.getParameters());
            for (ColumnMetadata column : row.getRowMetadata().getColumns()) {
                String newColumnName = context.get(column.getId(), p -> row.get(column.getId()));
                column.setName(newColumnName);
            }
            context.setOutputRowMetadata(row.getRowMetadata().clone());
            row.setDeleted(true);
        } else {
            boolean hasChanged = false;
            for (ColumnMetadata column : row.getRowMetadata().getColumns()) {
                if (context.has(column.getId())) {
                    column.setName(context.get(column.getId()));
                    hasChanged = true;
                } // else don't change (new column names are not yet known.
            }
            if (hasChanged) {
                context.setOutputRowMetadata(row.getRowMetadata().clone());
            }
        }
    }

}
