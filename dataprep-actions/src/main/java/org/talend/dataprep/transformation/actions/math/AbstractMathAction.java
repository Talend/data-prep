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
package org.talend.dataprep.transformation.actions.math;

import static java.util.Collections.singletonList;
import static org.talend.dataprep.transformation.actions.common.ActionsUtils.appendColumnCreationParameter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Abstract Action for math operations
 */
public abstract class AbstractMathAction extends AbstractActionMetadata implements ColumnAction {

    protected static final String ERROR_RESULT = StringUtils.EMPTY;

    public static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    @Override
    public List<Parameter> getParameters(Locale locale) {
        return appendColumnCreationParameter(super.getParameters(locale), locale, CREATE_NEW_COLUMN_DEFAULT);
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, singletonList(
                    ActionsUtils.additionalColumn().withName(context.getColumnName() + getSuffix(context)).withType(Type.DOUBLE)));
        }
    }

    protected abstract String getSuffix(ActionContext context);

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.NUMERIC.isAssignableFrom(column.getType());
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.MATH.getDisplayName(locale);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.singleton(Behavior.METADATA_CREATE_COLUMNS);
    }
}
