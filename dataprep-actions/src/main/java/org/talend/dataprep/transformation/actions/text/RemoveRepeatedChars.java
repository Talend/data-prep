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

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.converters.DuplicateCharEraser;

import javax.annotation.Nonnull;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

/**
 * Remove consecutive repeated characters for a Text.
 */
@Action(RemoveRepeatedChars.ACTION_NAME)
public class RemoveRepeatedChars extends AbstractActionMetadata implements ColumnAction {

    /**
     * Action name.
     */
    public static final String ACTION_NAME = "remove_repeated_chars";

    /**
     * The selected remmove type within the provided list.
     */
    static final String REMOVE_TYPE = "remove_type";

    /**
     * Keys used in the values of different parameters.
     */
    static final String CUSTOM = "custom";

    /**
     * Custom repeated char
     */
    static final String CUSTOM_REPEAT_CHAR_PARAMETER = "custom_repeat_chars";

    static final String NEW_COLUMN_SUFFIX = "_without_consecutive";

    /**
     * Remove repeated white spaces(" ","\n","\r","\t","\f").
     */
    private static final String WHITESPACE = "whitespace";

    private static final String DUPLICATE_CHAR_ERASER_KEY = "duplicate_char_eraser_key";

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX).withType(STRING));
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, CREATE_NEW_COLUMN_DEFAULT));
        parameters.add(selectParameter(locale)
                .name(REMOVE_TYPE)
                .item(WHITESPACE, WHITESPACE)
                .item(CUSTOM, CUSTOM, parameter(locale).setName(CUSTOM_REPEAT_CHAR_PARAMETER)
                        .setType(ParameterType.STRING)
                        .setDefaultValue(EMPTY)
                        .build(this))
                .canBeBlank(false)
                .defaultValue(WHITESPACE)
                .build(this));
        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {
            Map<String, String> parameters = context.getParameters();
            if (CUSTOM.equals(parameters.get(REMOVE_TYPE))) { //for custom repeated chart
                String customChars = parameters.get(CUSTOM_REPEAT_CHAR_PARAMETER);
                context.get(DUPLICATE_CHAR_ERASER_KEY, p -> new DuplicateCharEraser(customChars));
            } else { //for repeated whitespace.
                context.get(DUPLICATE_CHAR_ERASER_KEY, p -> new DuplicateCharEraser());
            }
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String originalValue = row.get(columnId);
        if (StringUtils.isEmpty(originalValue)) {
            row.set(ActionsUtils.getTargetColumnId(context), originalValue);
            return;
        }
        final DuplicateCharEraser duplicateCharEraser = context.get(DUPLICATE_CHAR_ERASER_KEY);
        String cleanValue = duplicateCharEraser.removeRepeatedChar(originalValue);
        row.set(ActionsUtils.getTargetColumnId(context), cleanValue);
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS.getDisplayName(locale);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.isAssignableFrom(Type.get(column.getType()));
    }

}
