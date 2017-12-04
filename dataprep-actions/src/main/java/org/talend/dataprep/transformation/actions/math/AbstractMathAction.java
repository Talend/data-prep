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
package org.talend.dataprep.transformation.actions.math;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Abstract Action for math operations
 */
public abstract class AbstractMathAction extends AbstractActionMetadata implements ColumnAction {

    protected static final String ERROR_RESULT = StringUtils.EMPTY;

    @Override
    protected List<AdditionalColumn> getAdditionalColumns(ActionContext context) {
        final List<AdditionalColumn> additionalColumns = new ArrayList<>();

        additionalColumns.add(new AdditionalColumn(Type.DOUBLE, null));

        return additionalColumns;
    }

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
