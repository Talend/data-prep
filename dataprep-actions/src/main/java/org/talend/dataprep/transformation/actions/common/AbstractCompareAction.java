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

package org.talend.dataprep.transformation.actions.common;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.COLUMN;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.getColumnCreationParameter;

import java.util.*;

import javax.annotation.Nonnull;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

public abstract class AbstractCompareAction extends AbstractActionMetadata
        implements ColumnAction, OtherColumnParameters, CompareAction {

    public static final int ERROR_COMPARE_RESULT = Integer.MIN_VALUE;

    public static final String ERROR_COMPARE_RESULT_LABEL = StringUtils.EMPTY;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompareAction.class);

    private static final boolean CREATE_NEW_COLUMN_DEFAULT_VALUE = true;

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT_VALUE));

        parameters.add(getCompareModeSelectParameter(locale));

        //@formatter:off
        parameters.add(selectParameter(locale) //
                        .name(MODE_PARAMETER) //
                        .item(CONSTANT_MODE, CONSTANT_MODE, getDefaultConstantValue(locale)) //
                        .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE, parameter(locale).setName(SELECTED_COLUMN_PARAMETER).setType(COLUMN).setDefaultValue(EMPTY).setCanBeBlank(false).build(this)) //
                        .defaultValue(CONSTANT_MODE)
                        .build(this )
        );
        //@formatter:on

        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT_VALUE)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
    }

    /**
     * can be overridden as keys can be different (date have different keys/labels)
     *
     * @return {@link SelectParameter}
     */
    protected SelectParameter getCompareModeSelectParameter(Locale locale) {

        //@formatter:off
        return SelectParameter.selectParameter(locale) //
                           .name(CompareAction.COMPARE_MODE) //
                           .item(EQ, EQ) //
                           .item(NE, NE) //
                           .item(GT, GT) //
                           .item(GE, GE) //
                           .item(LT, LT) //
                           .item(LE, LE) //
                           .defaultValue(EQ) //
                           .build(this );
        //@formatter:on

    }

    /**
     *
     * @return {@link Parameter} the default value (can be a different type/value)
     */
    protected Parameter getDefaultConstantValue(Locale locale) {
        // olamy no idea why this 2 but was here before so just keep backward compat :-)
        return Parameter.parameter(locale).setName(CONSTANT_VALUE)
                .setType(ParameterType.STRING)
                .setDefaultValue("2")
                .build(this);
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        final List<ActionsUtils.AdditionalColumn> additionalColumns = new ArrayList<>();

        final RowMetadata rowMetadata = context.getRowMetadata();
        final Map<String, String> parameters = context.getParameters();
        final String compareMode = getCompareMode(parameters);

        String compareToLabel;
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            compareToLabel = parameters.get(CONSTANT_VALUE);
        } else {
            final ColumnMetadata selectedColumn = rowMetadata.getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            compareToLabel = selectedColumn.getName();
        }

        additionalColumns.add(ActionsUtils.additionalColumn()
                .withName(context.getColumnName() + "_" + compareMode + "_" + compareToLabel + "?")
                .withType(Type.BOOLEAN));

        return additionalColumns;
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();
        final String compareMode = getCompareMode(parameters);

        final String newColumnId = ActionsUtils.getTargetColumnId(context);

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
     */
    protected String getCompareMode(Map<String, String> parameters) {
        return parameters.get(CompareAction.COMPARE_MODE);
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
     * Do the real comparison.
     *
     * @return same result as {@link Comparable#compareTo(Object)} if any type issue or any problem use
     * #ERROR_COMPARE_RESULT
     */
    protected abstract int doCompare(ComparisonRequest comparisonRequest);

    /**
     * Transforming boolean to <code>true</code> or <code>false</code> as String in case of #doCompare returning
     * #ERROR_COMPARE_RESULT the label #ERROR_COMPARE_RESULT_LABEL is returned.
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

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.singleton(Behavior.METADATA_CREATE_COLUMNS);
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
