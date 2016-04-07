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
import static org.talend.dataprep.api.type.Type.STRING;
import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.STRINGS;
import static org.talend.dataprep.parameters.ParameterType.REGEX;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.metadata.common.ReplaceOnValueHelper;
import org.talend.dataprep.parameters.Parameter;

@Component(Cut.ACTION_BEAN_PREFIX + Cut.CUT_ACTION_NAME)
public class Cut extends ActionMetadata implements ColumnAction {

    @Autowired
    private ReplaceOnValueHelper regexParametersHelper;

    /**
     * The action name.
     */
    public static final String CUT_ACTION_NAME = "cut"; //$NON-NLS-1$

    /**
     * The pattern "where to cut" parameter name
     */
    public static final String PATTERN_PARAMETER = "pattern"; //$NON-NLS-1$

    public static final String REGEX_HELPER_KEY = "regex_helper";

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return CUT_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return STRINGS.getDisplayName();
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(PATTERN_PARAMETER, REGEX, EMPTY));
        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType()));
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            final Map<String, String> parameters = actionContext.getParameters();
            String rawParam = parameters.get(PATTERN_PARAMETER);

            try {
                actionContext.get(REGEX_HELPER_KEY, p -> regexParametersHelper.build(rawParam, false));
            } catch (IllegalArgumentException e) {
                actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String toCut = row.get(columnId);
        if (toCut != null) {
            final ReplaceOnValueHelper replaceOnValueParameter = context.get(REGEX_HELPER_KEY);

            if (replaceOnValueParameter.matches(toCut)) {
                if (replaceOnValueParameter.getOperator().equals(ReplaceOnValueHelper.REGEX_MODE)) {
                    String value = toCut.replaceAll(replaceOnValueParameter.getToken(), ""); //$NON-NLS-1$
                    row.set(columnId, value);
                } else {
                    String value = toCut.replace(replaceOnValueParameter.getToken(), ""); //$NON-NLS-1$
                    row.set(columnId, value);

                }
            }
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
