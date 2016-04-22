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

import static org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters.*;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.util.FastMath;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

/**
 * Abstract Action for basic math action with one parameter (constant or an other column)
 */
public abstract class AbstractMathOneParameterAction extends ActionMetadata implements ColumnAction {

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = super.getParameters();

        parameters.add(SelectParameter.Builder.builder() //
                .name(MODE_PARAMETER) //
                .item(CONSTANT_MODE, new Parameter(CONSTANT_VALUE, ParameterType.STRING, StringUtils.EMPTY)) //
                .item(OTHER_COLUMN_MODE,
                        new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, //
                                StringUtils.EMPTY, false, false, //
                                getMessagesBundle())) //
                .defaultValue(CONSTANT_MODE) //
                .build());

        return parameters;
    }

    protected abstract String getColumnNameSuffix();

    /**
     * @see ActionMetadata#compile(ActionContext)
     */
    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {

            String columnId = context.getColumnId();
            RowMetadata rowMetadata = context.getRowMetadata();
            ColumnMetadata column = rowMetadata.getById( columnId);

            // create new column and append it after current column
            context.column("result", r -> {
                ColumnMetadata c = ColumnMetadata.Builder //
                    .column() //
                    .name(column.getName() + "_" + getColumnNameSuffix()) //
                    .type( Type.STRING) // Leave actual type detection to transformation
                    .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });
        }
    }


    protected abstract String calculateResult(String columnValue, String parameter);

    @Override
    public void applyOnColumn( DataSetRow row, ActionContext context) {
        String columnId = context.getColumnId();
        String colValue = row.get(columnId);

        Map<String, String> parameters = context.getParameters();

        String mode = parameters.get( OtherColumnParameters.MODE_PARAMETER);

        String parameterValue;
        switch (mode) {
            case OtherColumnParameters.CONSTANT_MODE:
                parameterValue = parameters.get(CONSTANT_VALUE);
                break;
            case OtherColumnParameters.OTHER_COLUMN_MODE:
                String otherColId = parameters.get(SELECTED_COLUMN_PARAMETER);
                parameterValue = row.get(otherColId);
                break;
            default:
                throw new TDPException( CommonErrorCodes.BAD_ACTION_PARAMETER, //
                                        ExceptionContext.build().put( "paramName", OtherColumnParameters.CONSTANT_MODE));
        }

        String result = calculateResult( colValue, parameterValue );

        String newColumnId = context.column("result");
        row.set(newColumnId, result);
    }
}
