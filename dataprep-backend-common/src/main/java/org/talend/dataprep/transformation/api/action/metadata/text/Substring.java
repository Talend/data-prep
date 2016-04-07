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

package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.STRINGS;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;

@Component(Substring.ACTION_BEAN_PREFIX + Substring.SUBSTRING_ACTION_NAME)
public class Substring extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String SUBSTRING_ACTION_NAME = "substring"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    private static final String APPENDIX = "_substring"; //$NON-NLS-1$

    protected static final String FROM_MODE_PARAMETER = "from_mode"; //$NON-NLS-1$
    protected static final String FROM_BEGINNING = "from_beginning"; //$NON-NLS-1$
    protected static final String FROM_INDEX_PARAMETER = "from_index"; //$NON-NLS-1$
    protected static final String FROM_N_BEFORE_END_PARAMETER = "from_n_before_end"; //$NON-NLS-1$

    protected static final String TO_MODE_PARAMETER = "to_mode"; //$NON-NLS-1$
    protected static final String TO_END = "to_end"; //$NON-NLS-1$
    protected static final String TO_INDEX_PARAMETER = "to_index"; //$NON-NLS-1$
    protected static final String TO_N_BEFORE_END_PARAMETER = "to_n_before_end"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return SUBSTRING_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return STRINGS.getDisplayName();
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        final Parameter fromIndexParameters = new Parameter(FROM_INDEX_PARAMETER, ParameterType.INTEGER, "0");
        final Parameter fromNBeforeEndParameters = new Parameter(FROM_N_BEFORE_END_PARAMETER, ParameterType.INTEGER, "5");
        final Parameter toIndexParameters = new Parameter(TO_INDEX_PARAMETER, ParameterType.INTEGER, "5");
        final Parameter toNBeforeEndParameters = new Parameter(TO_N_BEFORE_END_PARAMETER, ParameterType.INTEGER, "1");

        // "to" parameter with all possible values
        final Parameter toCompleteParameters = SelectParameter.Builder.builder() //
                .name(TO_MODE_PARAMETER) //
                .item(TO_END) //
                .item(TO_INDEX_PARAMETER, toIndexParameters) //
                .item(TO_N_BEFORE_END_PARAMETER, toNBeforeEndParameters) //
                .defaultValue(TO_INDEX_PARAMETER) //
                .build();

        // "to" parameter with possible values for "From N Before End" selection
        // the "to index" option should not be available
        final Parameter toParametersWithoutIndexSelection = SelectParameter.Builder.builder() //
                .name(TO_MODE_PARAMETER) //
                .item(TO_END) //
                .item(TO_N_BEFORE_END_PARAMETER, toNBeforeEndParameters) //
                .defaultValue(TO_END) //
                .build();

        // "from" parameter
        final Parameter fromParameters = SelectParameter.Builder.builder() //
                .name(FROM_MODE_PARAMETER) //
                .item(FROM_BEGINNING, toCompleteParameters) // has all the "To" choices
                .item(FROM_INDEX_PARAMETER, fromIndexParameters, toCompleteParameters) // has all the "To" choices
                .item(FROM_N_BEFORE_END_PARAMETER, fromNBeforeEndParameters, toParametersWithoutIndexSelection) // cannot choose "To index"
                .defaultValue(FROM_BEGINNING) //
                .build();

        final List<Parameter> parameters = ImplicitParameters.getParameters();
        parameters.add(fromParameters);
        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            // Create result column
            final RowMetadata rowMetadata = context.getRowMetadata();
            final String columnId = context.getColumnId();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            context.column(column.getName() + APPENDIX, (r) -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .name(column.getName() + APPENDIX) //
                        .type(Type.get(column.getType())) //
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

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        // create the new column
        final RowMetadata rowMetadata = context.getRowMetadata();
        final String columnId = context.getColumnId();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final String substringColumn = context.column(column.getName() + APPENDIX);

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
