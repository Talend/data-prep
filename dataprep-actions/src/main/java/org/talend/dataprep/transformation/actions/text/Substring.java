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

package org.talend.dataprep.transformation.actions.text;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.STRINGS;

import java.util.*;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + Substring.SUBSTRING_ACTION_NAME)
public class Substring extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String SUBSTRING_ACTION_NAME = "substring"; //$NON-NLS-1$

    protected static final String FROM_MODE_PARAMETER = "from_mode"; //$NON-NLS-1$

    protected static final String FROM_BEGINNING = "from_beginning"; //$NON-NLS-1$

    protected static final String FROM_INDEX_PARAMETER = "from_index"; //$NON-NLS-1$

    protected static final String FROM_N_BEFORE_END_PARAMETER = "from_n_before_end"; //$NON-NLS-1$

    protected static final String TO_MODE_PARAMETER = "to_mode"; //$NON-NLS-1$

    protected static final String TO_END = "to_end"; //$NON-NLS-1$

    protected static final String TO_INDEX_PARAMETER = "to_index"; //$NON-NLS-1$

    protected static final String TO_N_BEFORE_END_PARAMETER = "to_n_before_end"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    private static final String APPENDIX = "_substring"; //$NON-NLS-1$

    @Override
    public String getName() {
        return SUBSTRING_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return STRINGS.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final Parameter fromIndexParameters = Parameter.parameter(locale).setName(FROM_INDEX_PARAMETER)
                .setType(ParameterType.INTEGER)
                .setDefaultValue("0")
                .build(this);
        final Parameter fromNBeforeEndParameters = Parameter.parameter(locale).setName(FROM_N_BEFORE_END_PARAMETER)
                .setType(ParameterType.INTEGER)
                .setDefaultValue("5")
                .build(this);
        final Parameter toIndexParameters = Parameter.parameter(locale).setName(TO_INDEX_PARAMETER)
                .setType(ParameterType.INTEGER)
                .setDefaultValue("5")
                .build(this);
        final Parameter toNBeforeEndParameters = Parameter.parameter(locale).setName(TO_N_BEFORE_END_PARAMETER)
                .setType(ParameterType.INTEGER)
                .setDefaultValue("1")
                .build(this);

        // "to" parameter with all possible values
        final Parameter toCompleteParameters = SelectParameter.selectParameter(locale) //
                .name(TO_MODE_PARAMETER) //
                .item(TO_END, TO_END) //
                .item(TO_INDEX_PARAMETER, TO_INDEX_PARAMETER, toIndexParameters) //
                .item(TO_N_BEFORE_END_PARAMETER, TO_N_BEFORE_END_PARAMETER, toNBeforeEndParameters) //
                .defaultValue(TO_INDEX_PARAMETER) //
                .build(this);

        // "to" parameter with possible values for "From N Before End" selection
        // the "to index" option should not be available
        final Parameter toParametersWithoutIndexSelection = SelectParameter.selectParameter(locale) //
                .name(TO_MODE_PARAMETER) //
                .item(TO_END, TO_END) //
                .item(TO_N_BEFORE_END_PARAMETER, TO_N_BEFORE_END_PARAMETER, toNBeforeEndParameters) //
                .defaultValue(TO_END) //
                .build(this);

        // "from" parameter
        final Parameter fromParameters = SelectParameter.selectParameter(locale) //
                .name(FROM_MODE_PARAMETER) //
                .item(FROM_BEGINNING, FROM_BEGINNING, toCompleteParameters) // has all the "To" choices
                .item(FROM_INDEX_PARAMETER, FROM_INDEX_PARAMETER, fromIndexParameters, toCompleteParameters) // has all the "To" choices
                .item(FROM_N_BEFORE_END_PARAMETER, FROM_N_BEFORE_END_PARAMETER, fromNBeforeEndParameters, toParametersWithoutIndexSelection) // cannot
                                                                                                                // choose
                                                                                                                // "To
                                                                                                                // index"
                .defaultValue(FROM_BEGINNING) //
                .build(this);

        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(fromParameters);
        return parameters;
    }

    @Override
    public boolean getCreateNewColumnDefaultValue() {
        return true;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public String getCreatedColumnName(ActionContext context) {
        return context.getColumnName() + APPENDIX;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        // create the new column
        final RowMetadata rowMetadata = context.getRowMetadata();
        final String columnId = context.getColumnId();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final String substringColumn = getTargetColumnId(context);

        // Perform substring
        final String value = row.get(columnId);
        if (value == null) {
            return;
        }
        final Map<String, String> parameters = context.getParameters();
        final int realFromIndex = getStartIndex(parameters, value);
        final int realToIndex = getEndIndex(parameters, value);

        try {
            final String newValue = value.substring(realFromIndex, realToIndex);
            row.set(substringColumn, newValue);
        } catch (IndexOutOfBoundsException e) {
            // Nothing to do in that case, just set with the empty string:
            row.set(substringColumn, EMPTY);
        }
    }

    /**
     * Compute the end index. This won't be more than the value length
     *
     * @param parameters the parameters
     * @param value the value to substring
     * @return the end index
     */
    private int getEndIndex(final Map<String, String> parameters, final String value) {
        switch (parameters.get(TO_MODE_PARAMETER)) {
        case TO_INDEX_PARAMETER:
            return Math.min(Integer.parseInt(parameters.get(TO_INDEX_PARAMETER)), value.length());
        case TO_N_BEFORE_END_PARAMETER:
            final int nbChars = Math.max(0, Integer.parseInt(parameters.get(TO_N_BEFORE_END_PARAMETER)));
            return Math.max(0, value.length() - nbChars);
        case TO_END:
        default:
            return value.length();
        }
    }

    /**
     * Compute the start index. This won't be more than the value length
     *
     * @param parameters the parameters
     * @param value the value to substring
     * @return the start index
     */
    private int getStartIndex(final Map<String, String> parameters, String value) {
        switch (parameters.get(FROM_MODE_PARAMETER)) {
        case FROM_INDEX_PARAMETER:
            final int index = Math.max(0, Integer.parseInt(parameters.get(FROM_INDEX_PARAMETER)));
            return Math.min(index, value.length());
        case FROM_N_BEFORE_END_PARAMETER:
            return Math.max(0, value.length() - Integer.parseInt(parameters.get(FROM_N_BEFORE_END_PARAMETER)));
        case FROM_BEGINNING:
        default:
            return 0;
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
