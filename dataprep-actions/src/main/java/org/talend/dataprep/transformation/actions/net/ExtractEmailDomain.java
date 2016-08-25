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

package org.talend.dataprep.transformation.actions.net;

import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.api.type.Type.STRING;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Split a cell value on a separator.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + ExtractEmailDomain.EXTRACT_DOMAIN_ACTION_NAME)
public class ExtractEmailDomain extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String EXTRACT_DOMAIN_ACTION_NAME = "extractemaildomain"; //$NON-NLS-1$

    /**
     * The local suffix.
     */
    private static final String LOCAL = "_local"; //$NON-NLS-1$

    /**
     * The domain suffix.
     */
    private static final String DOMAIN = "_domain"; //$NON-NLS-1$

    @Override
    public String getName() {
        return EXTRACT_DOMAIN_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.SPLIT.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType())) && StringUtils.equalsIgnoreCase("email", column.getDomain());
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            final String columnId = context.getColumnId();
            final RowMetadata rowMetadata = context.getRowMetadata();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            // Perform metadata level actions (add local + domain columns).
            final String local = context.column(LOCAL, r -> {
                final ColumnMetadata newColumn = column().name(column.getName() + LOCAL).type(Type.STRING).build();
                rowMetadata.insertAfter(columnId, newColumn);
                return newColumn;
            });
            context.column(DOMAIN, r -> {
                final ColumnMetadata newColumn = column().name(column.getName() + DOMAIN).type(Type.STRING).build();
                rowMetadata.insertAfter(local, newColumn);
                return newColumn;
            });
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String originalValue = row.get(columnId);
        // Perform metadata level actions (add local + domain columns).
        final String local = context.column(LOCAL);
        final String domain = context.column(DOMAIN);
        // Set the values in newly created columns
        if (originalValue == null) {
            return;
        }
        final String[] split = originalValue.split("@", 2);
        final String localPart = split.length >= 2 ? split[0] : StringUtils.EMPTY;
        row.set(local, localPart);
        final String domainPart = split.length >= 2 ? split[1] : StringUtils.EMPTY;
        row.set(domain, domainPart);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
