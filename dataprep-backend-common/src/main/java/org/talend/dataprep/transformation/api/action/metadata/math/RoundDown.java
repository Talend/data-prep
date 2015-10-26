// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.api.action.metadata.math;

import java.math.RoundingMode;

import org.springframework.stereotype.Component;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Round towards zero. Never increments the digit prior to a discarded fraction (i.e. truncates)
 * 
 * @see RoundingMode#DOWN
 */
@Component(RoundDown.ACTION_BEAN_PREFIX + RoundDown.ACTION_NAME)
public class RoundDown extends AbstractRound {

    /** The action name. */
    public static final String ACTION_NAME = "round_down"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    protected RoundingMode getRoundingMode() {
        return RoundingMode.DOWN;
    }
}
