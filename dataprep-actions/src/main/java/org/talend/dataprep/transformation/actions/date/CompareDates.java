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

package org.talend.dataprep.transformation.actions.date;

import java.time.LocalDateTime;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.*;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + CompareDates.ACTION_NAME)
public class CompareDates extends AbstractCompareAction implements ColumnAction, OtherColumnParameters, CompareAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "compare_dates"; //$NON-NLS-1$

    // -----------------------------------------
    // Overriding the default as we need
    // different labels for dates
    // -----------------------------------------
    public static final String EQ = "date.eq";

    public static final String NE = "date.ne";

    public static final String GT = "date.gt";

    public static final String GE = "date.ge";

    public static final String LT = "date.lt";

    public static final String LE = "date.le";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.DATE.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.DATE.isAssignableFrom(columnType);
    }

    @Override
    protected String getCompareMode(Map<String, String> parameters) {
        String dateCompareMode = parameters.get(COMPARE_MODE);
        return StringUtils.substringAfter(dateCompareMode, "date.");
    }

    @Override
    protected Parameter getDefaultConstantValue() {
        // olamy the javascript will tranform to now if empty
        return new Parameter(CONSTANT_VALUE, ParameterType.DATE, StringUtils.EMPTY);
    }

    @Override
    protected SelectParameter getCompareModeSelectParameter() {

        //@formatter:off
        return SelectParameter.Builder.builder() //
            .name(COMPARE_MODE) //
            .item(EQ) //
            .item(NE) //
            .item(GT) //
            .item(GE) //
            .item(LT) //
            .item(LE) //
            .defaultValue(EQ) //
            .build();
        //@formatter:on

    }

    @Override
    protected int doCompare(ComparisonRequest comparisonRequest) {

        if (StringUtils.isEmpty(comparisonRequest.value1) //
                || StringUtils.isEmpty(comparisonRequest.value2)) {
            return ERROR_COMPARE_RESULT;
        }

        try {
            final DateParser dateParser = Providers.get(DateParser.class);
            final LocalDateTime temporalAccessor1 = dateParser.parse(comparisonRequest.value1, comparisonRequest.colMetadata1);

            // we compare with the format of the first column when the comparison is with a CONSTANT
            final LocalDateTime temporalAccessor2 = dateParser.parse(comparisonRequest.value2,
                    comparisonRequest.mode.equals(CONSTANT_MODE) ? //
                            comparisonRequest.colMetadata2 : comparisonRequest.colMetadata1);

            return temporalAccessor1.compareTo(temporalAccessor2);
        } catch (Exception e) {
            return ERROR_COMPARE_RESULT;
        }
    }

}
