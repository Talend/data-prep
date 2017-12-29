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
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.common.ColumnAction;

@Action(FillWithValue.ACTION_NAME)
public class FillWithValue extends AbstractFillWith implements ColumnAction {

    public static final String ACTION_NAME = "fill_with_value";

    public FillWithValue() {
        this(Type.STRING);
    }

    public FillWithValue(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return DATA_CLEANSING.getDisplayName(locale);
    }

    @Override
    public boolean shouldBeProcessed(DataSetRow dataSetRow, String columnId) {
        return true;
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

}
