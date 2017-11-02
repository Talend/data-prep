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
package org.talend.dataprep.transformation.actions.duplication;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

/**
 *  Keep only one occurrence of duplicated rows.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + Deduplicate.DEDUPLICATION_ACTION_NAME)
public class Deduplicate extends AbstractActionMetadata implements DataSetAction {

    /**
     * The action code name.
     */
    public static final String DEDUPLICATION_ACTION_NAME = "deduplication";

    /** Hashes name. */
    public static final String HASHES_NAME = "hashes";

    @Override
    public String getName() {
        return DEDUPLICATION_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.DEDUPLICATION.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.FORBID_DISTRIBUTED, Behavior.VALUES_DELETE_ROWS);
    }

    @Override
    public void compile(ActionContext actionContext) {
        final Set<String> hashes = new HashSet<>();
        actionContext.get(HASHES_NAME, p -> hashes);
        super.compile(actionContext);
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        String data = evalHashCode(row);

        Set<String> hashes = context.get(HASHES_NAME);
        if (!hashes.contains(data)) {
            hashes.add(data);
        } else {
            row.setDeleted(true);
        }
    }

    protected String evalHashCode(DataSetRow row) {
        StringBuilder columnContents= new StringBuilder();
        for (ColumnMetadata column : row.getRowMetadata().getColumns()) {
            columnContents.append(column.getId()+":");
            columnContents.append(row.get(column.getId())+"-");
        }

        return sha256Hex(columnContents.toString());
    }
}
