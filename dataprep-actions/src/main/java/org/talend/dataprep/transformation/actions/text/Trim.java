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

package org.talend.dataprep.transformation.actions.text;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Trim leading and trailing spaces.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + Trim.TRIM_ACTION_NAME)
public class Trim extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String TRIM_ACTION_NAME = "trim"; //$NON-NLS-1$

    private static final String TRIM_PATTERN = "trimPattern";

    @Override
    public String getName() {
        return TRIM_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            final Pattern pattern = Pattern.compile("(^\\h*)|(\\h*$)");
            actionContext.get(TRIM_PATTERN, parameters -> pattern);
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String toTrim = row.get(columnId);
        if (toTrim != null) {
            final Pattern trimPattern = context.get(TRIM_PATTERN);
            row.set(columnId, trimPattern.matcher(toTrim).replaceAll(StringUtils.EMPTY));
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }
}
