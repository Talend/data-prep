package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.STRINGS;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

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

    private static final String FROM_BEGINNING = "From beginning"; //$NON-NLS-1$

    private static final String TO_END = "To end"; //$NON-NLS-1$

    protected static final String FROM_MODE_PARAMETER = "from_mode"; //$NON-NLS-1$

    protected static final String FROM_INDEX_PARAMETER = "from_index"; //$NON-NLS-1$

    protected static final String TO_MODE_PARAMETER = "to_mode"; //$NON-NLS-1$

    protected static final String TO_INDEX_PARAMETER = "to_index"; //$NON-NLS-1$

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
        List<Parameter> parameters = ImplicitParameters.getParameters();

        // from parameter
        parameters.add(SelectParameter.Builder.builder() //
                .name(FROM_MODE_PARAMETER) //
                .item(FROM_BEGINNING) //
                .item("From index", new Parameter(FROM_INDEX_PARAMETER, ParameterType.INTEGER, "0")) //
                .defaultValue(FROM_BEGINNING) //
                .build());

        // to parameter
        parameters.add(SelectParameter.Builder.builder() //
                .name(TO_MODE_PARAMETER) //
                .item(TO_END) //
                .item("To index", new Parameter(TO_INDEX_PARAMETER, ParameterType.INTEGER, "5")) //
                .defaultValue("To index") //
                .build());

        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        // create the new column
        final RowMetadata rowMetadata = row.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final String substringColumn = context.in(this).column(column.getName() + APPENDIX, rowMetadata, (r) -> {
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

        // Perform substring
        final String value = row.get(columnId);
        if (value == null) {
            return;
        }
        final int realFromIndex = getStartIndex(parameters, value);
        final int realToIndex = getEndIndex(parameters, value);

        try {
            final String newValue = value.substring(realFromIndex, realToIndex);
            row.set(substringColumn, newValue);
        } catch (IndexOutOfBoundsException e) {
            // Nothing to do in that case, just set with the empty string:
            row.set(substringColumn, StringUtils.EMPTY);
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
        final String toMode = parameters.get(TO_MODE_PARAMETER);
        if (toMode.equals(TO_END)) {
            return value.length();
        }
        return Math.min(Integer.parseInt(parameters.get(TO_INDEX_PARAMETER)), value.length());
    }

    /**
     * Compute the start index. This won't be more than the value length
     *
     * @param parameters the parameters
     * @param value the value to substring
     * @return the start index
     */
    private int getStartIndex(final Map<String, String> parameters, String value) {
        final String fromMode = parameters.get(FROM_MODE_PARAMETER);
        final int fromIndex = fromMode.equals(FROM_BEGINNING) ? 0 : Integer.parseInt(parameters.get(FROM_INDEX_PARAMETER));
        return Math.min(fromIndex, value.length());
    }

}
