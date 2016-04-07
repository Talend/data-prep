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

import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.BOOLEAN;
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.parameters.ParameterType.REGEX;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ReplaceOnValueHelper;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;

@Component(MatchesPattern.ACTION_BEAN_PREFIX + MatchesPattern.MATCHES_PATTERN_ACTION_NAME)
public class MatchesPattern extends ActionMetadata implements ColumnAction {

    @Autowired
    private ReplaceOnValueHelper regexParametersHelper;
    
    /**
     * The action name.
     */
    public static final String MATCHES_PATTERN_ACTION_NAME = "matches_pattern"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_matching"; //$NON-NLS-1$

    /**
     * The pattern shown to the user as a list. An item in this list is the value 'other', which allow the user to
     * manually enter his pattern.
     */
    public static final String PATTERN_PARAMETER = "proposed_pattern"; //$NON-NLS-1$

    /**
     * The pattern manually specified by the user. Should be used only if PATTERN_PARAMETER value is 'other'.
     */
    protected static final String MANUAL_PATTERN_PARAMETER = "manual_pattern"; //$NON-NLS-1$

    public static final String CUSTOM = "custom";

    public static final String REGEX_HELPER_KEY = "regex_helper";

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return MATCHES_PATTERN_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType()));
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
				.item("[a-z]+")
				.item("[A-Z]+")
				.item("[0-9]+")
				.item("[a-zA-Z]+")
				.item("[a-zA-Z0-9]+")
				.item(CUSTOM, new Parameter(MANUAL_PATTERN_PARAMETER, REGEX, EMPTY))
				.defaultValue("[a-zA-Z]+")
				.build());
		// @formatter:on
        return parameters;
    }

    /**
     * @param parameters the action parameters.
     * @return the pattern to use according to the given parameters.
     */
    private ReplaceOnValueHelper getPattern(Map<String, String> parameters) {
        if (CUSTOM.equals(parameters.get(PATTERN_PARAMETER))) {
            final String jsonString = parameters.get(MANUAL_PATTERN_PARAMETER);
            return regexParametersHelper.build(jsonString, true);
        } else {
            return new ReplaceOnValueHelper(parameters.get(PATTERN_PARAMETER), ReplaceOnValueHelper.REGEX_MODE);
        }
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            try {
                context.get(REGEX_HELPER_KEY, p -> getPattern(context.getParameters()));
            } catch (IllegalArgumentException e) {
                context.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
            // Create result column
            final String columnId = context.getColumnId();
            final RowMetadata rowMetadata = context.getRowMetadata();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            if (column != null) {
                context.column(column.getName() + APPENDIX, r -> {
                    final ColumnMetadata c = ColumnMetadata.Builder //
                            .column() //
                            .name(column.getName() + APPENDIX) //
                            .type(BOOLEAN) //
                            .empty(column.getQuality().getEmpty()) //
                            .invalid(column.getQuality().getInvalid()) //
                            .valid(column.getQuality().getValid()) //
                            .headerSize(column.getHeaderSize()) //
                            .build();
                    rowMetadata.insertAfter(columnId, c);
                    return c;
                });
            }
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        // Retrieve the pattern to use
        final String columnId = context.getColumnId();

        // create new column and append it after current column
        final RowMetadata rowMetadata = context.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final String matchingColumn = context.column(column.getName() + APPENDIX);

        final String value = row.get(columnId);
        final String newValue = toStringTrueFalse(computeNewValue(value, context));
        row.set(matchingColumn, newValue);
    }

    /**
     * Computes if a given string matches or not given pattern.
     *
     * @param value the value to test
     * @param actionContext context expected to contain the compiled pattern to match the value against.
     * @return true if 'value' matches 'pattern', false if not or if 'pattern' is not a valid pattern or is null or empty
     */
    protected boolean computeNewValue(String value, ActionContext actionContext) {
        // There are direct calls to this method from unit tests, normally such checks are done during transformation.
        if (actionContext.getActionStatus() != ActionContext.ActionStatus.OK) {
            return false;
        }
        final ReplaceOnValueHelper replaceOnValueParameter = actionContext.get(REGEX_HELPER_KEY);

        return replaceOnValueParameter.matches(value);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
