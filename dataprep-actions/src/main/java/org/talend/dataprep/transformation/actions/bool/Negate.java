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

package org.talend.dataprep.transformation.actions.bool;

import static java.util.Collections.singletonList;
import static org.talend.dataprep.api.type.Type.BOOLEAN;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Negate a boolean.
 *
 * @see Negate
 */
@Action(Negate.ACTION_NAME)
public class Negate extends AbstractActionMetadata implements ColumnAction {

    public static final String ACTION_NAME = "negate";

    protected static final String NEW_COLUMN_SUFFIX = "_negate";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT_VALUE = false;

    @Override
    public List<Parameter> getParameters(Locale locale) {
        return ActionsUtils.appendColumnCreationParameter(super.getParameters(locale), locale, CREATE_NEW_COLUMN_DEFAULT_VALUE);
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.BOOLEAN.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return BOOLEAN.equals(Type.get(column.getType()));
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT_VALUE)) {
            ActionsUtils.createNewColumn(context, singletonList(ActionsUtils.additionalColumn()
                    .withName(context.getColumnName() + NEW_COLUMN_SUFFIX)
                    .withType(BOOLEAN)));
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        if (isBoolean(value)) {
            final Boolean boolValue = Boolean.valueOf(value);
            row.set(ActionsUtils.getTargetColumnId(context), WordUtils.capitalizeFully("" + !boolValue));
        }
    }

    private boolean isBoolean(final String value) {
        return value != null && ("true".equalsIgnoreCase(value.trim()) || "false".equalsIgnoreCase(value.trim()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }
}
