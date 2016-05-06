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

package org.talend.dataprep.transformation.api.action.metadata.common;

import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Interface used to apply an action on a whole dataset.
 */
public interface DataSetAction {

    /**
     * such action can have very different display mode in recipe ui part
     * so we use a parameter as flag
     */
    static final String DISPLAY_TYPE_PARAMETER = "dataset_action_display_type";

    /**
     * Apply action on the whole dataset.
     * @param row the dataset row.
     * @param context the transformation context.
     */
    void applyOnDataSet(final DataSetRow row, final ActionContext context);

}
