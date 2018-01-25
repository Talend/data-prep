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

import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.talend.dataprep.parameters.Parameter.parameter;
import static org.talend.dataprep.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.parameters.ParameterType.STRING;
import static org.talend.dataprep.parameters.SelectParameter.selectParameter;
import static org.talend.dataprep.transformation.actions.category.ActionCategory.SPLIT;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.CANCELED;
import static org.talend.dataprep.transformation.api.action.context.ActionContext.ActionStatus.OK;

/**
 * Extract tokens from a String cell value based on regex matching groups.
 */
@Action(ExtractStringTokens.EXTRACT_STRING_TOKENS_ACTION_NAME)
public class ExtractStringTokens extends AbstractActionMetadata implements ColumnAction {

    protected static final String MODE_PARAMETER = "extract_mode";

    /**
     * The action name.
     */
    static final String EXTRACT_STRING_TOKENS_ACTION_NAME = "extract_string_tokens"; //$NON-NLS-1$

    static final String SINGLE_COLUMN_MODE = "single_column";

    /**
     * Regex action parameter.
     */
    static final String PARAMETER_REGEX = "regex"; //$NON-NLS-1$

    /**
     * Number of items produces by the action.
     */
    static final String LIMIT = "limit"; //$NON-NLS-1$

    /**
     * Separator for single column mode.
     */
    static final String PARAMETER_SEPARATOR = "concat_separator"; //$NON-NLS-1$

    /**
     * Key to put compiled pattern in action context.
     */
    private static final String PATTERN = "pattern"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    private static final String APPENDIX = "_part_"; //$NON-NLS-1$

    private static final String MULTIPLE_COLUMNS_MODE = "multiple_columns";

    /**
     * This class' logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractStringTokens.class);

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = true;

    @Override
    public String getName() {
        return EXTRACT_STRING_TOKENS_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return SPLIT.getDisplayName(locale);
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);

        parameters.add(
                parameter(locale).setName(PARAMETER_REGEX).setType(STRING).setDefaultValue("(\\w+)").build(this));

        //@formatter:off
        parameters.add(selectParameter(locale)
                .name(MODE_PARAMETER)
                .item(MULTIPLE_COLUMNS_MODE, MULTIPLE_COLUMNS_MODE, parameter(locale).setName(LIMIT).setType(INTEGER).setDefaultValue("4").build(this))
                .item(SINGLE_COLUMN_MODE, SINGLE_COLUMN_MODE, parameter(locale).setName(PARAMETER_SEPARATOR).setType(STRING).setDefaultValue(",").build(this))
                .defaultValue(MULTIPLE_COLUMNS_MODE)
                .build(this)
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
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, getAdditionalColumns(context));
        }
        if (context.getActionStatus() == OK) {

            final String regex = context.getParameters().get(PARAMETER_REGEX);

            // Validate the regex, and put it in context once for all lines:
            // Check 1: not null or empty
            if (isEmpty(regex)) {
                LOGGER.debug("Empty pattern, action canceled");
                context.setActionStatus(CANCELED);
                return;
            }
            // Check 2: valid regex
            try {
                context.get(PATTERN, p -> Pattern.compile(regex));
            } catch (PatternSyntaxException e) {
                LOGGER.debug("Invalid pattern {} --> {}, action canceled", regex, e.getMessage(), e);
                context.setActionStatus(CANCELED);
            }
        }
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        final List<ActionsUtils.AdditionalColumn> additionalColumns = new ArrayList<>();

        final Map<String, String> parameters = context.getParameters();
        int limit = parameters.get(MODE_PARAMETER).equals(MULTIPLE_COLUMNS_MODE)
                ? Integer.parseInt(parameters.get(LIMIT))
                : 1;

        for (int i = 0; i < limit; i++) {
            additionalColumns.add(
                    ActionsUtils.additionalColumn().withKey(Integer.toString(i)).withName(context.getColumnName() + APPENDIX + (i + 1)));
        }

        return additionalColumns;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        final String columnId = context.getColumnId();

        final Map<String, String> newColumns = ActionsUtils.getTargetColumnIds(context);

        // Set the split values in newly created columns
        final String originalValue = row.get(columnId);
        if (originalValue == null) {
            return;
        }

        Pattern pattern = context.get(PATTERN);
        Matcher matcher = pattern.matcher(originalValue);

        List<String> extractedValues = new ArrayList<>();
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                final String matchingValue = matcher.group(i);
                if (matchingValue != null) {
                    extractedValues.add(matchingValue);
                }
            }
        }

        if (parameters.get(MODE_PARAMETER).equals(MULTIPLE_COLUMNS_MODE)) {
            for (int i = 0; i < newColumns.size(); i++) {
                if (i < extractedValues.size()) {
                    row.set(newColumns.get("" + Integer.toString(i)), extractedValues.get(i));
                } else {
                    // If we found less tokens than limit, we complete with empty entries
                    row.set(newColumns.get("" + Integer.toString(i)), EMPTY);
                }
            }
        } else {
            StrBuilder strBuilder = new StrBuilder();
            strBuilder.appendWithSeparators(extractedValues, parameters.get(PARAMETER_SEPARATOR));
            row.set(ActionsUtils.getTargetColumnId(context), strBuilder.toString());
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
