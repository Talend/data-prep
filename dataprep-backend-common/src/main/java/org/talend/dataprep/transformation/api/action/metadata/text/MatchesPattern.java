package org.talend.dataprep.transformation.api.action.metadata.text;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;
import static org.talend.dataprep.transformation.api.action.parameters.ParameterType.STRING;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

@Component(MatchesPattern.ACTION_BEAN_PREFIX + MatchesPattern.MATCHES_PATTERN_ACTION_NAME)
public class MatchesPattern extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String MATCHES_PATTERN_ACTION_NAME = "matches_pattern"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_matching"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return MATCHES_PATTERN_ACTION_NAME;
    }

    /**
     * The pattern shown to the user as a list. An item in this list is the value 'other', which allow the user to
     * manually enter his pattern.
     */
    private static final String PATTERN_PARAMETER = "proposed_pattern"; //$NON-NLS-1$

    /**
     * The pattern manually specified by the user. Should be used only if PATTERN_PARAMETER value is 'other'.
     */
    private static final String MANUAL_PATTERN_PARAMETER = "manual_pattern"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        // @formatter:off
		parameters.add(SelectParameter.Builder.builder()
				.name(PATTERN_PARAMETER)
				.item("[a-z]*")
				.item("[A-Z]*")
				.item("[0-9]*")
				.item("[a-zA-Z]*")
				.item("[a-zA-Z0-9]*")
				.item(" *")
				.item(".*")
				.item("other", new Parameter(MANUAL_PATTERN_PARAMETER, STRING, EMPTY))
				.defaultValue("[a-zA-Z]*")
				.build());
		// @formatter:on
        return parameters;
    }

    /**
     * @param parameters the action parameters.
     * @return the pattern to use according to the given parameters.
     */
    private String getPattern(Map<String, String> parameters) {
        return ("other").equals(parameters.get(PATTERN_PARAMETER)) ? parameters.get(MANUAL_PATTERN_PARAMETER) : parameters
                .get(PATTERN_PARAMETER);
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, TransformationContext, Map, String)
     */
    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        // Retrieve the pattern to use
        final String realPattern = getPattern(parameters);

        // create new column and append it after current column
        final RowMetadata rowMetadata = row.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final ColumnMetadata newCol = createNewColumn(column);
        final String matchingColumn = rowMetadata.insertAfter(columnId, newCol);

        final String value = row.get(columnId);

        final String newValue = toStringTrueFalse(computeNewValue(value, realPattern));

        row.set(matchingColumn, newValue);
    }

    /**
     * Computes if a given string matches or not given pattern.
     *
     * @param value the value to test
     * @param pattern the pattern to match the value against
     * @return true if 'value' matches 'pattern', false if not or if 'pattern' is not a vlid pattern or is null or empty
     */
    protected boolean computeNewValue(String value, String pattern) {
        if (pattern == null || pattern.length() < 1) {
            // In case of empty pattern, consider that value does not match:
            return false;
        }
        try {
            final Pattern p = Pattern.compile(pattern);
            final Matcher matcher = p.matcher(value == null ? "" : value);
            return matcher.matches();
        } catch (PatternSyntaxException e) {
            // In case of wrong pattern, consider that value does not match:
            return false;
        }
    }

    /**
     * Create the new "string matching" column
     *
     * @param column the current column metadata
     * @return the new column metadata
     */
    private ColumnMetadata createNewColumn(final ColumnMetadata column) {
        return ColumnMetadata.Builder //
                .column() //
                .name(column.getName() + APPENDIX) //
                .type(Type.BOOLEAN) //
                .empty(column.getQuality().getEmpty()) //
                .invalid(column.getQuality().getInvalid()) //
                .valid(column.getQuality().getValid()) //
                .headerSize(column.getHeaderSize()) //
                .build();
    }

}
