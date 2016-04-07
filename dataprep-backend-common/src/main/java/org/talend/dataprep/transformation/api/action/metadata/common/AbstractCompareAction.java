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

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;

public abstract class AbstractCompareAction extends ActionMetadata implements ColumnAction, OtherColumnParameters, CompareAction {

    public static final int ERROR_COMPARE_RESULT = Integer.MIN_VALUE;

    public static final String ERROR_COMPARE_RESULT_LABEL = StringUtils.EMPTY;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompareAction.class);

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        parameters.add(getCompareModeSelectParameter());

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder() //
                        .name(MODE_PARAMETER) //
                        .item(CONSTANT_MODE, getDefaultConstantValue()) //
                        .item(OTHER_COLUMN_MODE, new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, //
                                                               StringUtils.EMPTY, false, false, //
                                                               StringUtils.EMPTY, getMessagesBundle())) //
                        .defaultValue(CONSTANT_MODE)
                        .build()
        );
        //@formatter:on

        return parameters;
    }

    /**
     * can be overridden as keys can be different (date have different keys/labels)
     * 
     * @return {@link SelectParameter}
     */
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

    /**
     *
     * @return {@link Parameter} the default value (can be a different type/value)
     */
    protected Parameter getDefaultConstantValue() {
        // olamy no idea why this 2 but was here before so just keep backward compat :-)
        return new Parameter(CONSTANT_VALUE, ParameterType.STRING, "2");
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            final String columnId = context.getColumnId();
            final RowMetadata rowMetadata = context.getRowMetadata();
            final Map<String, String> parameters = context.getParameters();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            final String compareMode = getCompareMode(parameters);

            String compareToLabel;
            if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
                compareToLabel = parameters.get(CONSTANT_VALUE);
            } else {
                final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
                compareToLabel = selectedColumn.getName();
            }

            context.column("result", (r) -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .name(column.getName() + "_" + compareMode + "_" + compareToLabel + "?") //
                        .type(Type.BOOLEAN) //
                        .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();
        final String compareMode = getCompareMode(parameters);

        // create new column and append it after current column
        final String newColumnId = context.column("result");

        ComparisonRequest comparisonRequest = new ComparisonRequest() //
                .setMode(compareMode) //
                .setColumnMetadata1(context.getRowMetadata().getById(columnId)) //
                .setValue1(row.get(columnId)) //
                // this can be null when comparing with a constant
                .setColumnMetadata2(getColumnMetadataToCompareWith(parameters, context)) //
                .setValue2(getValueToCompareWith(parameters, context, row));
        row.set(newColumnId, toStringCompareResult(comparisonRequest));
    }

    /**
     * can be overridden as keys can be different (date have different keys/labels)
     * 
     * @param parameters
     * @return
     */
    protected String getCompareMode(Map<String, String> parameters) {
        return parameters.get(COMPARE_MODE);
    }

    private String getValueToCompareWith(Map<String, String> parameters, ActionContext context, DataSetRow row) {
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            return parameters.get(CONSTANT_VALUE);
        } else {
            final ColumnMetadata selectedColumn = context.getRowMetadata().getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            return row.get(selectedColumn.getId());
        }
    }

    private ColumnMetadata getColumnMetadataToCompareWith(Map<String, String> parameters, ActionContext context) {
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            // we return the primary columnMetadata as we do not have an other one.
            return null;
        }
        return context.getRowMetadata().getById(parameters.get(SELECTED_COLUMN_PARAMETER));

    }

    /**
     * do the real comparison
     * 
     * @param comparisonRequest
     * @return same result as {@link Comparable#compareTo(Object)} if any type issue or any problem use
     * #ERROR_COMPARE_RESULT
     */
    protected abstract int doCompare(ComparisonRequest comparisonRequest);

    /**
     *
     * @param comparisonRequest
     * @return transforming boolean to <code>true</code> or <code>false</code> as String in case of #doCompare returning
     * #ERROR_COMPARE_RESULT the label #ERROR_COMPARE_RESULT_LABEL is returned
     */
    public String toStringCompareResult(ComparisonRequest comparisonRequest) {
        boolean booleanResult;
        try {

            final int result = doCompare(comparisonRequest);

            if (result == ERROR_COMPARE_RESULT) {
                return ERROR_COMPARE_RESULT_LABEL;
            }

            booleanResult = compareResultToBoolean(result, comparisonRequest.mode);

        } catch (NumberFormatException e) {
            LOGGER.debug("Unable to compare values '{}' ", comparisonRequest, e);
            return ERROR_COMPARE_RESULT_LABEL;
        }

        return BooleanUtils.toString(booleanResult, Boolean.TRUE.toString(), Boolean.FALSE.toString());
    }

    protected boolean compareResultToBoolean(final int result, String mode) {
        switch (mode) {
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
    }

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
            return "ComparisonRequest{" + "colMetadata1=" + colMetadata1 //
                    + ", value1='" + value1 + '\'' //
                    + ", value2='" + value2 + '\'' //
                    + ", colMetadata2=" + colMetadata2 //
                    + ", mode='" + mode + '\'' + '}';
        }
    }

}
