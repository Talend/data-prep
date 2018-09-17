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
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.*;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

@Action(Padding.ACTION_NAME)
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

    protected static final String NEW_COLUMN_SUFFIX = "_padded";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS_ADVANCED.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()))
                || Type.NUMERIC.isAssignableFrom(Type.get(column.getType()));
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));
        parameters.add(parameter(locale).setName(SIZE_PARAMETER).setType(INTEGER).setDefaultValue("5").build(this));
        parameters.add(
                parameter(locale).setName(PADDING_CHAR_PARAMETER).setType(STRING).setDefaultValue("0").build(this));

        //@formatter:off
        parameters.add(selectParameter(locale)
                        .name(PADDING_POSITION_PARAMETER)
                        .item(LEFT_POSITION, LEFT_POSITION)
                        .item(RIGHT_POSITION, RIGHT_POSITION)
                        .defaultValue(LEFT_POSITION)
                        .build(this )
        );
        //@formatter:on

        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, singletonList(
                    ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX)));
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();
        final String original = row.get(columnId);

        final String paddingStr = parameters.get(PADDING_CHAR_PARAMETER);
        final String paddingPosition = parameters.get(PADDING_POSITION_PARAMETER);
        //Judge paddingStr is surrogate pair or not
        final boolean replaceCharIsSurrogatePair = Character.isHighSurrogate(paddingStr.charAt(0));
        final int size =
                getRealSize(original, Integer.parseInt(parameters.get(SIZE_PARAMETER)), replaceCharIsSurrogatePair);

        String finallyStr = null;
        if (replaceCharIsSurrogatePair) {
            finallyStr = apply(original, size, paddingStr.substring(0, 2), paddingPosition);
        } else {
            finallyStr = apply(original, size, paddingStr.charAt(0), paddingPosition);
        }
        row.set(ActionsUtils.getTargetColumnId(context), finallyStr);
    }

    // surrogate pair contains two char so that change parameter paddingChar from char to String
    protected String apply(String from, int size, String paddingChar, String position) {
        if (from == null) {
            return EMPTY;
        }
        if (position.equals(LEFT_POSITION)) {
            return leftPad(from, size, paddingChar);
        } else {
            return rightPad(from, size, paddingChar);
        }
    }

    // One surrogate pair contains two char so that compute real size
    protected int getRealSize(String input, int targetSize, boolean replaceCharIsSurrogatePair) {
        final int originalSize = input.length();
        final int realSize = input.codePointCount(0, originalSize);
        if (targetSize > realSize) {
            if (replaceCharIsSurrogatePair) {
                return (targetSize - realSize) * 2 + originalSize;
            } else {
                return targetSize - realSize + originalSize;
            }
        }
        return targetSize;
    }

    protected String apply(String from, int size, char paddingChar, String position) {
        if (from == null) {
            return EMPTY;
        }

        if (position.equals(LEFT_POSITION)) {
            return leftPad(from, size, paddingChar);
        } else {
            return rightPad(from, size, paddingChar);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
