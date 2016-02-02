//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.SPLIT;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

/**
 * Extract tokens from a String cell value based on regex matching groups.
 */
@Component(ExtractStringTokens.ACTION_BEAN_PREFIX + ExtractStringTokens.EXTRACT_STRING_TOKENS_ACTION_NAME)
public class ExtractStringTokens extends ActionMetadata implements ColumnAction {

    /** The action name. */
    public static final String EXTRACT_STRING_TOKENS_ACTION_NAME = "extract_string_tokens"; //$NON-NLS-1$

    protected static final String MODE_PARAMETER = "extract_mode";

    protected static final String MULTIPLE_COLUMNS_MODE = "multiple_columns";

    protected static final String SINGLE_COLUMN_MODE = "single_column";

    /** The column appendix. */
    public static final String APPENDIX = "_part"; //$NON-NLS-1$

    /** Regex action parameter. */
    protected static final String PARAMETER_REGEX = "regex"; //$NON-NLS-1$

    /** Key to put compiled pattern in action context. */
    private static final String PATTERN = "pattern"; //$NON-NLS-1$

    /** Number of items produces by the action. */
    protected static final String LIMIT = "limit"; //$NON-NLS-1$

    /** Separator for single column mode. */
    protected static final String PARAMETER_SEPARATOR = "concat_separator"; //$NON-NLS-1$

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractStringTokens.class);

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return EXTRACT_STRING_TOKENS_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return SPLIT.getDisplayName();
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        parameters.add(new Parameter(PARAMETER_REGEX, STRING, "#(\\w+)"));

        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(MODE_PARAMETER)
                        .item(MULTIPLE_COLUMNS_MODE, new Parameter(LIMIT, INTEGER, "4"))
                        .item(SINGLE_COLUMN_MODE, new Parameter(PARAMETER_SEPARATOR, STRING, ","))
                        .defaultValue(MULTIPLE_COLUMNS_MODE)
                        .build()
        );
        //@formatter:on

        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    /**
     * @see ActionMetadata#compile(ActionContext)
     */
    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {

            final String regex = actionContext.getParameters().get(PARAMETER_REGEX);

            // Validate the regex, and put it in context once for all lines:
            // Check 1: not null or empty
            if (StringUtils.isEmpty(regex)) {
                LOGGER.debug("Empty pattern, action canceled");
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
                return;
            }
            // Check 2: valid regex
            try {
                actionContext.get(PATTERN, (p) -> Pattern.compile(regex));
            } catch (PatternSyntaxException e) {
                LOGGER.debug("Invalid pattern {} --> {}, action canceled", regex, e.getMessage());
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        final String columnId = context.getColumnId();

        // create the new columns
        int limit = (parameters.get(MODE_PARAMETER).equals(MULTIPLE_COLUMNS_MODE) ? Integer.parseInt(parameters.get(LIMIT)) : 1);

        final RowMetadata rowMetadata = row.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final List<String> newColumns = new ArrayList<>();
        final Stack<String> lastColumnId = new Stack<>();
        lastColumnId.push(columnId);
        for (int i = 0; i < limit; i++) {
            newColumns.add(context.column(column.getName() + APPENDIX + i, (r) -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .type(Type.STRING) //
                        .computedId(StringUtils.EMPTY) //
                        .name(column.getName() + APPENDIX) //
                        .build();
                lastColumnId.push(rowMetadata.insertAfter(lastColumnId.pop(), c));
                return c;
            }));
        }

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
                if (i<extractedValues.size()) {
                    row.set(newColumns.get(i), extractedValues.get(i));
                } else {
                    // If we found less tokens than limit, we complete with empty entries
                    row.set(newColumns.get(i), EMPTY);
                }
            }
        } else {
            StrBuilder strBuilder = new StrBuilder();
            strBuilder.appendWithSeparators(extractedValues, parameters.get(PARAMETER_SEPARATOR));
            row.set(newColumns.get(0), strBuilder.toString());
        }
    }

}
