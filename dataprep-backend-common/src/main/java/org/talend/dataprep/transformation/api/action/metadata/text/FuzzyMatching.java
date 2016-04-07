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
import static org.talend.dataprep.parameters.ParameterType.INTEGER;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;

/**
 * Create a new column with Boolean result <code>true</code> if the Levenstein distance is less or equals the parameter
 */
@Component(FuzzyMatching.ACTION_BEAN_PREFIX + FuzzyMatching.ACTION_NAME)
public class FuzzyMatching extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "fuzzy_matching";

    public static final String VALUE_PARAMETER = "reference_value";

    public static final String SENSITIVITY = "sensitivity";

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_matches";

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
                        new Parameter(VALUE_PARAMETER, ParameterType.STRING, EMPTY, false, true, StringUtils.EMPTY, getMessagesBundle())) //
                .item(OtherColumnParameters.OTHER_COLUMN_MODE, //
                        new Parameter(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, //
                                ParameterType.COLUMN, //
                                StringUtils.EMPTY, false, false, StringUtils.EMPTY, getMessagesBundle())) //
                .defaultValue(OtherColumnParameters.CONSTANT_MODE).build());

        parameters.add(new Parameter(SENSITIVITY, INTEGER, "1", false, false, StringUtils.EMPTY, getMessagesBundle()));
        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            final String columnId = context.getColumnId();
            // create new column and append it after current column
            RowMetadata rowMetadata = context.getRowMetadata();
            ColumnMetadata column = rowMetadata.getById(columnId);

            context.column(column.getName() + APPENDIX, (r) -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .name(column.getName() + APPENDIX) //
                        .type(BOOLEAN) //
                        .typeForce(true) //
                        .empty(column.getQuality().getEmpty()) //
                        .invalid(column.getQuality().getInvalid()) //
                        .valid(column.getQuality().getValid()) //
                        .headerSize(column.getHeaderSize()) //
                        .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        Map<String, String> parameters = context.getParameters();

        int sensitivity = NumberUtils.toInt(parameters.get(SENSITIVITY));

        // create new column and append it after current column
        RowMetadata rowMetadata = context.getRowMetadata();
        ColumnMetadata column = rowMetadata.getById(columnId);

        final String fuzzyMatches = context.column(column.getName() + APPENDIX);

        String value = row.get(context.getColumnId());
        String referenceValue;
        if (parameters.get(OtherColumnParameters.MODE_PARAMETER).equals(OtherColumnParameters.CONSTANT_MODE)) {
            referenceValue = parameters.get(VALUE_PARAMETER);
        } else {
            final ColumnMetadata selectedColumn = rowMetadata
                    .getById(parameters.get(OtherColumnParameters.SELECTED_COLUMN_PARAMETER));
            referenceValue = row.get(selectedColumn.getId());
        }

        final String columnValue = toStringTrueFalse(fuzzyMatches(value, referenceValue, sensitivity));
        row.set(fuzzyMatches, columnValue);
    }

    private boolean fuzzyMatches(String value, String reference, int sensitivity) {
        int levenshteinDistance = StringUtils.getLevenshteinDistance(value, reference);
        return levenshteinDistance <= sensitivity;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
    
}
