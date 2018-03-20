// ============================================================================
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
import static org.talend.dataprep.api.type.Type.DOUBLE;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ActionsUtils;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.util.NumericHelper;

/**
 * This will compute the absolute value for numerical columns.
 */
@Action(Absolute.ABSOLUTE_ACTION_NAME)
public class Absolute extends AbstractActionMetadata implements ColumnAction {

    public static final String ABSOLUTE_ACTION_NAME = "absolute"; //$NON-NLS-1$

    private static final boolean CREATE_NEW_COLUMN_DEFAULT = false;

    private final Type type;

    public Absolute() {
        type = Type.INTEGER;
    }

    public Absolute(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return ABSOLUTE_ACTION_NAME;
    }

    @Override
    public String getCategory(Locale locale) {
        return ActionCategory.MATH.getDisplayName(locale);
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        return ActionsUtils.appendColumnCreationParameter(super.getParameters(locale), locale, CREATE_NEW_COLUMN_DEFAULT);
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (ActionsUtils.doesCreateNewColumn(context.getParameters(), CREATE_NEW_COLUMN_DEFAULT)) {
            ActionsUtils.createNewColumn(context, singletonList(
                    ActionsUtils.additionalColumn().withName(context.getColumnName() + "_absolute").withType(DOUBLE)));
        }
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.FLOAT.equals(Type.get(column.getType())) //
                || Type.DOUBLE.equals(Type.get(column.getType())) //
                || Type.INTEGER.equals(Type.get(column.getType()));
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        if (value == null) {
            return;
        }
        String absValueStr = null;
        if(NumericHelper.isBigDecimal(value)) {
            absValueStr = BigDecimalParser.toBigDecimal(value).abs().toPlainString();
        }
        if (absValueStr != null) {
            row.set(ActionsUtils.getTargetColumnId(context), absValueStr);
        }
    }

    @Override
    public ActionDefinition adapt(ColumnMetadata column) {
        if (column == null || !acceptField(column)) {
            return this;
        }
        return new Absolute(type);
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
