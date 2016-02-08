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

    /**
     * do the real comparison
     * 
     * @param value1
     * @param value2
     * @return same result as {@link Comparable#compareTo(Object)}
     */
    protected abstract int doCompare(String value1, String value2);

    public boolean compare(String value1, String value2, String mode) {
        try {
            final int result = doCompare(value1, value2);

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
