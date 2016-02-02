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

import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.number.BigDecimalParser;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

@Component(CompareNumbers.ACTION_BEAN_PREFIX + CompareNumbers.ACTION_NAME)
public class CompareNumbers extends ActionMetadata implements ColumnAction, OtherColumnParameters {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "compare_numbers"; //$NON-NLS-1$

    protected static final String CONSTANT_VALUE = "constant_value"; //$NON-NLS-1$

    protected static final String COMPARE_MODE = "compare_mode"; //$NON-NLS-1$

    protected static final String EQ = "eq";

    protected static final String NE = "ne";

    protected static final String GT = "gt";

    protected static final String GE = "ge";

    protected static final String LT = "lt";

    protected static final String LE = "le";

    private static final Logger LOGGER = LoggerFactory.getLogger(CompareNumbers.class);

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        Type columnType = Type.get(column.getType());
        return Type.NUMERIC.isAssignableFrom(columnType);
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.MATH.getDisplayName();
    }

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
        parameters.add(SelectParameter.Builder.builder()
                        .name(MODE_PARAMETER)
                        .item(CONSTANT_MODE, new Parameter(CONSTANT_VALUE, ParameterType.STRING, "2"))
                        .item(OTHER_COLUMN_MODE, new Parameter(SELECTED_COLUMN_PARAMETER, ParameterType.COLUMN, StringUtils.EMPTY, false, false))
                        .defaultValue(CONSTANT_MODE)
                        .build()
        );
        //@formatter:on

        return parameters;
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

        row.set(newColumnId, toStringTrueFalse(compare(row.get(columnId), getValueToCompareWith(parameters, row), compareMode)));
    }

    private String getValueToCompareWith(Map<String, String> parameters, DataSetRow row) {
        if (parameters.get(MODE_PARAMETER).equals(CONSTANT_MODE)) {
            return parameters.get(CONSTANT_VALUE);
        } else {
            final ColumnMetadata selectedColumn = row.getRowMetadata().getById(parameters.get(SELECTED_COLUMN_PARAMETER));
            return row.get(selectedColumn.getId());
        }
    }

    protected boolean compare(String value1, String value2, String mode) {
        try {
            final BigDecimal value = BigDecimalParser.toBigDecimal(value1);
            final BigDecimal toCompare = BigDecimalParser.toBigDecimal(value2);
            final int result = value.compareTo(toCompare);

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
        } catch (NumberFormatException e) {
            LOGGER.debug("Unable to compare values '{}' and '{}'", value1, value2, e);
            return false;
        }
    }
}
