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
import org.talend.dataquality.converters.StringConverter;

/**
 * Trim leading and trailing characters.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + Trim.TRIM_ACTION_NAME)
public class Trim extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String TRIM_ACTION_NAME = "trim"; //$NON-NLS-1$

    /** Padding Character. */
    public static final String PADDING_CHAR_PARAMETER = "padding_character"; //$NON-NLS-1$

    /** Custom Padding Character. */
    public static final String CUSTOM_PADDING_CHAR_PARAMETER = "custom_padding_character"; //$NON-NLS-1$

    /** String Converter help class. */
    public static final String STRING_CONVERTRT = "string_converter"; //$NON-NLS-1$

    /**
     * Keys used in the values of different parameters:
     */
    public static final String CUSTOM = "custom"; //$NON-NLS-1$

    @Override
    public String getName() {
        return TRIM_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        // @formatter:off
        parameters.add(SelectParameter.Builder.builder()
                .name(PADDING_CHAR_PARAMETER)
                .item(" ",       "whitespace"               ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u0009", "character_tabulation"     ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u000A", "line_feed_(lf)"           ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u000B", "line_tabulation"          ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u000C", "form_feed_(ff)"           ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u000D", "carriage_return_(cr)"     ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u0085", "next_line_(nel)"          ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u00A0", "no_break_space"           ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u1680", "ogham_space_mark"         ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u180E", "mongolian_vowel_separator") //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2000", "en_quad"                  ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2001", "em_quad"                  ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2002", "en_space"                 ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2003", "em_space"                 ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2004", "three_per_em_space"       ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2005", "four_per_em_space"        ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2006", "six_per_em_space"         ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2007", "figure_space"             ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2008", "punctuation_space"        ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2009", "thin_space"               ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u200A", "hair_space"               ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2028", "line_separator"           ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u2029", "paragraph_separator"      ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u202F", "narrow_no_break_space"    ) //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u205F", "medium_mathematical_space") //$NON-NLS-1$ //$NON-NLS-2$
                .item("\\u3000", "ideographic_space"        ) //$NON-NLS-1$ //$NON-NLS-2$
                .item(CUSTOM, CUSTOM,new Parameter(CUSTOM_PADDING_CHAR_PARAMETER, ParameterType.STRING, StringUtils.EMPTY)) 
                .canBeBlank(true)
                .defaultValue(" ") //$NON-NLS-1$
                .build());
        // @formatter:on
        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            actionContext.get(STRING_CONVERTRT, p -> new StringConverter());
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String toTrim = row.get(columnId);
        if (toTrim != null) {
            final Map<String, String> parameters = context.getParameters();
            String removeChar;
            if (CUSTOM.equals(parameters.get(PADDING_CHAR_PARAMETER))) {
                removeChar = parameters.get(CUSTOM_PADDING_CHAR_PARAMETER);
            } else {
                removeChar = parameters.get(PADDING_CHAR_PARAMETER);
            }
            final StringConverter stringConverter = context.get(STRING_CONVERTRT);
            row.set(columnId, stringConverter.removeTrailingAndLeading(toTrim, removeChar));
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }
}
