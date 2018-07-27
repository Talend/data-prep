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
package org.talend.dataprep.transformation.actions.fill;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.singletonList;

@Action(GenerateUUID.ACTION_NAME)
public class GenerateUUID extends AbstractActionMetadata implements ColumnAction {

    public static final String ACTION_NAME = "generate_a_uuid";
    private static final String NEW_COLUMN_SUFFIX = "_uuid";

    @Override
    public String getName() {
        return GenerateUUID.ACTION_NAME;
    }

    protected List<ActionsUtils.AdditionalColumn> getAdditionalColumns(ActionContext context) {
        return singletonList(ActionsUtils.additionalColumn().withName(context.getColumnName() + NEW_COLUMN_SUFFIX));
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);
        parameters.add(ActionsUtils.getColumnCreationParameter(locale, true));

        return parameters;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.STRINGS_ADVANCED.getDisplayName(locale);
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.singleton(Behavior.VALUES_COLUMN);
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (ActionsUtils.doesCreateNewColumn(actionContext.getParameters(), true)) {
            ActionsUtils.createNewColumn(actionContext, getAdditionalColumns(actionContext));
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        row.set(ActionsUtils.getTargetColumnId(context), UUID.randomUUID().toString());
    }

}
