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
import java.util.List;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;

/**
 * Round towards zero. Never increments the digit prior to a discarded fraction (i.e. truncates)
 * 
 * @see RoundingMode#DOWN
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + RemoveFractionalPart.ACTION_NAME)
public class RemoveFractionalPart extends AbstractRound {

    /** The action name. */
    public static final String ACTION_NAME = "round_down"; //$NON-NLS-1$

    @Override
    public List<Parameter> getParameters() {
        return ImplicitParameters.getParameters();
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
