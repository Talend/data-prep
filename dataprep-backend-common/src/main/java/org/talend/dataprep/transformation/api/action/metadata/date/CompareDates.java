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

package org.talend.dataprep.transformation.api.action.metadata.date;

import java.time.LocalDateTime;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractCompareAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.CompareAction;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;

@Component(CompareDates.ACTION_BEAN_PREFIX + CompareDates.ACTION_NAME)
public class CompareDates extends AbstractCompareAction implements ColumnAction, OtherColumnParameters, CompareAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "compare_dates"; //$NON-NLS-1$

    @Autowired
    protected DateParser dateParser;

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.DATE.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.DATE.isAssignableFrom(columnType);
    }

    protected Parameter getDefaultConstantValue() {
        // olamy FIXME should be now but depends on date time pattern from which column??
        return new Parameter(CONSTANT_VALUE, ParameterType.STRING, StringUtils.EMPTY);
    }

    @Override
    protected int doCompare(ComparisonRequest comparisonRequest) {

        if (StringUtils.isEmpty(comparisonRequest.value1) //
                && StringUtils.isEmpty(comparisonRequest.value2)) {
            return 0;
        }

        if (StringUtils.isNotEmpty(comparisonRequest.value1) //
                && StringUtils.isEmpty(comparisonRequest.value2)) {
            return 1;
        }

        if (StringUtils.isNotEmpty(comparisonRequest.value2) //
                && StringUtils.isEmpty(comparisonRequest.value1)) {
            return -1;
        }

        final LocalDateTime temporalAccessor1 = dateParser.parse(comparisonRequest.value1, comparisonRequest.colMetadata1);

        // we compare with the format of the first column when the comparison is with a CONSTANT
        final LocalDateTime temporalAccessor2 = dateParser.parse(comparisonRequest.value2,
                comparisonRequest.mode.equals(CONSTANT_MODE) ? //
                        comparisonRequest.colMetadata2 : comparisonRequest.colMetadata1);

        return temporalAccessor1.compareTo(temporalAccessor2);
    }

}
