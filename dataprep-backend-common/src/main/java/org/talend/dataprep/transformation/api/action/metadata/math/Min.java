//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.CONSTANT_VALUE;
import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.SELECTED_COLUMN_PARAMETER;
import static org.talend.dataprep.transformation.api.action.metadata.math.Min.MIN_NAME;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;

/**
 * Calculate Min with a constant or an other column 
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + MIN_NAME)
public class Min extends AbstractBasicMathAction {

    protected static final String MIN_NAME = "min_numbers";

    @Override
    public String getName() {
        return MIN_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.MATH.getDisplayName();
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    /**
     * @see ActionMetadata#compile(ActionContext)
     */
    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {

            String columnId = context.getColumnId();
            RowMetadata rowMetadata = context.getRowMetadata();
            ColumnMetadata column = rowMetadata.getById(columnId);

            // create new column and append it after current column
            context.column("result", r -> {
                ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .name(column.getName() + "_min") //
                        .type(Type.STRING) // Leave actual type detection to transformation
                        .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        String columnId = context.getColumnId();
        String colValue = row.get(columnId);

        Map<String, String> parameters = context.getParameters();

        String mode = parameters.get(OtherColumnParameters.MODE_PARAMETER);

        String minWith;
        switch (mode) {
        case OtherColumnParameters.CONSTANT_MODE:
            minWith = parameters.get(CONSTANT_VALUE);
            break;
        case OtherColumnParameters.OTHER_COLUMN_MODE:
            String otherColId = parameters.get(SELECTED_COLUMN_PARAMETER);
            minWith = row.get(otherColId);
            break;
        default:
            throw new TDPException(CommonErrorCodes.BAD_ACTION_PARAMETER, //
                    ExceptionContext.build().put("paramName", OtherColumnParameters.CONSTANT_MODE));
        }

        String min = Double.toString(NumberUtils.toDouble(colValue, 0));

        if (StringUtils.isNotBlank(minWith)) {
            min = Double.toString(FastMath.min(NumberUtils.toDouble(colValue, Double.MAX_VALUE), //
                    NumberUtils.toDouble(minWith, Double.MAX_VALUE)));
        }

        String newColumnId = context.column("result");
        row.set(newColumnId, min);
    }
}
