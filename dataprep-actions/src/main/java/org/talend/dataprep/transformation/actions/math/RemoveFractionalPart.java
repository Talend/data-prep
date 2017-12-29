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
 * Round towards zero. Never increments the digit prior to a discarded fraction (i.e. truncates)
 *
 * @see RoundingMode#DOWN
 */
@Action(RemoveFractionalPart.ACTION_NAME)
public class RemoveFractionalPart extends AbstractRound {

    /** The action name. */
    public static final String ACTION_NAME = "round_down"; //$NON-NLS-1$

    protected boolean hasPrecisionField() {
        return false;
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    protected RoundingMode getRoundingMode() {
        return RoundingMode.DOWN;
    }
}
