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

package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.BOOLEAN;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.INTEGER;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

/**
 * Create a new column with Boolean result <code>true</code> if the Levenstein distance is less or equals the parameter
 */
@Component(LevenshteinDistance.ACTION_BEAN_PREFIX + LevenshteinDistance.ACTION_NAME)
public class LevenshteinDistance extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "levenshtein_distance";

    public static final String VALUE_PARAMETER = "levenshtein_compare_value";

    public static final String DISTANCE_PARAMETER = "levenshtein_distance_value";

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_ld_distance";

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        parameters.add(SelectParameter.Builder.builder() //
                .name(OtherColumnParameters.MODE_PARAMETER) //
                .item(OtherColumnParameters.CONSTANT_MODE, //
                        new Parameter(VALUE_PARAMETER, ParameterType.STRING, EMPTY)) //
                .item(OtherColumnParameters.OTHER_COLUMN_MODE, //
                        new Parameter(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, //
                                ParameterType.COLUMN, //
                                StringUtils.EMPTY, false, false)) //
                .defaultValue(OtherColumnParameters.CONSTANT_MODE).build());

        parameters.add(new Parameter(DISTANCE_PARAMETER, INTEGER, "0"));
        return parameters;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        Map<String, String> parameters = context.getParameters();

        String maxDistance = parameters.get(DISTANCE_PARAMETER);

        // create new column and append it after current column
        RowMetadata rowMetadata = row.getRowMetadata();
        ColumnMetadata column = rowMetadata.getById(columnId);

        final String levenshteinDistanceColumn = context.column(column.getName() + APPENDIX, (r) -> {
            final ColumnMetadata c = ColumnMetadata.Builder //
                    .column() //
                    .name(column.getName() + APPENDIX) //
                    .type(BOOLEAN) //
                    .empty(column.getQuality().getEmpty()) //
                    .invalid(column.getQuality().getInvalid()) //
                    .valid(column.getQuality().getValid()) //
                    .headerSize(column.getHeaderSize()) //
                    .build();
            rowMetadata.insertAfter(columnId, c);
            return c;
        });

        String value = row.get(context.getColumnId());
        int levenshteinDistance = 0;
        if (parameters.get(OtherColumnParameters.MODE_PARAMETER).equals(OtherColumnParameters.CONSTANT_MODE)) {
            String paramValue = parameters.get(VALUE_PARAMETER);
            levenshteinDistance = StringUtils.getLevenshteinDistance(value, paramValue);
        } else {
            final ColumnMetadata selectedColumn = rowMetadata
                    .getById(parameters.get(OtherColumnParameters.SELECTED_COLUMN_PARAMETER));
            String paramValue = row.get(selectedColumn.getId());
            levenshteinDistance = StringUtils.getLevenshteinDistance(value, paramValue);
        }

        final String columnValue = toStringTrueFalse(levenshteinDistance <= NumberUtils.toInt(maxDistance));
        row.set(levenshteinDistanceColumn, columnValue);

    }
}
