//  ============================================================================
//
//  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.actions.common;

/**
 * This interface mutualize code for actions that have a parameter which is a choice between constant/other column mode.
 */
public interface OtherColumnParameters {

    /**
     * The selected column id.
     */
    String SELECTED_COLUMN_PARAMETER = "selected_column"; //$NON-NLS-1$

    /**
     * Mode: tells if fill value is taken from another column or is a constant
     */
    String MODE_PARAMETER = "mode"; //$NON-NLS-1$

    /**
     * Constant to represents mode where we fill with a constant.
     */
    String OTHER_COLUMN_MODE = "other_column_mode"; //$NON-NLS-1$
    String CONSTANT_MODE = "constant_mode"; //$NON-NLS-1$

    String CONSTANT_VALUE = "constant_value";

}
