package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.STRING;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

/**
 * Split a cell value on a separator.
 */
@Component(Split.ACTION_BEAN_PREFIX + Split.SPLIT_ACTION_NAME)
public class Split extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String SPLIT_ACTION_NAME = "split"; //$NON-NLS-1$

    /**
     * The split column appendix.
     */
    public static final String SPLIT_APPENDIX = "_split"; //$NON-NLS-1$

    /**
     * The separator shown to the user as a list. An item in this list is the value 'other', which allow the user to
     * manually enter its separator.
     */
    private static final String SEPARATOR_PARAMETER = "separator"; //$NON-NLS-1$

    /**
     * The separator manually specified by the user. Should be used only if SEPARATOR_PARAMETER value is 'other'.
     */
    private static final String MANUAL_SEPARATOR_PARAMETER = "manual_separator"; //$NON-NLS-1$

    /**
     * Number of items produces by the split
     */
    private static final String LIMIT = "limit"; //$NON-NLS-1$

    /**
     * @see org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return SPLIT_ACTION_NAME;
    }

    /**
     * @see org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.SPLIT.getDisplayName();
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(LIMIT, ParameterType.INTEGER, "2"));
        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(SEPARATOR_PARAMETER)
                        .item(":")
                        .item("@")
                        .item(" ")
                        .item(",")
                        .item("-")
                        .item("other", new Parameter(MANUAL_SEPARATOR_PARAMETER, STRING, EMPTY))
                        .defaultValue(":")
                        .build()
        );
        //@formatter:on
        return parameters;
    }

    /**
     * @see org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    /**
     * @param parameters the action parameters.
     * @return the separator to use according to the given parameters.
     */
    private String getSeparator(Map<String, String> parameters) {
        return ("other").equals(parameters.get(SEPARATOR_PARAMETER)) ? parameters.get(MANUAL_SEPARATOR_PARAMETER) : parameters
                .get(SEPARATOR_PARAMETER);
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        // Retrieve the separator to use
        final String realSeparator = getSeparator(parameters);
        if (StringUtils.isEmpty(realSeparator)) {
            return;
        }

        // create the new columns
        int limit = Integer.parseInt(parameters.get(LIMIT));
        final RowMetadata rowMetadata = row.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final List<String> newColumns = new ArrayList<>();
        String lastColumnId = columnId;
        for (int i = 0; i < limit; i++) {
            final ColumnMetadata newColumnMetadata = createNewColumn(column);
            final String newColumnId = rowMetadata.insertAfter(lastColumnId, newColumnMetadata);
            newColumns.add(newColumnId);
            lastColumnId = newColumnId;
        }

        // Set the split values in newly created columns
        final String originalValue = row.get(columnId);
        if (originalValue == null) {
            return;
        }
        final Iterator<String> iterator = newColumns.iterator();
        final String[] split = originalValue.split(realSeparator, limit);
        for (int i = 0; i < limit && iterator.hasNext(); i++) {
            final String newValue = i < split.length ? split[i] : EMPTY;
            row.set(iterator.next(), newValue);
        }
    }

    /**
     * Create a new column from current column
     * @param column the current column
     * @return the new created column
     */
    private ColumnMetadata createNewColumn(final ColumnMetadata column) {
        return ColumnMetadata.Builder //
                .column() //
                .name(column.getName() + SPLIT_APPENDIX) //
                .type(Type.get(column.getType())) //
                .empty(column.getQuality().getEmpty()) //
                .invalid(column.getQuality().getInvalid()) //
                .valid(column.getQuality().getValid()) //
                .headerSize(column.getHeaderSize()) //
                .build();
    }
}
