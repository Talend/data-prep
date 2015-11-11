package org.talend.dataprep.transformation.api.action.metadata.text;

import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.ParameterType;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

@Component(Padding.ACTION_BEAN_PREFIX + Padding.ACTION_NAME)
public class Padding extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "padding"; //$NON-NLS-1$

    /**
     * The attended size of cell content after padding.
     */
    public static final String SIZE_PARAMETER = "size"; //$NON-NLS-1$

    /**
     * The the char to repeat to complete size.
     */
    public static final String PADDING_CHAR_PARAMETER = "padding_char"; //$NON-NLS-1$

    /**
     * The position of the char to repeat.
     */
    public static final String PADDING_POSITION_PARAMETER = "padding_position"; //$NON-NLS-1$

    public static final String LEFT_POSITION = "Left";

    public static final String RIGHT_POSITION = "Right";

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.STRINGS_ADVANCED.getDisplayName();
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.isAssignableFrom(Type.get(column.getType())) //
                || Type.NUMERIC.isAssignableFrom(Type.get(column.getType()));
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(SIZE_PARAMETER, ParameterType.INTEGER, "5"));
        parameters.add(new Parameter(PADDING_CHAR_PARAMETER, ParameterType.STRING, "0"));

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(PADDING_POSITION_PARAMETER)
                        .item(LEFT_POSITION)
                        .item(RIGHT_POSITION)
                        .defaultValue(LEFT_POSITION)
                        .build()
        );
        //@formatter:on

        return parameters;
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String original = row.get(columnId);

        final int size = Integer.parseInt(parameters.get(SIZE_PARAMETER));
        final char padding_char = parameters.get(PADDING_CHAR_PARAMETER).charAt(0);
        final String padding_position = parameters.get(PADDING_POSITION_PARAMETER);

        row.set(columnId, apply(original, size, padding_char, padding_position));
    }

    protected String apply(String from, int size, char padding_char, String position) {
        if (from == null) {
            return "";
        }

        if (position.equals(LEFT_POSITION)) {
            return Strings.padStart(from, size, padding_char);
        } else {
            return Strings.padEnd(from, size, padding_char);
        }
    }

}
