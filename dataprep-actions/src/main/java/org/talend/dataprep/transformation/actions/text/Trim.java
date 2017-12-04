// ============================================================================
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

import static java.util.Collections.singletonList;
import static org.talend.dataprep.api.type.Type.STRING;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataquality.converters.StringTrimmer;

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
    public static final String STRING_TRIMMER = "string_trimmer"; //$NON-NLS-1$

    /**
     * Keys used in the values of different parameters:
     */
    public static final String CUSTOM = "custom"; //$NON-NLS-1$

    public static final String WHITESPACE = "whitespace"; //$NON-NLS-1$

    protected static final String NEW_COLUMN_SUFFIX = "_trim";

    @Override
    public String getName() {
        return TRIM_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    protected List<AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(new AdditionalColumn(STRING, context.getColumnName() + NEW_COLUMN_SUFFIX));
    }

    @Override
        public List<Parameter> getParameters(Locale locale) {
            final List<Parameter> parameters = super.getParameters(locale);

        // @formatter:off
        parameters.add(SelectParameter.selectParameter(locale)
                .name(PADDING_CHAR_PARAMETER)
                .item(WHITESPACE,WHITESPACE)
                .item(CUSTOM, CUSTOM, Parameter.parameter(locale).setName(CUSTOM_PADDING_CHAR_PARAMETER).setType(ParameterType.STRING).setDefaultValue(StringUtils.EMPTY).build(this))
                .canBeBlank(true)
                .defaultValue(WHITESPACE)
                .build(this ));
        // @formatter:on
        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            actionContext.get(STRING_TRIMMER, p -> new StringTrimmer());
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
            final StringTrimmer stringTrimmer = context.get(STRING_TRIMMER);
            if (CUSTOM.equals(parameters.get(PADDING_CHAR_PARAMETER))) {
                row.set(getTargetColumnId(context), stringTrimmer.removeTrailingAndLeading(toTrim, parameters.get(CUSTOM_PADDING_CHAR_PARAMETER)));
            } else {
                row.set(getTargetColumnId(context), stringTrimmer.removeTrailingAndLeadingWhitespaces(toTrim));
            }
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }
}
