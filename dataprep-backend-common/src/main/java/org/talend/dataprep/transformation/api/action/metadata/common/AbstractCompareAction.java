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

package org.talend.dataprep.transformation.api.action.metadata.common;

import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

public abstract class AbstractCompareAction extends ActionMetadata implements ColumnAction, OtherColumnParameters, CompareAction {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(COMPARE_MODE)
                        .item(EQ)
                        .item(NE)
                        .item(GT)
                        .item(GE)
                        .item(LT)
                        .item(LE)
                        .defaultValue(EQ)
                        .build()
        );
        //@formatter:on

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder() //
                        .name(MODE_PARAMETER) //
                        .item(CONSTANT_MODE, getDefaultConstantValue()) //
                        .item(OTHER_COLUMN_MODE, new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, StringUtils.EMPTY, false, false)) //
                        .defaultValue(CONSTANT_MODE)
                        .build()
        );
        //@formatter:on

        return parameters;
    }

    protected Parameter getDefaultConstantValue(){
        // olamy no idea why this 2 but was here before so just keep backward compat :-)
        return new Parameter(CONSTANT_VALUE, ParameterType.STRING, "2");
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final RowMetadata rowMetadata = row.getRowMetadata();
        final Map<String, String> parameters = context.getParameters();

        String compareMode = parameters.get(COMPARE_MODE);

        String compareToLabel;
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            compareToLabel = parameters.get(CONSTANT_VALUE);
        } else {
            final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            compareToLabel = selectedColumn.getName();
        }

        // create new column and append it after current column
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final String newColumnId = context.column("result", (r) -> {
            final ColumnMetadata c = ColumnMetadata.Builder //
                    .column() //
                    .name(column.getName() + "_" + compareMode + "_" + compareToLabel + "?") //
                    .type(Type.BOOLEAN) //
                    .build();
            rowMetadata.insertAfter(columnId, c);
            return c;
        });

        ComparisonRequest comparisonRequest = new ComparisonRequest() //
                .setMode(compareMode) //
                .setColumnMetadata1(row.getRowMetadata().getById(columnId)) //
                .setValue1(row.get(columnId)) //
                // this can be null when comparing with a constant
                .setColumnMetadata2(getColumnMetadataToCompareWith(parameters, row)) //
                .setValue2(getValueToCompareWith(parameters, row));
        row.set(newColumnId, toStringTrueFalse(compare(comparisonRequest)));
    }

    private String getValueToCompareWith(Map<String, String> parameters, DataSetRow row) {
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            return parameters.get(CONSTANT_VALUE);
        } else {
            final ColumnMetadata selectedColumn = row.getRowMetadata().getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            return row.get(selectedColumn.getId());
        }
    }

    private ColumnMetadata getColumnMetadataToCompareWith(Map<String, String> parameters, DataSetRow row) {
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            // we return the primary columnMetadata as we do not have an other one.
            return null;
        }
        final ColumnMetadata selectedColumn = row.getRowMetadata().getById(parameters.get(SELECTED_COLUMN_PARAMETER));
        return selectedColumn;

    }

    /**
     * do the real comparison
     * 
     * @param comparisonRequest
     * @return same result as {@link Comparable#compareTo(Object)}
     */
    protected abstract int doCompare(ComparisonRequest comparisonRequest);

    /**
     * bean to ease passing values to do comparison (easier adding fields than changing method parameters)
     */
    public static class ComparisonRequest {

        public String value1, value2;

        public ColumnMetadata colMetadata1, colMetadata2;

        public String mode;

        public ComparisonRequest setValue1(String value1) {
            this.value1 = value1;
            return this;
        }

        public ComparisonRequest setValue2(String value2) {
            this.value2 = value2;
            return this;
        }

        public ComparisonRequest setColumnMetadata1(ColumnMetadata colMetadata1) {
            this.colMetadata1 = colMetadata1;
            return this;
        }

        public ComparisonRequest setColumnMetadata2(ColumnMetadata colMetadata2) {
            this.colMetadata2 = colMetadata2;
            return this;
        }

        public ComparisonRequest setMode(String mode) {
            this.mode = mode;
            return this;
        }



        @Override
        public String toString() {
            return "ComparisonRequest{" + "colMetadata1=" + colMetadata1 + ", value1='" + value1 + '\'' + ", value2='" + value2
                    + '\'' + ", colMetadata2=" + colMetadata2 + ", mode='" + mode + '\'' + '}';
        }
    }

    public boolean compare(ComparisonRequest comparisonRequest) {
        try {
            final int result = doCompare(comparisonRequest);

            switch (comparisonRequest.mode) {
            case EQ:
                return result == 0;
            case NE:
                return result != 0;
            case GT:
                return result > 0;
            case GE:
                return result >= 0;
            case LT:
                return result < 0;
            case LE:
                return result <= 0;
            default:
                return false;
            }
        } catch (NumberFormatException e) {
            LOGGER.debug("Unable to compare values '{}' ", comparisonRequest, e);
            return false;
        }
    }
}
