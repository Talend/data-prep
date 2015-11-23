package org.talend.dataprep.transformation.api.action.metadata.text;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.SPLIT;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.REGEX;

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
    protected static final String SEPARATOR_PARAMETER = "separator"; //$NON-NLS-1$

    /**
     * The separator manually specified by the user. Should be used only if SEPARATOR_PARAMETER value is 'other'.
     */
    protected static final String MANUAL_SEPARATOR_PARAMETER = "manual_separator"; //$NON-NLS-1$

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
        return SPLIT.getDisplayName();
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(LIMIT, INTEGER, "2"));
        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(SEPARATOR_PARAMETER)
                        .item(":")
                        .item("@")
                        .item(" ")
                        .item(",")
                        .item("-")
                        .item("other", new Parameter(MANUAL_SEPARATOR_PARAMETER, REGEX, EMPTY))
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
        final Stack<String> lastColumnId = new Stack<>();
        lastColumnId.push(columnId);
        for (int i = 0; i < limit; i++) {
            newColumns.add(context.in(this).column(column.getName() + SPLIT_APPENDIX + i,
                    rowMetadata,
                    (r) -> {
                        final ColumnMetadata c = ColumnMetadata.Builder //
                                .column() //
                                .copy(column) //
                                .type(Type.STRING) //
                                .computedId(EMPTY) //
                                .name(column.getName() + SPLIT_APPENDIX) //
                                .build();
                        lastColumnId.push(rowMetadata.insertAfter(lastColumnId.pop(), c));
                        return c;
                    }
            ));
        }

        // Set the split values in newly created columns
        final String originalValue = row.get(columnId);
        if (originalValue == null) {
            return;
        }

        String[] split = new String[0];
        try {
            // Check if separator is a valid regex
            Pattern.compile(realSeparator);

            // execute split when separator is valid
            split = originalValue.split(realSeparator, limit);
        } catch (PatternSyntaxException e) {
            // In case the pattern is not valid: nothing to do, empty string will go in the cells.
        }

        final Iterator<String> iterator = newColumns.iterator();

        for (int i = 0; i < limit && iterator.hasNext(); i++) {
            final String newValue = i < split.length ? split[i] : EMPTY;
            row.set(iterator.next(), newValue);
        }
    }

}
