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
import org.talend.dataprep.api.type.Type;
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
        return ActionCategory.TABLE.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

    @Override
    public void compile(ActionContext actionContext) {
        final Set<String> hashes = new HashSet<>();
        actionContext.get(HASHES_NAME, p -> hashes);
        super.compile(actionContext);
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        Set<String> hashes = context.get(HASHES_NAME);

        StringBuilder columnContents= new StringBuilder();
        for (ColumnMetadata column : row.getRowMetadata().getColumns()) {
            columnContents.append(context.getColumnId());
            columnContents.append(row.get(column.getId()));
        }

        String data = sha256Hex(columnContents.toString());

        if (!hashes.contains(data)) {
            hashes.add(data);
        } else {
            row.setDeleted(true);
        }
    }
}
