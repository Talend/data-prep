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

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + Padding.ACTION_NAME)
public class Padding extends AbstractActionMetadata implements ColumnAction {

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

    public static final String LEFT_POSITION = "left";

    public static final String RIGHT_POSITION = "right";

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.STRINGS_ADVANCED.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType())) || Type.NUMERIC.isAssignableFrom(Type.get(column.getType()));
    }

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

        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();
        final String original = row.get(columnId);

        final int size = Integer.parseInt(parameters.get(SIZE_PARAMETER));
        final char paddingChar = parameters.get(PADDING_CHAR_PARAMETER).charAt(0);
        final String paddingPosition = parameters.get(PADDING_POSITION_PARAMETER);

        row.set(columnId, apply(original, size, paddingChar, paddingPosition));
    }

    protected String apply(String from, int size, char paddingChar, String position) {
        if (from == null) {
            return StringUtils.EMPTY;
        }

        if (position.equals(LEFT_POSITION)) {
            return StringUtils.leftPad(from, size, paddingChar);
        } else {
            return StringUtils.rightPad(from, size, paddingChar);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
