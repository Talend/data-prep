// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
// ============================================================================

package org.talend.dataprep.transformation.actions.clear;

import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.math.BigDecimal;
import java.util.Locale;

import static org.talend.dataprep.api.type.Type.NUMERIC;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.NUMBERS;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + RemoveNegativeValues.ACTION_NAME)
public class RemoveNegativeValues extends AbstractClear implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "remove_negative_values";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return NUMBERS.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return NUMERIC.isAssignableFrom(Type.get(column.getType()));
    }

    @Override
    protected boolean toClear(DataSetRow dataSetRow, String columnId, ActionContext actionContext) {
        final String value = dataSetRow.get(columnId);
        BigDecimal bd = BigDecimalParser.toBigDecimal(value.trim());
        return bd.compareTo(BigDecimal.ZERO) < 0;
    }

}
