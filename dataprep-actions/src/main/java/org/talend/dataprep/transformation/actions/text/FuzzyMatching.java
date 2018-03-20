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

package org.talend.dataprep.transformation.actions.text;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.*;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.*;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Create a new column with Boolean result <code>true</code> if the Levenstein distance is less or equals the parameter
 */
@Action(FuzzyMatching.ACTION_NAME)
public class FuzzyMatching extends AbstractActionMetadata implements ColumnAction {

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
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS.getDisplayName(locale);
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
        public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);

        parameters.add(selectParameter(locale) //
                .name(MODE_PARAMETER) //
                .item(CONSTANT_MODE, CONSTANT_MODE,//
                        parameter(locale).setName(VALUE_PARAMETER)
                                .setType(STRING)
                                .setDefaultValue(EMPTY)
                                .build(this)) //
                .item(OTHER_COLUMN_MODE, OTHER_COLUMN_MODE,//
                        parameter(locale).setName(SELECTED_COLUMN_PARAMETER)
                                .setType(COLUMN)
                                .setDefaultValue(EMPTY)
                                .setCanBeBlank(false)
                                .build(this)) //
                .defaultValue(CONSTANT_MODE).build(this));

        parameters.add(parameter(locale).setName(SENSITIVITY)
                .setType(INTEGER)
                .setDefaultValue("1")
                .setCanBeBlank(false)
                .build(this));
        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), true)) {
            ActionsUtils.createNewColumn(context, singletonList(
                    ActionsUtils.additionalColumn().withName(context.getColumnName() + APPENDIX).withType(Type.BOOLEAN)));
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        Map<String, String> parameters = context.getParameters();

        int sensitivity = NumberUtils.toInt(parameters.get(SENSITIVITY));

        // create new column and append it after current column
        RowMetadata rowMetadata = context.getRowMetadata();

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
        row.set(ActionsUtils.getTargetColumnId(context), columnValue);
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
