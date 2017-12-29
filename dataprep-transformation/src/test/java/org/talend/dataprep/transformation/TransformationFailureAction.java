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

package org.talend.dataprep.transformation;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.CellAction;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.DataSetAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * A unit test action: only use to test unexpected action failures.
 */
@Action(TransformationFailureAction.TRANSFORMATION_FAILURE_ACTION)
public class TransformationFailureAction extends AbstractActionMetadata implements ColumnAction, CellAction, DataSetAction {

    public static final String TRANSFORMATION_FAILURE_ACTION = "testtransformationfailure";

    @Override
    public String getName() {
        return TRANSFORMATION_FAILURE_ACTION;
    }

    @Override
    public String getCategory(Locale locale) {
        return "TEST";
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return false;
    }

    @Override
    public void applyOnCell(DataSetRow row, ActionContext context) {
        throw new Error("On purpose unchecked exception");
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        throw new Error("On purpose unchecked exception");
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        throw new Error("On purpose unchecked exception");
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.allOf(Behavior.class);
    }
}
