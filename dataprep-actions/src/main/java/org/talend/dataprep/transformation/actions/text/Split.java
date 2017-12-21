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

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.SPLIT;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

import java.util.*;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Split a cell value on a separator.
 */
@Action(Split.SPLIT_ACTION_NAME)
public class Split extends AbstractActionMetadata implements ColumnAction {

    /** The action name. */
    public static final String SPLIT_ACTION_NAME = "split"; //$NON-NLS-1$

    /** The split column appendix. */
    public static final String SPLIT_APPENDIX = "_split_"; //$NON-NLS-1$

    /** The selected separator within the provided list. */
    protected static final String SEPARATOR_PARAMETER = "separator"; //$NON-NLS-1$

    /** Choice of other separator as string. */
    protected static final String OTHER_STRING = "other_string";

    /** Choice of other separator as regex. */
    protected static final String OTHER_REGEX = "other_regex";

    /** The string separator specified by the user. Should be used only if SEPARATOR_PARAMETER value is 'other'. */
    protected static final String MANUAL_SEPARATOR_PARAMETER_STRING = "manual_separator_string"; //$NON-NLS-1$

    /** The regex separator specified by the user. Should be used only if SEPARATOR_PARAMETER value is 'other'. */
    protected static final String MANUAL_SEPARATOR_PARAMETER_REGEX = "manual_separator_regex"; //$NON-NLS-1$

    /** Number of items produces by the split. */
    protected static final String LIMIT = "limit"; //$NON-NLS-1$

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Split.class);

    @Override
    public String getName() {
        return SPLIT_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return SPLIT.getDisplayName(locale);
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(parameter(locale).setName(LIMIT).setType(INTEGER).setDefaultValue("2").build(this));
        //@formatter:off
        parameters.add(selectParameter(locale)
                        .name(SEPARATOR_PARAMETER)
                        .canBeBlank(true)
                        .item(":")
                        .item(";")
                        .item(",")
                        .item("@")
                        .item("-")
                        .item("_")
                        .item(" ", "space")
                        .item("\t", "tabulation")
                        .item(OTHER_STRING, OTHER_STRING, //
                                parameter(locale).setName(MANUAL_SEPARATOR_PARAMETER_STRING).setType(STRING).setDefaultValue(EMPTY).build(this))
                        .item(OTHER_REGEX, OTHER_REGEX, //
                                parameter(locale).setName(MANUAL_SEPARATOR_PARAMETER_REGEX).setType(STRING).setDefaultValue(EMPTY).build(this))
                        .defaultValue(":")
                        .build(this )
        );
        //@formatter:on
        return parameters;
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), true)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {
            if (isEmpty(getSeparator(context))) {
                LOGGER.warn("Cannot split on an empty separator");
                context.setActionStatus(CANCELED);
            }
        }
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        final List<ActionsUtils.AdditionalColumn> additionalColumns = new ArrayList<>();

        final RowMetadata rowMetadata = context.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(context.getColumnId());

        int limit = Integer.parseInt(context.getParameters().get(LIMIT));

        for (int i = 0; i < limit; i++) {
            additionalColumns.add(
                    ActionsUtils.additionalColumn().withKey("" + i).withName(column.getName() + SPLIT_APPENDIX + (i + 1)));
        }

        return additionalColumns;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        final String columnId = context.getColumnId();
        // Set the split values in newly created columns
        final String originalValue = row.get(columnId);
        if (originalValue == null) {
            return;
        }
        // Perform the split
        String realSeparator = getSeparator(context);
        if (!isRegexMode(context)) {
            realSeparator = '[' + realSeparator + ']';
        }
        final int limit = Integer.parseInt(parameters.get(LIMIT));
        final String[] split = originalValue.split(realSeparator, limit);
        final Map<String, String> newColumns = ActionsUtils.getTargetColumnIds(context);
        if (split.length != 0) {
            for (int i = 0; i < limit; i++) {
                final String newValue = i < split.length ? split[i] : EMPTY;
                row.set(newColumns.get("" + i), newValue);
            }
        }
    }

    /**
     * @param context The action context.
     * @return True if the separator is a regex.
     */
    private boolean isRegexMode(ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        return StringUtils.equals(OTHER_REGEX, parameters.get(SEPARATOR_PARAMETER));
    }

    /**
     * @param context The action context.
     * @return The separator from the parameters.
     */
    private String getSeparator(ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        if (StringUtils.equals(OTHER_STRING, parameters.get(SEPARATOR_PARAMETER))) {
            return parameters.get(MANUAL_SEPARATOR_PARAMETER_STRING);
        } else if (StringUtils.equals(OTHER_REGEX, parameters.get(SEPARATOR_PARAMETER))) {
            return parameters.get(MANUAL_SEPARATOR_PARAMETER_REGEX);
        } else {
            return parameters.get(SEPARATOR_PARAMETER);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
}
