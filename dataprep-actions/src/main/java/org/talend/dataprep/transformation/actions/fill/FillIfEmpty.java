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

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.transformation.actions.common.ColumnAction;

@Action(FillIfEmpty.FILL_EMPTY_ACTION_NAME)
public class FillIfEmpty extends AbstractFillWith implements ColumnAction {

    public static final String FILL_EMPTY_ACTION_NAME = "fillemptywithdefault";

    public static final String FILL_EMPTY_BOOLEAN = "fillemptywithdefaultboolean"; //$NON-NLS-1$

    public static final String FILL_EMPTY_DATE = "fillemptywithdefaultdate"; //$NON-NLS-1$

    public static final String FILL_EMPTY_INTEGER = "fillemptywithdefaultinteger"; //$NON-NLS-1$

    public static final String FILL_EMPTY_STRING = "fillemptywithdefault"; //$NON-NLS-1$

    public FillIfEmpty() {
        this(Type.STRING);
    }

    public FillIfEmpty(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return FILL_EMPTY_ACTION_NAME;
    }

    @Override
    public String getDescription(Locale locale) {
        switch (type) {
        case STRING:
            return ActionsBundle.actionDescription(this, locale, FILL_EMPTY_STRING);
        case NUMERIC:
        case DOUBLE:
        case FLOAT:
        case INTEGER:
            return ActionsBundle.actionDescription(this, locale, FILL_EMPTY_INTEGER);
        case BOOLEAN:
            return ActionsBundle.actionDescription(this, locale, FILL_EMPTY_BOOLEAN);
        case DATE:
            return ActionsBundle.actionDescription(this, locale, FILL_EMPTY_DATE);
        default:
            throw new UnsupportedOperationException("Type '" + type + "' is not supported.");
        }
    }

    @Override
    public String getLabel(Locale locale) {
        switch (type) {
        case STRING:
            return ActionsBundle.actionLabel(this, locale, FILL_EMPTY_STRING);
        case NUMERIC:
        case DOUBLE:
        case FLOAT:
        case INTEGER:
            return ActionsBundle.actionLabel(this, locale, FILL_EMPTY_INTEGER);
        case BOOLEAN:
            return ActionsBundle.actionLabel(this, locale, FILL_EMPTY_BOOLEAN);
        case DATE:
            return ActionsBundle.actionLabel(this, locale, FILL_EMPTY_DATE);
        default:
            throw new UnsupportedOperationException("Type '" + type + "' is not supported.");
        }

    }

    @Override
    public String getCategory(Locale locale) {
        return DATA_CLEANSING.getDisplayName(locale);
    }

    @Override
    public boolean shouldBeProcessed(DataSetRow dataSetRow, String columnId) {
        final String value = dataSetRow.get(columnId);
        return (value == null || value.trim().length() == 0);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.BOOLEAN.equals(Type.get(column.getType())) //
                || Type.DATE.equals(Type.get(column.getType())) //
                || Type.INTEGER.equals(Type.get(column.getType())) //
                || Type.DOUBLE.equals(Type.get(column.getType())) //
                || Type.FLOAT.equals(Type.get(column.getType())) //
                || Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public ActionDefinition adapt(ColumnMetadata column) {
        if (column == null || !acceptField(column)) {
            return this;
        }
        return new FillIfEmpty(Type.valueOf(column.getType().toUpperCase()));
    }
}
