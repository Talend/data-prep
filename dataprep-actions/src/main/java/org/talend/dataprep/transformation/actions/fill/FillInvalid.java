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

package org.talend.dataprep.transformation.actions.fill;

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;

import java.util.Locale;
import java.util.Set;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.transformation.actions.common.ColumnAction;

@Action(FillInvalid.FILL_INVALID_ACTION_NAME)
public class FillInvalid extends AbstractFillWith implements ColumnAction {

    public static final String FILL_INVALID_ACTION_NAME = "fillinvalidwithdefault"; //$NON-NLS-1$

    private static final String FILL_INVALID_BOOLEAN = "fillinvalidwithdefaultboolean"; //$NON-NLS-1$

    private static final String FILL_INVALID_DATE = "fillinvalidwithdefaultdate"; //$NON-NLS-1$

    private static final String FILL_INVALID_NUMERIC = "fillinvalidwithdefaultnumeric"; //$NON-NLS-1$

    public FillInvalid() {
        this(Type.STRING);
    }

    private FillInvalid(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return FILL_INVALID_ACTION_NAME;
    }

    @Override
    public String getDescription(Locale locale) {
        if (Type.BOOLEAN.isAssignableFrom(type)) {
            return ActionsBundle.actionDescription(this, locale, FILL_INVALID_BOOLEAN);
        } else if (Type.DATE.isAssignableFrom(type)) {
            return ActionsBundle.actionDescription(this, locale, FILL_INVALID_DATE);
        } else if (Type.NUMERIC.isAssignableFrom(type)) {
            return ActionsBundle.actionDescription(this, locale, FILL_INVALID_NUMERIC);
        } else {
            return ActionsBundle.actionDescription(this, locale, FILL_INVALID_ACTION_NAME);
        }
    }

    @Override
    public String getLabel(Locale locale) {
        if (Type.BOOLEAN.isAssignableFrom(type)) {
            return ActionsBundle.actionLabel(this, locale, FILL_INVALID_BOOLEAN);
        } else if (Type.DATE.isAssignableFrom(type)) {
            return ActionsBundle.actionLabel(this, locale, FILL_INVALID_DATE);
        } else if (Type.NUMERIC.isAssignableFrom(type)) {
            return ActionsBundle.actionLabel(this, locale, FILL_INVALID_NUMERIC);
        } else {
            return ActionsBundle.actionLabel(this, locale, FILL_INVALID_ACTION_NAME);
        }
    }

    @Override
    public String getCategory(Locale locale) {
        return DATA_CLEANSING.getDisplayName(locale);
    }

    @Override
    public boolean shouldBeProcessed(DataSetRow dataSetRow, String columnId) {
        return dataSetRow.isInvalid(columnId);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.BOOLEAN.isAssignableFrom(Type.get(column.getType())) //
                || Type.DATE.isAssignableFrom(Type.get(column.getType())) //
                || Type.NUMERIC.isAssignableFrom(Type.get(column.getType())) //
                || Type.STRING.isAssignableFrom(Type.get(column.getType()));
    }

    @Override
    public ActionDefinition adapt(ColumnMetadata column) {
        if (column == null || !acceptField(column)) {
            return this;
        }
        return new FillInvalid(Type.valueOf(column.getType().toUpperCase()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        Set<Behavior> behaviors = super.getBehavior();
        behaviors.add(Behavior.NEED_STATISTICS_INVALID);
        return behaviors;
    }
}
