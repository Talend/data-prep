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
import org.talend.dataquality.converters.DuplicateCharEraser;

/**
 * Remove consecutive repeated characters for a Text.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + RemoveRepeatedChars.ACTION_NAME)
public class RemoveRepeatedChars extends AbstractActionMetadata implements ColumnAction {

    /** Action name. */
    public static final String ACTION_NAME = "remove_repeated_chars";

    private static final String DUPLICATE_CHAR_ERASER_KEY = "duplicate_char_eraser_key";

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveRepeatedChars.class);

    /** The selected remmove type within the provided list.*/
    protected static final String REMOVE_TYPE = "remove_type";

    /** Keys used in the values of different parameters. */
    protected static final String CUSTOM = "custom";

    /** Remove repeated white spaces(" ","\n","\r","\t","\f"). */
    protected static final String WHITESPACE = "whitespace";

    /** Custom repeated char  */
    protected static final String CUSTOM_REPEAT_CHAR_PARAMETER = "custom_repeat_chars";

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            try {
                Map<String, String> parameters = context.getParameters();
                if (CUSTOM.equals(parameters.get(REMOVE_TYPE))) {//for custom repeated chart
                    String customChars = parameters.get(CUSTOM_REPEAT_CHAR_PARAMETER);
                    char[] chars = customChars.toCharArray();
                    if (chars.length != 1) {//API only support a char not String.
                        throw new IllegalArgumentException("'Repeated character' only support a valid character not a String!");
                    }
                    context.get(DUPLICATE_CHAR_ERASER_KEY, p -> new DuplicateCharEraser(chars[0]));
                } else {//for repeated whitespace.
                    context.get(DUPLICATE_CHAR_ERASER_KEY, p -> new DuplicateCharEraser());
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
        final DuplicateCharEraser duplicateCharEraser = context.get(DUPLICATE_CHAR_ERASER_KEY);
        String cleanValue = originalValue;
        Map<String, String> parameters = context.getParameters();
        cleanValue = duplicateCharEraser.removeRepeatedChar(originalValue);
        row.set(columnId, cleanValue);
    }


    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(SelectParameter.Builder.builder()
                .name(REMOVE_TYPE)
                .item(WHITESPACE, WHITESPACE)
                .item(CUSTOM, CUSTOM, new Parameter(CUSTOM_REPEAT_CHAR_PARAMETER, ParameterType.STRING, StringUtils.EMPTY))
                .canBeBlank(false)
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
