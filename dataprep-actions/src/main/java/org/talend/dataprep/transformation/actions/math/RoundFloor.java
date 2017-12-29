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

import java.math.RoundingMode;

import org.talend.dataprep.api.action.Action;

/**
 * This will compute the largest (closest to positive infinity) value that is less than or equal to the cell value and
 * is equal to a mathematical integer.
 *
 * @see RoundingMode#FLOOR
 */
@Action(RoundFloor.ACTION_NAME)
public class RoundFloor extends AbstractRound {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "floor"; //$NON-NLS-1$

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    protected RoundingMode getRoundingMode() {
        return RoundingMode.FLOOR;
    }
}
