// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.actions.math;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractCompareAction;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.CompareAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;

@Action(CompareNumbers.ACTION_NAME)
public class CompareNumbers extends AbstractCompareAction implements ColumnAction, OtherColumnParameters, CompareAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "compare_numbers"; //$NON-NLS-1$

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.NUMBERS.getDisplayName(locale);
    }

    @Override
    protected int doCompare(ComparisonRequest comparisonRequest) {
        final BigDecimal value = BigDecimalParser.toBigDecimal(comparisonRequest.value1);
        final BigDecimal toCompare = BigDecimalParser.toBigDecimal(comparisonRequest.value2);

        return value.compareTo(toCompare);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
}
