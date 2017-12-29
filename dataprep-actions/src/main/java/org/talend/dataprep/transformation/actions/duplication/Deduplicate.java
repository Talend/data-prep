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

import static java.util.Collections.singletonList;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Keep only one occurrence of duplicated rows.
 */
@Action(Deduplicate.DEDUPLICATION_ACTION_NAME)
public class Deduplicate extends AbstractActionMetadata implements DataSetAction {

    /**
     * The action code name.
     */
    public static final String DEDUPLICATION_ACTION_NAME = "deduplication";

    /** Key to store in context hashes */
    private static final String HASHES_NAME = "hashes";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return DEDUPLICATION_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.DEDUPLICATION.getDisplayName(locale);
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
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(actionContext, singletonList(ActionsUtils.additionalColumn()));
        }
        final Set<String> hashes = new HashSet<>();
        actionContext.get(HASHES_NAME, p -> hashes);
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        if (!row.isDeleted()) {
            String data = evalHashCode(row);

            Set<String> hashes = context.get(HASHES_NAME);
            if (!hashes.contains(data)) {
                hashes.add(data);
            } else {
                row.setDeleted(true);
            }
        }
    }

    protected String evalHashCode(DataSetRow row) {
        StringBuilder columnContents = new StringBuilder();
        for (ColumnMetadata column : row.getRowMetadata().getColumns()) {
            columnContents
                    .append(column.getId()) //
                    .append(":") //
                    .append(row.get(column.getId())) //
                    .append("-");
        }

        return sha256Hex(columnContents.toString());
    }
}
