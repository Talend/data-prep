package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.STRING;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.CellAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;

@Component(ReplaceOnValue.ACTION_BEAN_PREFIX + ReplaceOnValue.REPLACE_ON_VALUE_ACTION_NAME)
public class ReplaceOnValue extends AbstractActionMetadata implements ColumnAction, CellAction {

    /**
     * The action name.
     */
    public static final String REPLACE_ON_VALUE_ACTION_NAME = "replace_on_value"; //$NON-NLS-1$

    /**
     * Value to match
     */
    public static final String CELL_VALUE_PARAMETER = "cell_value"; //$NON-NLS-1$

    /**
     * Replace Value
     */
    public static final String REPLACE_VALUE_PARAMETER = "replace_value"; //$NON-NLS-1$

    /**
     * Scope Value
     */
    public static final String REPLACE_ENTIRE_CELL_PARAMETER = "replace_entire_cell"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return REPLACE_ON_VALUE_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(CELL_VALUE_PARAMETER, ParameterType.STRING, EMPTY));
        parameters.add(new Parameter(REPLACE_VALUE_PARAMETER, ParameterType.STRING, EMPTY));
        parameters.add(new Parameter(REPLACE_ENTIRE_CELL_PARAMETER, ParameterType.BOOLEAN, "false"));
        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType()));
    }

    private void apply(DataSetRow row, Map<String, String> parameters, String columnId) {
        final String value = row.get(columnId);

        // defensive programming against null pointer exception
        if (value == null) {
            return;
        }

        final String newValue = computeNewValue(value, //
                parameters.get(CELL_VALUE_PARAMETER), //
                parameters.get(REPLACE_VALUE_PARAMETER), //
                new Boolean(parameters.get(REPLACE_ENTIRE_CELL_PARAMETER)));
        row.set(columnId, newValue);
    }

    protected String computeNewValue(String originalValue, String regexp, String replacement, boolean replaceEntireCell) {
        if (originalValue == null) {
            return originalValue;
        }

        try {
            Pattern p = Pattern.compile(regexp);
            final Matcher matcher = p.matcher(originalValue);

            if (replaceEntireCell) {
                if (matcher.matches()) {
                    return replacement;
                } else {
                    return originalValue;
                }
            } else {
                return matcher.replaceAll(replacement);
            }
        } catch (PatternSyntaxException e) {
            // In case the pattern is not valid, consider that the value does not match: nothing to do.
            return originalValue;
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        apply(row, parameters, columnId);
    }

    /**
     * @see CellAction#applyOnCell(DataSetRow, TransformationContext, Map, Long, String)
     */
    @Override
    public void applyOnCell(DataSetRow row, TransformationContext context, Map<String, String> parameters, Long rowId,
            String columnId) {
        apply(row, parameters, columnId);
    }
}
