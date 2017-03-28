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

import java.util.*;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.talend.dataquality.converters.StringConverter;

/**
 * Remove consecutive repeated characters for a Text.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + RemoveRepeatedChars.ACTION_NAME)
public class RemoveRepeatedChars extends AbstractActionMetadata implements ColumnAction {

    /** Action name. */
    public static final String ACTION_NAME = "remove_repeated_chars"; //$NON-NLS-1$

    private static final String STRING_CONVERT_KEY = "string_convert_key";//$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveRepeatedChars.class);

    /** The selected remmove type within the provided list. */
    protected static final String REMOVE_TYPE = "remove_type"; //$NON-NLS-1$

    /** Keys used in the values of different parameters. */
    protected static final String CUSTOM = "custom"; //$NON-NLS-1$

    /** Remove repeated white spaces(" ","\n","\r","\t","\f")  */
    protected static final String WHITESPACE = "whitespace"; //$NON-NLS-1$

    /** Custom repeated char  */
    protected static final String CUSTOM_REPEAT_CHAR_PARAMETER = "custom_repeat_chars"; //$NON-NLS-1$

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            try {
                Map<String, String> parameters = context.getParameters();
                //for custom repeated chart
                if (CUSTOM.equals(parameters.get(REMOVE_TYPE))) {
                    String customRepChar = parameters.get(CUSTOM_REPEAT_CHAR_PARAMETER);
                    context.get(STRING_CONVERT_KEY, p -> new StringConverter(customRepChar));
                } else {//for repeated whitespace.
                    context.get(STRING_CONVERT_KEY, p -> new StringConverter());
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                context.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String originalValue = row.get(columnId);
        if (StringUtils.isEmpty(originalValue)) {
            return;
        }
        final StringConverter stringConverter = context.get(STRING_CONVERT_KEY);
        String cleanValue = originalValue;
        Map<String, String> parameters = context.getParameters();
        // remove all whitespaces
        if (WHITESPACE.equals(parameters.get(REMOVE_TYPE))) {
            cleanValue = stringConverter.removeRepeatedWhitespaces(originalValue);
        } else {// remove specified repeated chars.
            cleanValue = stringConverter.removeRepeatedChar(originalValue);
        }
        row.set(columnId, cleanValue);
    }


    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(SelectParameter.Builder.builder()
                .name(REMOVE_TYPE)
                .item(WHITESPACE)
                .item(CUSTOM, CUSTOM, new Parameter(CUSTOM_REPEAT_CHAR_PARAMETER, ParameterType.STRING, StringUtils.EMPTY))
                .canBeBlank(true)
                .defaultValue(WHITESPACE)
                .build());
        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

}
